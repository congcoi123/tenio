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
package com.tenio.examples.example2;

import com.tenio.entities.element.TObject;
import com.tenio.server.Server;

/**
 * Only for testing the FSM mechanism
 * 
 * @author kong
 *
 */
public class TestFSM {

	/**
	 * The entry point
	 */
	public static void main(String[] args) {
		// create a heart-beat
		Server.getInstance().getHeartBeatApi().initialize(1);
		Server.getInstance().getHeartBeatApi().create("daily-life", new LifeCycle());
		
		// try to send messages immediately
		for (int i = 1; i <= 5; i++) {
			var message = TObject.newInstance();
			message.put("IMMEDIATELY", "Hello Heartbeat at: " + System.currentTimeMillis() + " with order: " + i);
			System.out.println("SEND IMMEDIATELY: " + message);
			Server.getInstance().getHeartBeatApi().sendMessage("daily-life", message);
		}
		
		// try to send messages with delay time
		for (int i = 1; i <= 5; i++) {
			var mess = TObject.newInstance();
			mess.put("DELAY", "Hello Heartbeat at: " + System.currentTimeMillis() + " with delay: " + i * 10 + " second");
			System.out.println("SEND DELAY: " + mess);
			Server.getInstance().getHeartBeatApi().sendMessage("daily-life", mess, i * 10);
		}
		
	}

}
