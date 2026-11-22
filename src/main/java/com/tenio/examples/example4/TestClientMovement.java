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

package com.tenio.examples.example4;

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.DataUtility;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.utility.TimeUtility;
import com.tenio.examples.client.ClientUtility;
import com.tenio.examples.client.DatagramListener;
import com.tenio.examples.client.SocketListener;
import com.tenio.examples.client.TCP;
import com.tenio.examples.client.UDP;
import com.tenio.examples.example4.constant.Example4Constant;
import com.tenio.examples.example4.statistic.LocalCounter;
import com.tenio.examples.example4.statistic.NetworkStatistic;
import com.tenio.examples.server.DatagramEstablishedState;
import com.tenio.examples.server.SharedEventKey;
import com.tenio.examples.client.ClientTasks;
import java.io.IOException;
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
    implements SocketListener<ZeroMap>, DatagramListener<ZeroMap> {

  private static final boolean LOGGER_DEBUG = false;
  private static final NetworkStatistic statistic = NetworkStatistic.newInstance();
  private static final long START_EXECUTION_TIME = TimeUtility.currentTimeSeconds();
  private final String playerName;
  private final LocalCounter localCounter;
  private volatile UDP udp;
  private final ClientTasks tasks = new ClientTasks();
  private final TCP tcp;
  private boolean measurementsStarted;
  private int udpConvey;
  private volatile long sentTimestamp;

  public TestClientMovement(String playerName) throws IOException {
    this.playerName = playerName;
    localCounter = LocalCounter.newInstance();

    tcp = new TCP("localhost", Example4Constant.SOCKET_PORT, 5000);
    tcp.handleDisconnection(() -> {
      tasks.close();
      if (udp != null) {
        udp.close();
      }
    });
    sendLoginRequest(tcp);
    tcp.receive(this);
  }

  public static void main(String[] args) throws IOException {

    var summaryTasks = new ClientTasks();
    Runtime.getRuntime().addShutdownHook(new Thread(summaryTasks::close));

    // average measurement
    summaryTasks.repeat(() -> {

      logExecutionTime();
      logLatencyAverage();
      logFpsAverage();
      logLostPacketsAverage();

    }, 0, Example4Constant.AVERAGE_LATENCY_MEASUREMENT_INTERVAL, TimeUnit.MINUTES);

    // create clients
    for (int i = 0; i < Example4Constant.NUMBER_OF_PLAYERS; i++) {
      new TestClientMovement(String.valueOf(i));
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

  private void sendLoginRequest(TCP tcp) {
    var request = DataUtility.newZeroMap();
    request.putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
    tcp.send(request);

    if (LOGGER_DEBUG) {
      System.err.println("Login Request -> " + request);
    }
  }

  @Override
  public void onReceivedTCP(ZeroMap parcel) {
    if (LOGGER_DEBUG) {
      System.err.println("[RECV FROM SERVER TCP] -> " + parcel);
    }

    if (parcel.containsKey(SharedEventKey.KEY_ALLOW_TO_ACCESS_UDP_CHANNEL)) {
      var accessingPhaseData = parcel.getZeroArray(SharedEventKey.KEY_ALLOW_TO_ACCESS_UDP_CHANNEL);
      switch (accessingPhaseData.getByte(0)) {
        case DatagramEstablishedState.ALLOW_TO_ACCESS -> {
          // create a new UDP object and listen to this port
          if (udp != null) {
            return;
          }
          try {
            udp = new UDP("localhost", accessingPhaseData.getInteger(1));
            udp.receive(this);
            var body = DataUtility.newZeroMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, playerName);
            udp.send(DataUtility.newZeroMap().putZeroMap(SharedEventKey.KEY_UDP_MESSAGE_DATA, body));
          } catch (IOException exception) {
            tcp.close();
            throw new java.io.UncheckedIOException(exception);
          }
        }
        case DatagramEstablishedState.ESTABLISHED -> {
          udpConvey = accessingPhaseData.getInteger(1);
          var udpMessageData = DataUtility.newZeroMap();
          udpMessageData.putByte(SharedEventKey.KEY_COMMAND, DatagramEstablishedState.ESTABLISHED);
          var request = DataUtility.newZeroMap();
          request.putInteger(SharedEventKey.KEY_UDP_CONVEY_ID, udpConvey);
          request.putZeroMap(SharedEventKey.KEY_UDP_MESSAGE_DATA, udpMessageData);
          udp.send(request);
        }
        case DatagramEstablishedState.COMMUNICATING -> {
          if (measurementsStarted) {
            return;
          }
          measurementsStarted = true;
          // the UDP connected successful, you now can send test requests
          System.out.println(playerName + " started the conversation -> " + parcel);

          // packets counting
          tasks.repeat(() -> {

            var snapshot = localCounter.drain();
            double lostPacket = Math.max(0, 100.0 *
                (Example4Constant.ONE_MINUTE_EXPECT_RECEIVE_TOTAL_PACKETS - snapshot.packets()) /
                Example4Constant.ONE_MINUTE_EXPECT_RECEIVE_TOTAL_PACKETS);
            statistic.addLostPackets(lostPacket);
            if (isInfoEnabled()) {
              info("COUNTING", String.format("Player %s -> Packets: %d, Loss: %.2f %%, Data: %.2f KB",
                  playerName, snapshot.packets(), lostPacket, snapshot.bytes() / 1000.0));
            }
          }, 1, 1, TimeUnit.MINUTES);

          // send requests to calculate latency
          tasks.repeat(() -> {

                sentTimestamp = System.nanoTime();

                requestNeighbours();

              }, Example4Constant.SEND_MEASUREMENT_REQUEST_INTERVAL,
              Example4Constant.SEND_MEASUREMENT_REQUEST_INTERVAL, TimeUnit.SECONDS);
        }
      }
    } else if (parcel.containsKey(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS)) {
      int fps = parcel.getInteger(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS);
      long latency = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - sentTimestamp);

      statistic.addLatency(latency);
      statistic.addFps(fps);

      if (isInfoEnabled()) {
        info("LATENCY", buildgen("Player ", playerName, " -> ", latency, " ms | fps -> ", fps));
      }
    }
  }

  private void requestNeighbours() {
    var udpMessageData = DataUtility.newZeroMap();
    udpMessageData.putByte(SharedEventKey.KEY_COMMAND, DatagramEstablishedState.COMMUNICATING);
    udpMessageData.putString(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS,
        ClientUtility.generateRandomString(10));
    var request = DataUtility.newZeroMap();
    request.putInteger(SharedEventKey.KEY_UDP_CONVEY_ID, udpConvey);
    request.putZeroMap(SharedEventKey.KEY_UDP_MESSAGE_DATA, udpMessageData);
    udp.send(request);
  }

  @Override
  public void onReceivedUDP(ZeroMap parcel) {
    if (LOGGER_DEBUG) {
      System.err.println("[RECV FROM SERVER UDP] -> " + parcel);
    }

    counting(parcel);
  }

  private void counting(DataCollection message) {
    localCounter.record(message.toBinaries().length);
  }
}
