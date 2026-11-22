/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

import com.tenio.common.data.DataUtility;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.examples.client.ClientUtility;
import com.tenio.examples.client.SocketListener;
import com.tenio.examples.client.TCP;
import com.tenio.examples.server.SharedEventKey;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
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
public final class TestClientEchoStress implements SocketListener<ZeroMap> {

  private static final int SOCKET_PORT = 8032;
  private static final int NUMBER_CLIENTS = Integer.getInteger("tenio.clients", 10000);
  private static final int REQUEST_INTERVAL_MS = 1;
  private static final boolean ENABLED_DEBUG = false;
  /**
   * List of TCP clients
   */
  private final Map<String, TCP> tcps;

  public TestClientEchoStress() throws IOException {
    tcps = new ConcurrentHashMap<>();
    try {
      for (int i = 0; i < NUMBER_CLIENTS; i++) {
        String name = "stress-" + i;
        var tcp = new TCP("localhost", SOCKET_PORT, 5000);
        tcps.put(name, tcp);
        tcp.handleDisconnection(() -> tcps.remove(name, tcp));
        tcp.send(DataUtility.newZeroMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, name));
        tcp.receive(this);
      }
    } catch (IOException | RuntimeException exception) {
      tcps.values().forEach(TCP::close);
      throw exception;
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> tcps.values().forEach(TCP::close)));
  }

  /**
   * The entry point
   */
  public static void main(String[] args) throws IOException {
    new TestClientEchoStress();
  }

  @Override
  public void onReceivedTCP(ZeroMap parcel) {
    if (ENABLED_DEBUG) {
      System.out.println("[RECV FROM SERVER TCP] -> " + parcel);
    }

    try {
      Thread.sleep(REQUEST_INTERVAL_MS);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return;
    }

    var tcp = tcps.get(parcel.getString(SharedEventKey.KEY_PLAYER_LOGIN));

    if (tcp == null || tcp.isClosed()) {
      return;
    }

    // make an echo message
    var request = DataUtility.newZeroMap();
    request.putString(SharedEventKey.KEY_CLIENT_SERVER_ECHO, "Hello from client");
    tcp.send(request);
  }
}
