/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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
package com.tenio.examples.example1;

import java.security.SecureRandom;

import com.tenio.entities.element.TObject;
import com.tenio.examples.client.ISocketListener;
import com.tenio.examples.client.TCP;

/**
 * This class shows how a client communicates with the server:<br>
 * 1. Create connections.<br>
 * 2. Send a login request.<br>
 * 3. Receive messages via TCP connection from the server.<br>
 * 4. Be logout by server.
 * 
 * [NOTE] The client test is also available on <code>C++</code> and
 * <code>JavaScript</code> language, please see the <code>README.md</code> for
 * more details
 * 
 * @author kong
 *
 */
public final class TestClientLogin implements ISocketListener {

	private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
	private static final String NUMBER = "0123456789";

	private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
	private static SecureRandom RANDOM = new SecureRandom();

	/**
	 * The entry point
	 */
	public static void main(String[] args) {
		new TestClientLogin();
	}

	/**
	 * @see TCP
	 */
	private TCP __tcp;

	public TestClientLogin() {
		// create a new TCP object and listen for this port
		__tcp = new TCP(8032);
		__tcp.receive(this);

		// send a login request
		var message = new TObject();
		message.put("u", __generateRandomString(5));
		__tcp.send(message);
		System.err.println("Login Request -> " + message);

	}

	@Override
	public void onReceivedTCP(TObject message) {
		System.out.println("[RECV FROM SERVER TCP] -> " + message);
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
