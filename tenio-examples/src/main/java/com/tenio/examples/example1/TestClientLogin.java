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
 * [NOTE] The client test is also available on <code>C++</code> language, please
 * see the <code>README.md</code> for more details
 * 
 * @author kong
 *
 */
public final class TestClientLogin implements ISocketListener {

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
		TObject message = new TObject();
		message.put("u", "kong");
		__tcp.send(message);
		System.err.println("Login Request -> " + message);

	}

	@Override
	public void onReceivedTCP(TObject message) {
		System.out.println("[RECV FROM SERVER TCP] -> " + message);
	}

}
