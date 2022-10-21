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

package com.tenio.examples.example4;

import com.tenio.common.data.DataType;
import com.tenio.common.data.DataUtility;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.data.zero.utility.ZeroUtility;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.utility.TimeUtility;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.examples.client.ClientUtility;
import com.tenio.examples.client.DatagramListener;
import com.tenio.examples.client.SocketListener;
import com.tenio.examples.client.TCP;
import com.tenio.examples.client.UDP;
import com.tenio.examples.example4.constant.Example4Constant;
import com.tenio.examples.example4.statistic.LocalCounter;
import com.tenio.examples.example4.statistic.NetworkStatistic;
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
 */
public final class TestClientMovement extends AbstractLogger
    implements SocketListener, DatagramListener {

  private static final boolean LOGGER_DEBUG = false;
  private static final NetworkStatistic statistic = NetworkStatistic.newInstance();
  private static final long START_EXECUTION_TIME = TimeUtility.currentTimeSeconds();
  private final TCP tcp;
  private final String playerName;
  private final LocalCounter localCounter;
  private volatile long sentTimestamp;

  public TestClientMovement(String playerName) {
    this.playerName = playerName;
    localCounter = LocalCounter.newInstance();

    // create a new TCP object and listen for this port
    tcp = new TCP(Example4Constant.SOCKET_PORT);
    tcp.receive(this);

    // send a login request
    sendLoginRequest();
  }

  public static void main(String[] args) throws InterruptedException {

    // average measurement
    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

      logExecutionTime();
      logLatencyAverage();
      logFpsAverage();
      logLostPacketsAverage();

    }, 0, Example4Constant.AVERAGE_LATENCY_MEASUREMENT_INTERVAL, TimeUnit.MINUTES);

    // create clients
    for (int i = 0; i < Example4Constant.NUMBER_OF_PLAYERS; i++) {

      new TestClientMovement(String.valueOf(i));

      Thread.sleep((long) (Example4Constant.DELAY_CREATION * 1000));
    }

  }

  private static void logExecutionTime() {
    System.out.printf("[EXECUTION TIME -> CCU: %d] %s%n", Example4Constant.NUMBER_OF_PLAYERS,
        ClientUtility.getTimeFormat(TimeUtility.currentTimeSeconds() - START_EXECUTION_TIME));
  }

  private static void logLatencyAverage() {
    System.err.printf("[AVERAGE LATENCY] Total Requests: %d -> Average Latency: %.2f ms%n",
        statistic.getLatencySize(), statistic.getLatencyAverage());
  }

  private static void logFpsAverage() {
    System.err.printf("[AVERAGE FPS] Total Requests: %d -> Average FPS: %.2f%n",
        statistic.getFpsSize(), statistic.getFpsAverage());
  }

  private static void logLostPacketsAverage() {
    System.err.printf("[AVERAGE LOST PACKETS] Average Lost Packets: %.2f %%%n",
        statistic.getLostPacketsAverage());
  }

  private void sendLoginRequest() {
    var data = ZeroUtility.newZeroMap();
    data.putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
    tcp.send(ServerMessage.newInstance().setData(data));

    if (LOGGER_DEBUG) {
      System.err.println("Login Request -> " + data);
    }
  }

  @Override
  public void onReceivedTCP(byte[] binaries) {
    var dat = DataUtility.binaryToCollection(DataType.ZERO, binaries);
    var message = ServerMessage.newInstance().setData(dat);

    if (LOGGER_DEBUG) {
      System.err.println("[RECV FROM SERVER TCP] -> " + message);
    }

    var data = (ZeroMap) message.getData();
    if (data.containsKey(SharedEventKey.KEY_ALLOW_TO_ATTACH)) {
      switch (data.getByte(SharedEventKey.KEY_ALLOW_TO_ATTACH)) {
        case UdpEstablishedState.ALLOW_TO_ATTACH: {
          // create a new UDP object and listen for this port
          var udp = new UDP(data.getInteger(SharedEventKey.KEY_ALLOW_TO_ATTACH_PORT));
          udp.receive(this);
          System.out.println(playerName + " connected to UDP port: " +
              data.getInteger(SharedEventKey.KEY_ALLOW_TO_ATTACH_PORT));

          // now you can send request for UDP connection request
          var sendData =
              ZeroUtility.newZeroMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
          var request = ServerMessage.newInstance().setData(sendData);
          udp.send(request);

          if (LOGGER_DEBUG) {
            System.out.println(playerName + " requests a UDP connection -> " + request);
          }
        }
        break;

        case UdpEstablishedState.ATTACHED: {
          // the UDP connected successful, you now can send test requests
          System.out.println(playerName + " started the conversation -> " + message);

          // packets counting
          Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

            int countPackets = localCounter.getCountUdpPacketsOneMinute();
            double lostPacket =
                ((double) (Example4Constant.ONE_MINUTE_EXPECT_RECEIVE_PACKETS - countPackets)
                    / (double) Example4Constant.ONE_MINUTE_EXPECT_RECEIVE_PACKETS) * (double) 100;

            statistic.addLostPackets(lostPacket);
            logLostPacket(lostPacket);

            localCounter.setCountUdpPacketsOneMinute(0);
            localCounter.setCountReceivedPacketSizeOneMinute(0);

          }, 1, 1, TimeUnit.MINUTES);

          // send requests to calculate latency
          Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

                sentTimestamp = TimeUtility.currentTimeMillis();

                requestNeighbours();

              }, Example4Constant.SEND_MEASUREMENT_REQUEST_INTERVAL,
              Example4Constant.SEND_MEASUREMENT_REQUEST_INTERVAL, TimeUnit.SECONDS);
        }
        break;

      }
    } else if (data.containsKey(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS)) {
      int fps = data.getInteger(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS);
      long receivedTimestamp = TimeUtility.currentTimeMillis();
      long latency = receivedTimestamp - sentTimestamp;

      statistic.addLatency(latency);
      statistic.addFps(fps);

      info("LATENCY", buildgen("Player ", playerName, " -> ", latency, " ms | fps -> ", fps));
    }

  }

  private void logLostPacket(double lostPacket) {
    info("COUNTING",
        String.format("Player %s -> Packet Count: %d (Loss: %.2f %%) -> Received Data: %.2f KB",
            playerName,
            localCounter.getCountUdpPacketsOneMinute(), lostPacket,
            (float) localCounter.getCountReceivedPacketSizeOneMinute() / 1000.0f));
  }

  private void requestNeighbours() {
    var data = ZeroUtility.newZeroMap().putString(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS,
        ClientUtility.generateRandomString(10));
    var request = ServerMessage.newInstance().setData(data);
    tcp.send(request);
  }

  @Override
  public void onReceivedUDP(byte[] binary) {
    var data = ZeroUtility.binaryToMap(binary);
    var message = ServerMessage.newInstance().setData(data);

    if (LOGGER_DEBUG) {
      System.err.println("[RECV FROM SERVER UDP] -> " + message);
    }

    counting(message);
  }

  private void counting(ServerMessage message) {
    localCounter.addCountUdpPacketsOneMinute();
    localCounter.addCountReceivedPacketSizeOneMinute((message.getData().toBinary().length));
  }
}
