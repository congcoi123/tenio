/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.tenio.examples.example3;

import com.tenio.common.data.DataType;
import com.tenio.common.data.DataUtility;
import com.tenio.common.data.zero.ZeroArray;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.data.zero.utility.ZeroUtility;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.examples.client.ClientUtility;
import com.tenio.examples.client.DatagramListener;
import com.tenio.examples.client.SocketListener;
import com.tenio.examples.client.TCP;
import com.tenio.examples.client.UDP;
import com.tenio.examples.server.SharedEventKey;
import com.tenio.examples.server.UdpEstablishedState;

/**
 * This class shows how a client communicates with the server:<br>
 * 1. Create connections.<br>
 * 2. Send a login request.<br>
 * 3. Receive a response for login success and send a UDP connection
 * request.<br>
 * 4. Receive a response for allowed UDP connection.<br>
 * 5. Send messages via UDP connection and get these echoes from the server.<br>
 * 6. Close connections.
 */
public final class TestClientAttach implements SocketListener, DatagramListener {

  private static final int SOCKET_PORT = 8032;
  private final TCP tcp;
  private final String playerName;
  private UDP udp;

  public TestClientAttach() {
    playerName = ClientUtility.generateRandomString(5);

    // create a new TCP object and listen for this port
    tcp = new TCP(SOCKET_PORT);
    tcp.receive(this);

    // send a login request
    var data =
        ZeroUtility.newZeroMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
    var message = ServerMessage.newInstance().setData(data);
    tcp.send(message);

    System.out.println("Login Request -> " + message);
  }

  /**
   * The entry point.
   */
  public static void main(String[] args) {
    new TestClientAttach();
  }

  @Override
  public void onReceivedTCP(byte[] binaries) {
    var dat = DataUtility.binaryToCollection(DataType.ZERO, binaries);
    var message = ServerMessage.newInstance().setData(dat);

    System.err.println("[RECV FROM SERVER TCP] -> " + message);
    ZeroArray pack = ((ZeroMap) message.getData()).getZeroArray(SharedEventKey.KEY_ALLOW_TO_ATTACH);

    switch (pack.getByte(0)) {
      case UdpEstablishedState.ALLOW_TO_ATTACH: {
        // now you can send request for UDP connection request
        var data =
            ZeroUtility.newZeroMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
        var request = ServerMessage.newInstance().setData(data);
        // create a new UDP object and listen for this port
        udp = new UDP(pack.getInteger(1));
        udp.receive(this);
        udp.send(request);

        System.out.println("Request a UDP connection -> " + request);
      }
      break;

      case UdpEstablishedState.ATTACHED: {
        // the UDP connected successful, you now can send test requests
        System.out.println("Start the conversation ...");

        for (int i = 1; i <= 100; i++) {
          var data = ZeroUtility.newZeroMap().putString(SharedEventKey.KEY_CLIENT_SERVER_ECHO,
              String.format("Hello from client %d", i));
          var request = ServerMessage.newInstance().setData(data);
          udp.send(request);

          System.out.println("[SENT TO SERVER " + i + "] -> " + request);

          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

        tcp.close();
        udp.close();
      }
      break;
    }
  }

  @Override
  public void onReceivedUDP(byte[] binary) {
    var data = ZeroUtility.binaryToMap(binary);
    var message = ServerMessage.newInstance().setData(data);
    System.err.println("[RECV FROM SERVER UDP] -> " + message);
  }
}
