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
package com.tenio.engine.heartbeat;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.bootstrap.annotations.Component;
import com.tenio.common.loggers.SystemLogger;
import com.tenio.engine.exceptions.HeartbeatNotFoundException;
import com.tenio.engine.message.EMessage;

/**
 * The Java ExecutorService is a construct that allows you to pass a task to be
 * executed by a thread asynchronously. The executor service creates and
 * maintains a reusable pool of threads for executing submitted tasks. This
 * class helps you create and manage your HeartBeats. See:
 * {@link AbstractHeartBeat}
 * 
 * @see HeartBeatManager
 */
@ThreadSafe
@Component
public final class HeartBeatManagerImpl extends SystemLogger implements HeartBeatManager {

	@GuardedBy("this")
	private final Map<String, Future<Void>> __threadsManager;
	/**
	 * A Set is used as the container for the delayed messages because of the
	 * benefit of automatic sorting and avoidance of duplicates. Messages are sorted
	 * by their dispatch time. @see {@link HMessage}
	 */
	@GuardedBy("this")
	private final Map<String, TreeSet<HMessage>> __messagesManager;
	private ExecutorService __executorService;

	public HeartBeatManagerImpl() {
		__threadsManager = new HashMap<String, Future<Void>>();
		__messagesManager = new HashMap<String, TreeSet<HMessage>>();
	}

	@Override
	public void initialize(final int maxHeartbeat) throws Exception {
		__executorService = Executors.newFixedThreadPool(maxHeartbeat);
		info("INITIALIZE HEART BEAT", buildgen(maxHeartbeat));
	}

	@Override
	public synchronized void create(final String id, final AbstractHeartBeat heartbeat) {
		try {
			info("CREATE HEART BEAT", buildgen("id: ", id));
			// Add the listener
			var listener = new TreeSet<HMessage>();
			heartbeat.setMessageListener(listener);
			__messagesManager.put(id, listener);
			// Start the heart-beat
			var future = __executorService.submit(heartbeat);
			__threadsManager.put(id, future);
		} catch (Exception e) {
			error(e, "id: ", id);
		}
	}

	@Override
	public synchronized void dispose(final String id) {
		try {
			if (!__threadsManager.containsKey(id)) {
				throw new HeartbeatNotFoundException();
			}

			var future = __threadsManager.get(id);
			if (future == null) {
				throw new NullPointerException();
			}

			future.cancel(true);
			__threadsManager.remove(id);

			info("DISPOSE HEART BEAT", buildgen(id));

			// Remove the listener
			__messagesManager.get(id).clear();
			__messagesManager.remove(id);

			future = null;

		} catch (Exception e) {
			error(e, "id: ", id);
		}
	}

	@Override
	public synchronized boolean contains(final String id) {
		return __threadsManager.containsKey(id);
	}

	@Override
	public synchronized void clear() {
		if (__executorService != null) {
			__executorService.shutdownNow();
		}
		__executorService = null;
		__threadsManager.clear();
	}

	@Override
	public void sendMessage(String id, EMessage message, double delayTime) {
		var container = HMessage.newInstance(message, delayTime);
		var treeSet = __messagesManager.get(id);
		synchronized (treeSet) {
			treeSet.add(container);
		}
	}

	@Override
	public void sendMessage(String id, EMessage message) {
		sendMessage(id, message, 0);
	}

}
