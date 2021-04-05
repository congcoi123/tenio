/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.example.example3;

import java.security.SecureRandom;

import com.tenio.common.element.CommonObject;
import com.tenio.common.element.CommonObjectArray;
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
 * 5. Send messages via UDP connection and get these echoes from the server.<br>
 * 6. Close connections.
 * 
 * @author kong
 *
 */
public final class TestClientAttach implements ISocketListener, IDatagramListener {

	private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
	private static final String NUMBER = "0123456789";

	private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
	private static SecureRandom RANDOM = new SecureRandom();

	/**
	 * The entry point
	 */
	public static void main(String[] args) {
		new TestClientAttach();
	}

	private final TCP __tcp;
	private final UDP __udp;
	private final String __playerName;

	public TestClientAttach() {
		__playerName = __generateRandomString(6);

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
			for (int i = 1; i <= 100; i++) {
				var request = CommonObject.newInstance();
				request.put("fc", CommonObjectArray.newInstance().put("F").put("r").put(0).put(false));
				__udp.send(request);
				System.out.println("[SENT TO SERVER " + i + "] -> " + request);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			__tcp.close();
			__udp.close();

		}
			break;

		}
	}

	@Override
	public void onReceivedUDP(CommonObject message) {
		System.err.println("[RECV FROM SERVER UDP] -> " + message);
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
