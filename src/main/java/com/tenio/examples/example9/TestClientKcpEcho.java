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

package com.tenio.examples.example9;

import com.tenio.common.data.DataType;
import com.tenio.common.data.DataUtility;
import com.tenio.common.data.zero.ZeroArray;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.core.network.entity.session.Session;
import com.tenio.examples.client.ClientUtility;
import com.tenio.examples.client.SocketListener;
import com.tenio.examples.client.TCP;
import com.tenio.examples.server.DatagramEstablishedState;
import com.tenio.examples.server.SharedEventKey;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetSocketAddress;
import kcp.ChannelConfig;
import kcp.KcpClient;
import kcp.KcpListener;
import kcp.Ukcp;

/**
 * This class shows how a client communicates with the server:<br>
 * 1. Create connections.<br>
 * 2. Send a login request.<br>
 * 3. Receive a response for login success and send a KCP connection
 * request.<br>
 * 4. Receive a response for allowed KCP connection.<br>
 * 5. Send messages via KCP connection and get these echoes from the server.<br>
 * 6. Close connections.
 */
public final class TestClientKcpEcho implements SocketListener<ZeroMap>, KcpListener {

  private static final int SOCKET_PORT = 8032;
  private final TCP tcp;
  private final String playerName;
  private Ukcp ukcp;

  public TestClientKcpEcho() {
    playerName = ClientUtility.generateRandomString(5);

    // create a new TCP object and listen to this port
    tcp = new TCP(SOCKET_PORT, it -> {
      it.receive(TestClientKcpEcho.this);
      Session session = it.getSession();
      session.setName(playerName);

      // send a login request
      var request =
          DataUtility.newZeroMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
      it.send(request);

      System.out.println("Login Request -> " + request);
    });
  }

  /**
   * The entry point.
   */
  public static void main(String[] args) {
    new TestClientKcpEcho();
  }

  @Override
  public void onReceivedTCP(ZeroMap parcel) {
    System.err.println("[RECV FROM SERVER TCP] -> " + parcel);
    ZeroArray udpParcel = parcel.getZeroArray(SharedEventKey.KEY_ALLOW_TO_ACCESS_KCP_CHANNEL);

    switch (udpParcel.getByte(0)) {
      case DatagramEstablishedState.ALLOW_TO_ACCESS -> {
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setSndwnd(512);
        channelConfig.setRcvwnd(512);
        channelConfig.setMtu(512);
        channelConfig.setAckNoDelay(true);
        channelConfig.setConv(55);
        channelConfig.setCrc32Check(true);
        KcpClient kcpClient = new KcpClient();
        kcpClient.init(channelConfig);

        kcpClient.connect(new InetSocketAddress("localhost", 20003), channelConfig, this);
      }
      case DatagramEstablishedState.COMMUNICATING -> {
        // the KCP connected successful, you now can send test requests
        System.out.println("Start the conversation ...");

        if (ukcp == null) {
          System.err.println("[ERROR] No KCP instance found!");
          return;
        }

        for (int i = 1; i <= 100; i++) {
          var request = DataUtility.newZeroMap();
          request.putByte(SharedEventKey.KEY_COMMAND, DatagramEstablishedState.COMMUNICATING);
          request.putString(SharedEventKey.KEY_CLIENT_SERVER_ECHO, String.format("Hello from client %d", i));
          ByteBuf byteBuf = Unpooled.wrappedBuffer(request.toBinaries());
          ukcp.write(byteBuf);
          byteBuf.release();

          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

        ukcp.close();
        tcp.close();
      }
    }
  }

  @Override
  public void onConnected(Ukcp ukcp) {
    this.ukcp = ukcp;
    System.out.println("[KCP ESTABLISHED] " + this.ukcp);

    // now you can send request via the KCP channel
    var request = DataUtility.newZeroMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
    ByteBuf byteBuf = Unpooled.wrappedBuffer(request.toBinaries());
    ukcp.write(byteBuf);
    byteBuf.release();

    System.out.println("Request a KCP connection -> " + request);
  }

  @Override
  public void handleReceive(ByteBuf byteBuf, Ukcp ukcp) {
    var binary = new byte[byteBuf.readableBytes()];
    byteBuf.getBytes(byteBuf.readerIndex(), binary);

    var message = DataUtility.binariesToCollection(DataType.ZERO, binary);

    System.out.println("[KCP RECEIVE] " + message);
  }

  @Override
  public void handleException(Throwable throwable, Ukcp ukcp) {
    // Do nothing
  }

  @Override
  public void handleClose(Ukcp ukcp) {
    // Do nothing
  }
}
