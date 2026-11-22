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

package com.tenio.examples.example10;

import com.tenio.common.data.DataUtility;
import com.tenio.common.data.msgpack.element.MsgPackMap;
import com.tenio.examples.client.ClientTasks;
import com.tenio.examples.client.ClientUtility;
import com.tenio.examples.client.DatagramListener;
import com.tenio.examples.client.SocketListener;
import com.tenio.examples.client.TCP;
import com.tenio.examples.client.UDP;
import com.tenio.examples.server.DatagramEstablishedState;
import com.tenio.examples.server.SharedEventKey;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
public final class TestClientMsgPackEcho implements SocketListener<MsgPackMap>, DatagramListener<MsgPackMap> {

  private final String playerName = ClientUtility.generateRandomString(5);
  private final TCP tcp;
  private final ClientTasks tasks = new ClientTasks();
  private volatile UDP udp;
  private int udpConvey;
  private int sent;
  private final AtomicInteger received = new AtomicInteger();

  public TestClientMsgPackEcho() throws IOException {
    tcp = new TCP("localhost", 8032, 5000);
    tcp.handleDisconnection(() -> {
      tasks.close();
      if (udp != null) {
        udp.close();
      }
    });
    tcp.send(DataUtility.newMsgMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName));
    tcp.receive(this);
    tasks.later(tcp::close, 15000); // Bound failed registration/lost-packet runs.
  }

  public static void main(String[] args) throws IOException {
    new TestClientMsgPackEcho();
  }

  @Override
  public void onReceivedTCP(MsgPackMap parcel) {
    System.out.println("[TCP] " + parcel);
    if (!parcel.containsKey(SharedEventKey.KEY_ALLOW_TO_ACCESS_UDP_CHANNEL)) {
      return;
    }
    var state = parcel.getIntegerArray(SharedEventKey.KEY_ALLOW_TO_ACCESS_UDP_CHANNEL);
    switch (state[0]) {
      case DatagramEstablishedState.ALLOW_TO_ACCESS -> {
        if (udp != null) {
          return;
        }
        try {
          udp = new UDP("localhost", state[1]);
          udp.receive(this);
          udp.send(DataUtility.newMsgMap().putMsgPackMap(SharedEventKey.KEY_UDP_MESSAGE_DATA,
              DataUtility.newMsgMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName)));
        } catch (IOException exception) {
          tcp.close();
          throw new UncheckedIOException(exception);
        }
      }
      case DatagramEstablishedState.ESTABLISHED -> {
        udpConvey = state[1];
        tasks.repeat(() -> {
          if (++sent <= 5) {
            var body = DataUtility.newMsgMap()
                .putString(SharedEventKey.KEY_CLIENT_SERVER_ECHO, "MsgPack echo " + sent);
            udp.send(DataUtility.newMsgMap()
                .putInteger(SharedEventKey.KEY_UDP_CONVEY_ID, udpConvey)
                .putMsgPackMap(SharedEventKey.KEY_UDP_MESSAGE_DATA, body));
          }
        }, 0, 1, TimeUnit.SECONDS);
      }
    }
  }

  @Override
  public void onReceivedUDP(MsgPackMap parcel) {
    System.out.println("[UDP] " + parcel);
    if (received.incrementAndGet() >= 5) {
      tcp.close();
    }
  }
}
