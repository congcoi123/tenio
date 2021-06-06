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

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tenio.common.data.ZeroObject;
import com.tenio.common.data.implement.ZeroObjectImpl;
import com.tenio.common.loggers.AbstractLogger;
import com.tenio.common.utilities.TimeUtility;
import com.tenio.core.entities.data.ServerMessage;
import com.tenio.examples.client.ClientUtility;
import com.tenio.examples.client.DatagramListener;
import com.tenio.examples.client.SocketListener;
import com.tenio.examples.client.TCP;
import com.tenio.examples.client.UDP;
import com.tenio.examples.example4.constant.Example4Constant;
import com.tenio.examples.example4.statistics.LocalCounter;
import com.tenio.examples.example4.statistics.NetworkStatistics;
import com.tenio.examples.server.SharedEventKey;
import com.tenio.examples.server.UdpEstablishedState;

/**
 * This class shows how a client communicates with the server:<br>
 * 1. Create connections.<br>
 * 2. Send a login request.<br>
 * 3. Receive a response for login success and send a UDP connection
 * request.<br>
 * 4. Receive a response for allowed UDP connection.<br>
 */
public final class TestClientMovement extends AbstractLogger implements SocketListener, DatagramListener {

	private static final boolean LOGGER_DEBUG = false;
	private static final NetworkStatistics __statistics = NetworkStatistics.newInstance();
	private static final long START_EXECUTION_TIME = TimeUtility.currentTimeSeconds();

	public static void main(String[] args) throws InterruptedException {

		// average measurement
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

			__logExecutionTime();
			__logLatencyAverage();
			__logFpsAverage();
			__logLostPacketsAverage();

		}, 0, Example4Constant.AVERAGE_LATENCY_MEASUREMENT_INTERVAL, TimeUnit.MINUTES);

		// create clients
		for (int i = 0; i < Example4Constant.NUMBER_OF_PLAYERS; i++) {

			new TestClientMovement(String.valueOf(i));

			if (Example4Constant.DELAY_CREATION != -1) {
				Thread.sleep((long) (Example4Constant.DELAY_CREATION * 1000));
			}
		}

	}

	private static void __logExecutionTime() {
		System.out.println(String.format("[EXECUTION TIME -> CCU: %d] %s", Example4Constant.NUMBER_OF_PLAYERS,
				ClientUtility.getTimeFormat(TimeUtility.currentTimeSeconds() - START_EXECUTION_TIME)));
	}

	private static void __logLatencyAverage() {
		System.err.println(String.format("[AVERAGE LATENCY] Total Requests: %d -> Average Latency: %.2f ms",
				__statistics.getLatencySize(), __statistics.getLatencyAverage()));
	}

	private static void __logFpsAverage() {
		System.err.println(String.format("[AVERAGE FPS] Total Requests: %d -> Average FPS: %.2f",
				__statistics.getFpsSize(), __statistics.getFpsAverage()));
	}

	private static void __logLostPacketsAverage() {
		System.err.println(String.format("[AVERAGE LOST PACKETS] Average Lost Packets: %.2f %%",
				__statistics.getLostPacketsAverage()));
	}

	private final TCP __tcp;
	private final UDP __udp;
	private final String __playerName;
	private final LocalCounter __counter;
	private volatile long __sentTimestamp;

	public TestClientMovement(String playerName) {
		__playerName = playerName;
		__counter = LocalCounter.newInstance();

		// create a new TCP object and listen for this port
		__tcp = new TCP(Example4Constant.SOCKET_PORT);
		__tcp.receive(this);

		// create a new UDP object and listen for this port
		__udp = new UDP(Example4Constant.DATAGRAM_PORT);
		__udp.receive(this);

		// send a login request
		__sendLoginRequest();

	}

	private void __sendLoginRequest() {
		var data = ZeroObjectImpl.newInstance();
		data.putString(SharedEventKey.KEY_PLAYER_LOGIN, __playerName);
		__tcp.send(ServerMessage.newInstance().setData(data));

		if (LOGGER_DEBUG) {
			System.err.println("Login Request -> " + data.toString());
		}
	}

	@Override
	public void onReceivedTCP(ServerMessage message) {
		if (LOGGER_DEBUG) {
			System.err.println("[RECV FROM SERVER TCP] -> " + message);
		}

		var data = (ZeroObject) message.getData();
		if (data.containsKey(SharedEventKey.KEY_ALLOW_TO_ATTACH)) {
			switch (data.getByte(SharedEventKey.KEY_ALLOW_TO_ATTACH)) {
			case UdpEstablishedState.ALLOW_TO_ATTACH: {
				// now you can send request for UDP connection request
				var sendData = ZeroObjectImpl.newInstance().putString(SharedEventKey.KEY_PLAYER_LOGIN, __playerName);
				var request = ServerMessage.newInstance().setData(sendData);
				__udp.send(request);

				if (LOGGER_DEBUG) {
					System.out.println("Request a UDP connection -> " + request);
				}
			}
				break;

			case UdpEstablishedState.ATTACHED: {
				// the UDP connected successful, you now can send test requests
				if (LOGGER_DEBUG) {
					System.out.println("Start the conversation ...");
				}

				// packets counting
				Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

					int countPackets = __counter.getCountUdpPacketsOneMinute();
					double lostPacket = ((double) (Example4Constant.ONE_MINUTE_EXPECT_RECEIVE_PACKETS - countPackets)
							/ (double) Example4Constant.ONE_MINUTE_EXPECT_RECEIVE_PACKETS) * (double) 100;

					__statistics.addLostPackets(lostPacket);
					__logLostPacket(lostPacket);

					__counter.setCountUdpPacketsOneMinute(0);
					__counter.setCountReceivedPacketSizeOneMinute(0);

				}, 1, 1, TimeUnit.MINUTES);

				// send requests to calculate latency
				Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

					__sentTimestamp = TimeUtility.currentTimeMillis();

					__requestNeighbours();

				}, Example4Constant.SEND_MEASUREMENT_REQUEST_INTERVAL,
						Example4Constant.SEND_MEASUREMENT_REQUEST_INTERVAL, TimeUnit.SECONDS);
			}
				break;

			}
		} else if (data.containsKey(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS)) {
			int fps = data.getInteger(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS);
			long receivedTimestamp = TimeUtility.currentTimeMillis();
			long latency = receivedTimestamp - __sentTimestamp;

			__statistics.addLatency(latency);
			__statistics.addFps(fps);

			info("LATENCY", buildgen("Player ", __playerName, " -> ", latency, " ms | fps -> ", fps));
		}

	}

	private void __logLostPacket(double lostPacket) {
		info("COUNTING",
				String.format("Player %s -> Packet Count: %d (Loss: %.2f %%) -> Received Data: %.2f KB", __playerName,
						__counter.getCountUdpPacketsOneMinute(), lostPacket,
						(float) __counter.getCountReceivedPacketSizeOneMinute() / 1000.0f));
	}

	private void __requestNeighbours() {
		var data = ZeroObjectImpl.newInstance().putString(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS,
				ClientUtility.generateRandomString(10));
		var request = ServerMessage.newInstance().setData(data);
		__tcp.send(request);
	}

	@Override
	public void onReceivedUDP(ServerMessage message) {
		if (LOGGER_DEBUG) {
			System.err.println("[RECV FROM SERVER UDP] -> " + message);
		}

		__counting(message);
	}

	private void __counting(ServerMessage message) {
		__counter.addCountUdpPacketsOneMinute();
		__counter.addCountReceivedPacketSizeOneMinute((message.getData().toBinary().length));
	}

}
