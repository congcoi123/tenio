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

package com.tenio.examples.example6;

import com.tenio.common.data.DataType;
import com.tenio.common.data.DataUtility;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.data.zero.utility.ZeroUtility;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.examples.client.ClientUtility;
import com.tenio.examples.client.SocketListener;
import com.tenio.examples.client.TCP;
import com.tenio.examples.server.SharedEventKey;
import java.util.HashMap;
import java.util.Map;

/**
 * This class shows how a client communicates with the server:<br>
 * 1. Create connections.<br>
 * 2. Send a login request.<br>
 * 3. Receive messages via TCP connection from the server.<br>
 * 4. Be logout by server.
 * <p>
 * [NOTE] The client test is also available on <code>C++</code> and
 * <code>JavaScript</code> language, please see the <code>README.md</code> for
 * more details
 */
public final class TestClientEchoStress implements SocketListener {

  private static final int SOCKET_PORT = 8032;
  private static final int NUMBER_CLIENTS = 1000;
  private static final boolean ENABLED_DEBUG = false;
  /**
   * List of TCP clients
   */
  private final Map<String, TCP> tcps;

  public TestClientEchoStress() {
    tcps = new HashMap<>();
    // create a list of TCP objects and listen for this port
    for (int i = 0; i < NUMBER_CLIENTS; i++) {
      var name = ClientUtility.generateRandomString(5);
      var tcp = new TCP(SOCKET_PORT);
      tcp.receive(this);
      tcps.put(name, tcp);

      // send a login request
      var data = ZeroUtility.newZeroMap();
      data.putString(SharedEventKey.KEY_PLAYER_LOGIN, name);
      tcp.send(ServerMessage.newInstance().setData(data));

      if (ENABLED_DEBUG) {
        System.err.println("Login Request -> " + data);
      }
    }
  }

  /**
   * The entry point
   */
  public static void main(String[] args) {
    new TestClientEchoStress();
  }

  @Override
  public void onReceivedTCP(byte[] binaries) {
    var dat = DataUtility.binaryToCollection(DataType.ZERO, binaries);
    var message = ServerMessage.newInstance().setData(dat);

    if (ENABLED_DEBUG) {
      System.out.println("[RECV FROM SERVER TCP] -> " + message);
    }

    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    var tcp =
        tcps.get(((ZeroMap) message.getData()).getString(SharedEventKey.KEY_PLAYER_LOGIN));

    // make an echo message
    var data = ZeroUtility.newZeroMap();
    data.putString(SharedEventKey.KEY_CLIENT_SERVER_ECHO, "Hello from client");
    var request = ServerMessage.newInstance().setData(data);
    tcp.send(request);
  }
}
