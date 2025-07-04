/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.examples.example10;

import com.tenio.common.data.DataType;
import com.tenio.common.data.DataUtility;
import com.tenio.common.data.msgpack.element.MsgPackMap;
import com.tenio.core.network.entity.session.Session;
import com.tenio.examples.client.ClientUtility;
import com.tenio.examples.client.DatagramListener;
import com.tenio.examples.client.SocketListener;
import com.tenio.examples.client.TCP;
import com.tenio.examples.client.UDP;
import com.tenio.examples.server.DatagramEstablishedState;
import com.tenio.examples.server.SharedEventKey;

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
public final class TestClientMsgPackEcho implements SocketListener, DatagramListener {

  private static final int SOCKET_PORT = 8032;
  private final String playerName;

  public TestClientMsgPackEcho() {
    playerName = ClientUtility.generateRandomString(5);

    // create a new TCP object and listen for this port
    TCP tcp = new TCP(SOCKET_PORT);
    tcp.receive(this);
    Session session = tcp.getSession();
    session.setName(playerName);

    // send a login request
    var request =
        DataUtility.newMsgMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
    tcp.send(request);

    System.out.println("Login Request -> " + request);
  }

  /**
   * The entry point.
   */
  public static void main(String[] args) {
    new TestClientMsgPackEcho();
  }

  @Override
  public void onReceivedTCP(byte[] binaries) {
    var parcel = (MsgPackMap) DataUtility.binaryToCollection(DataType.MSG_PACK, binaries);

    System.err.println("[RECV FROM SERVER TCP] -> " + parcel);
    var pack = parcel.getIntegerArray(SharedEventKey.KEY_ALLOW_TO_ACCESS_UDP_CHANNEL);

    switch (pack[0]) {
      case DatagramEstablishedState.ALLOW_TO_ACCESS -> {
        // now you can send request for UDP connection request
        var request =
            DataUtility.newMsgMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
        // create a new UDP object and listen for this port
        UDP udp = new UDP(pack[1]);
        udp.receive(this);
        udp.send(request);

        System.out.println(
            udp.getLocalAddress().getHostAddress() + ", " + udp.getLocalPort() + " Request a UDP " +
                "connection -> " + request);
      }
      case DatagramEstablishedState.ESTABLISHED -> {
      }
    }
  }

  @Override
  public void onReceivedUDP(byte[] binary) {
  }
}
