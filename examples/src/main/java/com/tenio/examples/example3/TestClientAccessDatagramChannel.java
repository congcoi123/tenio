/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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
public final class TestClientAccessDatagramChannel implements SocketListener, DatagramListener {

  private static final int SOCKET_PORT = 8032;
  private final TCP tcp;
  private final String playerName;
  private UDP udp;
  private int udpConvey;

  public TestClientAccessDatagramChannel() {
    playerName = ClientUtility.generateRandomString(5);

    // create a new TCP object and listen for this port
    tcp = new TCP(SOCKET_PORT);
    tcp.receive(this);

    // send a login request
    var request =
        DataUtility.newZeroMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
    tcp.send(request);

    System.out.println("Login Request -> " + request);
  }

  /**
   * The entry point.
   */
  public static void main(String[] args) {
    new TestClientAccessDatagramChannel();
  }

  @Override
  public void onReceivedTCP(byte[] binaries) {
    var parcel = (ZeroMap) DataUtility.binaryToCollection(DataType.ZERO, binaries);

    System.err.println("[RECV FROM SERVER TCP] -> " + parcel);
    ZeroArray udpParcel = parcel.getZeroArray(SharedEventKey.KEY_ALLOW_TO_ACCESS_UDP_CHANNEL);

    switch (udpParcel.getByte(0)) {
      case UdpEstablishedState.ALLOW_TO_ACCESS -> {
        // now you can send request for UDP connection request
        var udpMessageData = DataUtility.newZeroMap();
        udpMessageData.putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
        var request =
            DataUtility.newZeroMap().putZeroMap(SharedEventKey.KEY_UDP_MESSAGE_DATA,
                udpMessageData);
        // create a new UDP object and listen for this port
        udp = new UDP(udpParcel.getInteger(1));
        udp.receive(this);
        udp.send(request);

        System.out.println("Request a UDP connection -> " + request);
      }
      case UdpEstablishedState.ESTABLISHED -> {
        udpConvey = udpParcel.getInteger(1);
        var udpMessageData = DataUtility.newZeroMap();
        udpMessageData.putByte(SharedEventKey.KEY_COMMAND, UdpEstablishedState.ESTABLISHED);
        var request = DataUtility.newZeroMap();
        request.putInteger(SharedEventKey.KEY_UDP_CONVEY_ID, udpConvey);
        request.putZeroMap(SharedEventKey.KEY_UDP_MESSAGE_DATA, udpMessageData);
        udp.send(request);
      }
      case UdpEstablishedState.COMMUNICATING -> {
        // the UDP connected successful, you now can send test requests
        System.out.println("Start the conversation ...");

        for (int i = 1; i <= 100; i++) {
          var udpMessageData = DataUtility.newZeroMap();
          udpMessageData.putByte(SharedEventKey.KEY_COMMAND, UdpEstablishedState.COMMUNICATING);
          udpMessageData.putString(SharedEventKey.KEY_CLIENT_SERVER_ECHO,
              String.format("Hello from client %d", i));
          var request = DataUtility.newZeroMap();
          request.putInteger(SharedEventKey.KEY_UDP_CONVEY_ID, udpConvey);
          request.putZeroMap(SharedEventKey.KEY_UDP_MESSAGE_DATA, udpMessageData);
          udp.send(request);

          System.out.println("[SENT TO SERVER " + i + "] -> " + request);

          try {
            Thread.sleep(1000);
          } catch (InterruptedException exception) {
            exception.printStackTrace();
          }
        }

        tcp.close();
        udp.close();
      }
    }
  }

  @Override
  public void onReceivedUDP(byte[] binary) {
    var parcel = DataUtility.binaryToCollection(DataType.ZERO, binary);
    System.err.println("[RECV FROM SERVER UDP] -> " + parcel);
  }
}
