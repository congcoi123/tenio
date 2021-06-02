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
package com.tenio.core.controller;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import com.tenio.common.utilities.StringUtility;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.RequestQueueFullException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.entities.protocols.Request;

public abstract class AbstractController extends AbstractManager implements Controller, Runnable {

	private static final int DEFAULT_MAX_QUEUE_SIZE = 50;
	private static final int DEFAULT_NUMBER_WORKERS = 5;

	private volatile int __id;
	private String __name;

	private ExecutorService __executor;
	private int __executorSize;

	private BlockingQueue<Request> __requestQueue;

	private int __maxQueueSize;

	private boolean __initialized;
	private volatile boolean __activated;

	protected AbstractController(EventManager eventManager) {
		super(eventManager);

		__maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
		__executorSize = DEFAULT_NUMBER_WORKERS;
		__activated = false;
		__initialized = false;
	}

	private void __initializeWorkers() {
		var requestComparator = RequestComparator.newInstance();
		__requestQueue = new PriorityBlockingQueue<Request>(__maxQueueSize, requestComparator);

		__executor = Executors.newFixedThreadPool(__executorSize);
		for (int i = 0; i < __executorSize; i++) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				error(e);
			}
			__executor.execute(this);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (__executor != null && !__executor.isShutdown()) {
					try {
						__shutdown();
					} catch (Exception e) {
						error(e);
					}
				}
			}
		});
	}

	private void __shutdown() {
		__activated = false;

		__executor.shutdownNow();

		info("STOPPED SERVICE", buildgen("controller-", getName(), " (", __executorSize, ")"));
		__destroy();
		info("DESTROYED SERVICE", buildgen("controller-", getName(), " (", __executorSize, ")"));
	}

	@Override
	public void run() {
		__id++;

		__setThreadName();

		while (true) {
			if (__activated) {
				try {
					var request = __requestQueue.take();
					processRequest(request);
				} catch (InterruptedException e1) {
					error(e1);
				} catch (Throwable e2) {
					error(e2);
				}
			}
		}
	}

	private void __destroy() {
		__executor = null;
		__requestQueue.clear();
		__requestQueue = null;
		onDestroyed();
	}

	private void __setThreadName() {
		Thread.currentThread().setName(StringUtility.strgen("controller-", getName(), "-", __id));
	}

	@Override
	public void initialize() {
		__initializeWorkers();
		__initialized = true;
	}

	@Override
	public void start() {
		__activated = true;
		info("START SERVICE", buildgen("controller-", getName(), " (", __executorSize, ")"));
	}

	@Override
	public void shutdown() {
		if (!__initialized) {
			return;
		}
		__shutdown();
	}

	@Override
	public String getName() {
		return __name;
	}

	@Override
	public void setName(String name) {
		__name = name;
	}

	@Override
	public boolean isActivated() {
		return __activated;
	}

	@Override
	public void enqueueRequest(Request request) {
		if (__requestQueue.size() >= __maxQueueSize) {
			var exception = new RequestQueueFullException(__requestQueue.size());
			error(exception, exception.getMessage());
			throw exception;
		}
		__requestQueue.add(request);
	}

	@Override
	public int getMaxRequestQueueSize() {
		return __maxQueueSize;
	}

	@Override
	public void setMaxRequestQueueSize(int maxSize) {
		__maxQueueSize = maxSize;
	}

	@Override
	public int getThreadPoolSize() {
		return __executorSize;
	}

	@Override
	public void setThreadPoolSize(int maxSize) {
		__executorSize = maxSize;
	}

	@Override
	public float getPercentageUsedRequestQueue() {
		return __maxQueueSize == 0 ? 0.0f : (float) (__requestQueue.size() * 100) / (float) __maxQueueSize;
	}

	public abstract void subscribe();

	public abstract void processRequest(Request request);

}
