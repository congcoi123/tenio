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
package com.tenio.api;

import com.tenio.engine.heartbeat.AbstractHeartBeat;
import com.tenio.engine.heartbeat.IHeartBeatManager;
import com.tenio.entity.element.TObject;
import com.tenio.logger.AbstractLogger;

/**
 * This class provides you a necessary interface for managing heart beats.
 * 
 * @see IHeartBeatManager
 * 
 * @author kong
 * 
 */
public final class HeartBeatApi extends AbstractLogger {

	private final IHeartBeatManager __heartBeatManager;

	public HeartBeatApi(IHeartBeatManager heartBeatManager) {
		__heartBeatManager = heartBeatManager;
	}

	/**
	 * The number of maximum heart-beats that the server can handle.
	 * 
	 * @param maxHeartbeat The number of maximum heart-beats that the server can
	 *                     handle
	 */
	public void initialize(int maxHeartbeat) {
		__heartBeatManager.initialize(maxHeartbeat);
	}

	/**
	 * Create a new heart-beat.
	 * 
	 * @param id        the unique id
	 * @param heartbeat See {@link AbstractHeartBeat}
	 */
	public void create(final String id, final AbstractHeartBeat heartbeat) {
		__heartBeatManager.create(id, heartbeat);
	}

	/**
	 * Dispose a heart-beat.
	 * 
	 * @param id the unique id
	 */
	public void dispose(final String id) {
		__heartBeatManager.dispose(id);
	}

	/**
	 * Send a message to a particular heart-beat with a delay time
	 * 
	 * @param id        the unique id
	 * @param message   the message content
	 * @param delayTime the delay time in seconds
	 */
	public void sendMessage(final String id, final TObject message, final double delayTime) {
		__heartBeatManager.sendMessage(id, message, delayTime);
	}

	/**
	 * Send a message to a particular heart-beat with no delay time
	 * 
	 * @param id      the unique id
	 * @param message the message content
	 */
	public void sendMessage(final String id, final TObject message) {
		__heartBeatManager.sendMessage(id, message);
	}

}
