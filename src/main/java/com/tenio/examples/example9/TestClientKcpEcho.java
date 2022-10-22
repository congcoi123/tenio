/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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
import com.tenio.core.configuration.kcp.KcpConfiguration;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.network.entity.kcp.Ukcp;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.handler.KcpIoHandler;
import com.tenio.examples.client.ClientUtility;
import com.tenio.examples.client.DatagramListener;
import com.tenio.examples.client.KcpWriterHandler;
import com.tenio.examples.client.SocketListener;
import com.tenio.examples.client.TCP;
import com.tenio.examples.client.UDP;
import com.tenio.examples.server.SharedEventKey;
import com.tenio.examples.server.UdpEstablishedState;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
public final class TestClientKcpEcho implements SocketListener, DatagramListener, KcpIoHandler {

  private static final int SOCKET_PORT = 8032;
  private final TCP tcp;
  private final Session session;
  private final String playerName;
  private final NetworkWriterStatistic networkWriterStatistic;
  private UDP udp;

  public TestClientKcpEcho() {
    playerName = ClientUtility.generateRandomString(5);

    // create a new TCP object and listen for this port
    tcp = new TCP(SOCKET_PORT);
    tcp.receive(this);
    session = tcp.getSession();
    session.setName(playerName);

    networkWriterStatistic = NetworkWriterStatistic.newInstance();

    // send a login request
    var data =
        DataUtility.newZeroMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
    var message = ServerMessage.newInstance().setData(data);
    tcp.send(message);

    System.out.println("Login Request -> " + message);
  }

  /**
   * The entry point.
   */
  public static void main(String[] args) {
    new TestClientKcpEcho();
  }

  @Override
  public void sessionRead(Session session, byte[] binary) {
    System.out.println("[KCP SESSION READ] " + DataUtility.binaryToCollection(DataType.ZERO,
        binary));
  }

  @Override
  public void sessionException(Session session, Exception exception) {
    System.out.println("[KCP EXCEPTION] " + exception.getMessage());
  }

  @Override
  public void setSessionManager(SessionManager sessionManager) {
    // do nothing
  }

  @Override
  public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
    // do nothing
  }

  @Override
  public void setDataType(DataType dataType) {
    // do nothing
  }

  @Override
  public void channelActiveIn(Session session) {
    System.out.println("[KCP ACTIVATED] " + session.toString());
  }

  @Override
  public void channelInactiveIn(Session session) {
    System.out.println("[KCP INACTIVATED] " + session.toString());
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
            DataUtility.newZeroMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
        var request = ServerMessage.newInstance().setData(data);
        // create a new UDP object and listen for this port
        udp = new UDP(pack.getInteger(1));
        udp.receive(this);
        udp.send(request);

        System.out.println(
            udp.getLocalAddress().getHostAddress() + ", " + udp.getLocalPort() + " Request a UDP " +
                "connection -> " + request);
      }
      break;

      case UdpEstablishedState.ATTACHED: {
        // the UDP connected successful, you now can send test requests
        System.out.println("Start the conversation ...");

        var ukcp = initializeKcp(pack.getInteger(1));
        kcpProcessing();

        for (int i = 1; i <= 100; i++) {
          var data = DataUtility.newZeroMap().putString(SharedEventKey.KEY_CLIENT_SERVER_ECHO,
              String.format("Hello from client %d", i));
          ukcp.send(data.toBinary());

          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

        session.setUkcp(null);
        udp.close();
        tcp.close();
      }
      break;
    }
  }

  private Ukcp initializeKcp(int conv) {
    var kcpWriter = new KcpWriterHandler(udp.getDatagramSocket(),
        udp.getLocalAddress(), udp.getRemotePort());
    var ukcp =
        new Ukcp(conv, KcpConfiguration.PROFILE, session, this, kcpWriter, networkWriterStatistic);
    ukcp.getKcpIoHandler().channelActiveIn(session);

    return ukcp;
  }

  private void kcpProcessing() {
    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
        () -> {
          if (session.containsKcp()) {
            var ukcp = session.getUkcp();
            ukcp.update();
            ukcp.receive();
          }
        }, 0, 10, TimeUnit.MILLISECONDS);
  }

  @Override
  public void onReceivedUDP(byte[] binary) {
    session.getUkcp().input(binary);
  }
}
