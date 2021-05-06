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
package com.tenio.example.example4;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SerializationUtils;

import com.tenio.common.data.element.CommonObject;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.utility.TimeUtility;
import com.tenio.example.client.IDatagramListener;
import com.tenio.example.client.ISocketListener;
import com.tenio.example.client.TCP;
import com.tenio.example.client.UDP;

/**
 * This class shows how a client communicates with the server:<br>
 * 1. Create connections.<br>
 * 2. Send a login request.<br>
 * 3. Receive a response for login success and send a UDP connection
 * request.<br>
 * 4. Receive a response for allowed UDP connection.<br>
 * 
 * @author kong
 *
 */
public final class TestClientMovement extends AbstractLogger implements ISocketListener, IDatagramListener {

	private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
	private static final String NUMBER = "0123456789";

	private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
	private static SecureRandom RANDOM = new SecureRandom();

	private static float DELAY_CREATION = 0.1f;
	// time in minutes
	private static int AVERAGE_LATENCY_MEASUREMENT_INTERVAL = 1;
	// time in seconds
	private static int SEND_MEASUREMENT_REQUEST_INTERVAL = 20;

	private static int NUMBER_OF_PLAYERS = 180;

	private static int ONE_SECOND_EXPECT_RECEIVE_PACKETS = 10;
	// 100 entities * ONE_SECOND_EXPECT_RECEIVE_PACKETS times * 60
	private static int ONE_MINUTE_EXPECT_RECEIVE_PACKETS = ONE_SECOND_EXPECT_RECEIVE_PACKETS * 60 * 100;

	private static final List<Long> __latencyRecorder = new ArrayList<Long>();
	private static final List<Integer> __fpsRecorder = new ArrayList<Integer>();
	private static final List<Float> __lostPacketRecorder = new ArrayList<Float>();

