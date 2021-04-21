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

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SerializationUtils;

import com.tenio.common.element.CommonObject;
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
public final class TestClientMovement implements ISocketListener, IDatagramListener {

	private static float DELAY_CREATION = 0.1f;

	private static int NUMBER_OF_PLAYERS = 1;
	// 100 objects * 4 times * 60
	private static int ONE_MINUTE_EXPECT_RECEIVE_PACKETS = 4 * 60 * 100;

	/**
	 * The entry point
	 * 
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
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
	private int __countReceivedPacketSizeOneMinute;
	private int __countUdpPacketsOneMinute;

	public TestClientMovement(String playerName) {
		__playerName = playerName;
		__countUdpPacketsOneMinute = 0;
		__countReceivedPacketSizeOneMinute = 0;

		// logging
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

			System.out.println(String.format("Player %s -> Packet Count: %d (Loss: %.2f %%) -> Received Data: %.2f KB",
					__playerName, __countUdpPacketsOneMinute,
					(float) ((float) (ONE_MINUTE_EXPECT_RECEIVE_PACKETS - __countUdpPacketsOneMinute)
							/ ONE_MINUTE_EXPECT_RECEIVE_PACKETS * 100),
					(float) __countReceivedPacketSizeOneMinute / 1000));
			__countReceivedPacketSizeOneMinute = 0;
			__countUdpPacketsOneMinute = 0;

		}, 1, 1, TimeUnit.MINUTES);

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
		System.out.println("Login Request -> " + message);

	}

	@Override
	public void onReceivedTCP(CommonObject message) {

		if (message.contain("c")) {
			System.err.println("[RECV FROM SERVER TCP] -> " + message);
			switch ((String) message.get("c")) {
			case "udp": {
				// now you can send request for UDP connection request
				var request = CommonObject.newInstance();
				request.put("u", __playerName);
				__udp.send(request);
				System.out.println("Request a UDP connection -> " + request);
			}
				break;

			case "udp-done": {
				// the UDP connected successful, you now can send test requests
				System.out.println("Start the conversation ...");
			}
				break;

			}
		} else if (message.contain("p")) {
			__counting(message);
		}

	}

	@Override
	public void onReceivedUDP(CommonObject message) {
		// System.err.println("[RECV FROM SERVER UDP] -> " + message);
		__counting(message);
	}

	private void __counting(CommonObject message) {
		__countUdpPacketsOneMinute++;
		__countReceivedPacketSizeOneMinute += SerializationUtils.serialize(message).length;
	}

}
