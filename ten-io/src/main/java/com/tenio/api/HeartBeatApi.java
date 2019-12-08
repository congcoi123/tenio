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
package com.tenio.api;

import com.tenio.engine.heartbeat.AbstractHeartBeat;
import com.tenio.engine.heartbeat.HeartBeatManager;
import com.tenio.logger.AbstractLogger;

/**
 * This class provides you a necessary interface for managing hearbeats.
 * 
 * @see {@link HeartBeatManager}
 * 
 * @author kong
 * 
 */
public class HeartBeatApi extends AbstractLogger {

	private static volatile HeartBeatApi __instance;

	private HeartBeatApi() {
	} // prevent creation manually

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static HeartBeatApi getInstance() {
		if (__instance == null) {
			__instance = new HeartBeatApi();
		}
		return __instance;
	}

	/**
	 * @see HeartBeatManager
	 */
	private HeartBeatManager __manager = HeartBeatManager.getInstance();

	/**
	 * @see HeartBeatManager#create(String, AbstractHeartBeat)
	 */
	public synchronized void create(final String id, final AbstractHeartBeat heartbeat) {
		__manager.create(id, heartbeat);
	}

	/**
	 * @see HeartBeatManager#dispose(String)
	 */
	public synchronized void dispose(final String id) {
		__manager.dispose(id);
	}

}
