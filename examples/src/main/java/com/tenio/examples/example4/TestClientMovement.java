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

	private static final NetworkStatistics __statistics = NetworkStatistics.newInstance();

	public static void main(String[] args) throws InterruptedException {

		// average measurement
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

			System.err.println(String.format("[AVERAGE LATENCY] Total Requests: %d -> Average Latency: %.2f ms",
					__statistics.getLatencySize(), __statistics.getLatencyAverage()));

			System.err.println(String.format("[AVERAGE FPS] Total Requests: %d -> Average FPS: %.2f",
					__statistics.getFpsSize(), __statistics.getFpsAverage()));

			System.err.println(String.format("[AVERAGE LOST PACKETS] Average Lost Packets: %.2f %%",
					__statistics.getLostPacketsAverage()));

		}, 0, Example4Constant.AVERAGE_LATENCY_MEASUREMENT_INTERVAL, TimeUnit.MINUTES);

		// create clients
		for (int i = 0; i < Example4Constant.NUMBER_OF_PLAYERS; i++) {

			new TestClientMovement(String.valueOf(i));

			if (Example4Constant.DELAY_CREATION != -1) {
				Thread.sleep((long) (Example4Constant.DELAY_CREATION * 1000));
			}
		}

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

		// packets counting
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

			float lostPacket = (float) ((float) (Example4Constant.ONE_MINUTE_EXPECT_RECEIVE_PACKETS
					- __counter.getCountUdpPacketsOneMinute()) / Example4Constant.ONE_MINUTE_EXPECT_RECEIVE_PACKETS
					* 100);
			info("COUNTING",
					String.format("Player %s -> Packet Count: %d (Loss: %.2f %%) -> Received Data: %.2f KB",
							__playerName, __counter.getCountUdpPacketsOneMinute(), lostPacket,
							(float) __counter.getCountReceivedPacketSizeOneMinute() / 1000));

			__counter.setCountUdpPacketsOneMinute(0);
			__counter.setCountReceivedPacketSizeOneMinute(0);

			__statistics.addLostPackets(lostPacket);

		}, 1, 1, TimeUnit.MINUTES);

	}

	private void __sendLoginRequest() {
		var data = ZeroObjectImpl.newInstance();
		data.putString(SharedEventKey.KEY_PLAYER_LOGIN, __playerName);
		__tcp.send(ServerMessage.newInstance().setData(data));

		System.err.println("Login Request -> " + data.toString());
	}

	@Override
	public void onReceivedTCP(ServerMessage message) {
		System.err.println("[RECV FROM SERVER TCP] -> " + message);

		if (message.getData().containsKey(SharedEventKey.KEY_ALLOW_TO_ATTACH)) {
			switch (message.getData().getByte(SharedEventKey.KEY_ALLOW_TO_ATTACH)) {
			case UdpEstablishedState.ALLOW_TO_ATTACH: {
				// now you can send request for UDP connection request
				var data = ZeroObjectImpl.newInstance().putString(SharedEventKey.KEY_PLAYER_LOGIN, __playerName);
				var request = ServerMessage.newInstance().setData(data);
				__udp.send(request);

				System.out.println("Request a UDP connection -> " + request);
			}
				break;

			case UdpEstablishedState.ATTACHED: {
				// the UDP connected successful, you now can send test requests
				System.out.println("Start the conversation ...");

				// send requests to calculate latency
				Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

					__sentTimestamp = TimeUtility.currentTimeMillis();

					__requestNeighbours();

				}, Example4Constant.SEND_MEASUREMENT_REQUEST_INTERVAL,
						Example4Constant.SEND_MEASUREMENT_REQUEST_INTERVAL, TimeUnit.SECONDS);
			}
				break;

			}
		} else if (message.getData().containsKey(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS)) {
			int fps = message.getData().getInteger(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS);
			long receivedTimestamp = TimeUtility.currentTimeMillis();
			long latency = receivedTimestamp - __sentTimestamp;

			__statistics.addLatency(latency);
			__statistics.addFps(fps);

			info("LATENCY", buildgen("Player ", __playerName, " -> ", latency, " ms | fps -> ", fps));
		}

	}

	private void __requestNeighbours() {
		var data = ZeroObjectImpl.newInstance().putString(SharedEventKey.KEY_PLAYER_REQUEST_NEIGHBOURS,
				ClientUtility.generateRandomString(10));
		var request = ServerMessage.newInstance().setData(data);
		__tcp.send(request);
	}

	@Override
	public void onReceivedUDP(ServerMessage message) {
		__counting(message);
	}

	private void __counting(ServerMessage message) {
		__counter.addCountUdpPacketsOneMinute();
		__counter.setCountReceivedPacketSizeOneMinute((message.getData().toBinary().length));
	}

}