	/**
	 * The entry point
	 * 
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

		// average measurement
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

			synchronized (__latencyRecorder) {
				long average = 0;
				int size = __latencyRecorder.size();
				for (int i = 0; i < size; i++) {
					average += __latencyRecorder.get(i).longValue();
				}
				double result = (double) average / (double) size;

				System.err.println(String.format("[AVERAGE LATENCY] Total Requests: %d -> Average Latency: %.2f ms",
						size, result));

				if (size >= Integer.MAX_VALUE) {
					System.out.println(String.format("[AVERAGE LATENCY] Reset counter -> %d", size));
					__latencyRecorder.clear();
				}
			}

			synchronized (__fpsRecorder) {
				int average = 0;
				int size = __fpsRecorder.size();
				for (int i = 0; i < size; i++) {
					average += __fpsRecorder.get(i).intValue();
				}
				double result = (double) average / (double) size;

				System.err
						.println(String.format("[AVERAGE FPS] Total Requests: %d -> Average FPS: %.2f", size, result));

				if (size >= Integer.MAX_VALUE) {
					System.out.println(String.format("[AVERAGE LATENCY] Reset counter -> %d", size));
					__fpsRecorder.clear();
				}
			}

			synchronized (__lostPacketRecorder) {
				float average = 0;
				int size = __lostPacketRecorder.size();
				for (int i = 0; i < size; i++) {
					average += __lostPacketRecorder.get(i).floatValue();
				}
				float result = (float) average / (float) size;

				System.err.println(String.format("[AVERAGE LOST PACKETS] Average Lost Packets: %.2f %%", result));

				if (size >= Integer.MAX_VALUE) {
					System.out.println(String.format("[AVERAGE LOST PACKETS] Reset counter -> %d", size));
					__lostPacketRecorder.clear();
				}
			}

		}, 0, AVERAGE_LATENCY_MEASUREMENT_INTERVAL, TimeUnit.MINUTES);

		for (int i = 0; i < NUMBER_OF_PLAYERS; i++) {
			new TestClientMovement(String.valueOf(i));
			if (DELAY_CREATION != -1) {
				Thread.sleep((long) (DELAY_CREATION * 1000));
			}
		}

	}

	private final TCP __tcp;
	private final UDP __udp;
	private final String __playerName;
	private volatile int __countReceivedPacketSizeOneMinute;
	private volatile int __countUdpPacketsOneMinute;
	private volatile long __sentTimestamp;

	public TestClientMovement(String playerName) {
		__playerName = playerName;
		__countUdpPacketsOneMinute = 0;
		__countReceivedPacketSizeOneMinute = 0;

		// create a new TCP object and listen for this port
		__tcp = new TCP(8032);
		__tcp.receive(this);

		// create a new UDP object and listen for this port
		__udp = new UDP(8031);
		__udp.receive(this);

		// send a login request
		var message = CommonObject.newInstance();
		message.put("u", __playerName);
		__tcp.send(message);
		_info("LOGIN REQUEST", message);

		// packets counting
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

			float lostPacket = (float) ((float) (ONE_MINUTE_EXPECT_RECEIVE_PACKETS - __countUdpPacketsOneMinute)
					/ ONE_MINUTE_EXPECT_RECEIVE_PACKETS * 100);
			_info("COUNTING",
					String.format("Player %s -> Packet Count: %d (Loss: %.2f %%) -> Received Data: %.2f KB",
							__playerName, __countUdpPacketsOneMinute, lostPacket,
							(float) __countReceivedPacketSizeOneMinute / 1000));
			__countReceivedPacketSizeOneMinute = 0;
			__countUdpPacketsOneMinute = 0;

			synchronized (__lostPacketRecorder) {
				__lostPacketRecorder.add(lostPacket);
			}

		}, 1, 1, TimeUnit.MINUTES);

	}

	@Override
	public void onReceivedTCP(CommonObject message) {

		if (message.contain("c")) {
			_info("RECV FROM SERVER TCP", message);
			switch ((String) message.get("c")) {
			case "udp": {
				// now you can send request for UDP connection request
				var request = CommonObject.newInstance();
				request.put("u", __playerName);
				__udp.send(request);
				_info("REQUEST UDP CONNECTION", request);

				// send requests to calculate latency
				Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

					__sentTimestamp = TimeUtility.currentTimeMillis();
					__tcp.send(CommonObject.newInstance().add("a", __generateRandomString(10)));

				}, SEND_MEASUREMENT_REQUEST_INTERVAL, SEND_MEASUREMENT_REQUEST_INTERVAL, TimeUnit.SECONDS);
			}
				break;

			case "udp-done": {
				// the UDP connected successful, you now can send test requests
				_info("UDP CONNECTED", "Start the conversation ...");
			}
				break;

			}
		} else if (message.contain("r")) {
			// _info("RECV RESPONSE FROM SERVER TCP", message);
			int fps = message.getMessageObjectArray("r").getInt(0);
			long receivedTimestamp = TimeUtility.currentTimeMillis();
			long latency = receivedTimestamp - __sentTimestamp;
			synchronized (__latencyRecorder) {
				__latencyRecorder.add(latency);
			}
			synchronized (__fpsRecorder) {
				__fpsRecorder.add(fps);
			}
			_info("LATENCY", _buildgen("Player ", __playerName, " -> ", latency, " ms | fps -> ", fps));
		}

	}

	@Override
	public void onReceivedUDP(CommonObject message) {
		__counting(message);
	}

	private void __counting(CommonObject message) {
		__countUdpPacketsOneMinute++;
		__countReceivedPacketSizeOneMinute += SerializationUtils.serialize(message).length;
	}

	private String __generateRandomString(int length) {
		if (length < 1) {
			throw new IllegalArgumentException();
		}

		var sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			// 0-62 (exclusive), random returns 0-61
			int rndCharAt = RANDOM.nextInt(DATA_FOR_RANDOM_STRING.length());
			char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

			sb.append(rndChar);
		}

		return sb.toString();
	}

}
