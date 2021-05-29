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
package com.tenio.core.network.zero.engines.implement;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tenio.common.utilities.StringUtility;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.ServiceRuntimeException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.zero.engines.ZeroEngine;
import com.tenio.core.network.zero.handlers.DatagramIOHandler;
import com.tenio.core.network.zero.handlers.SocketIOHandler;

public abstract class AbstractZeroEngine extends AbstractManager implements ZeroEngine, Runnable {

	private static final int DEFAULT_NUMBER_WORKERS = 5;
	private static final int DEFAULT_BUFFER_SIZE = 1024;

	private volatile int __id;
	private String __name;

	private ExecutorService __executor;
	private int __executorSize;
	private int __bufferSize;

	private SocketIOHandler __socketIOHandler;
	private DatagramIOHandler __datagramIOHandler;
	private SessionManager __sessionManager;

	private volatile boolean __activated;

	protected AbstractZeroEngine(EventManager eventManager) {
		super(eventManager);

		__executorSize = DEFAULT_NUMBER_WORKERS;
		__bufferSize = DEFAULT_BUFFER_SIZE;
		__activated = false;
		__id = 0;
	}

	private void __initializeWorkers() {
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
						__halting();
					} catch (Exception e) {
						error(e);
					}
				}
			}
		});
	}

	private void __halting() throws ServiceRuntimeException {
		__activated = false;

		onShutdown();

		__executor.shutdownNow();

		info("STOPPED SERVICE", buildgen("zero-", getName(), " (", __executorSize, ")"));
		onDestroyed();
		info("DESTROYED SERVICE", buildgen("zero-", getName(), " (", __executorSize, ")"));
	}

	@Override
	public void run() {
		__id++;

		__setThreadName();

		onRunning();
	}

	private void __setThreadName() {
		Thread.currentThread().setName(StringUtility.strgen("zero-", getName(), "-", __id));
	}

	@Override
	public void setSocketIOHandler(SocketIOHandler socketIOHandler) {
		__socketIOHandler = socketIOHandler;
	}

	@Override
	public SocketIOHandler getSocketIOHandler() {
		return __socketIOHandler;
	}

	@Override
	public void setDatagramIOHandler(DatagramIOHandler datagramIOHandler) {
		__datagramIOHandler = datagramIOHandler;
	}

	@Override
	public DatagramIOHandler getDatagramIOHandler() {
		return __datagramIOHandler;
	}

	@Override
	public void setSessionManager(SessionManager sessionManager) {
		__sessionManager = sessionManager;
	}

	@Override
	public SessionManager getSessionManager() {
		return __sessionManager;
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
	public int getMaxBufferSize() {
		return __bufferSize;
	}

	@Override
	public void setMaxBufferSize(int maxSize) {
		__bufferSize = maxSize;
	}

	@Override
	public void initialize() {
		__initializeWorkers();
		onInitialized();
	}

	@Override
	public void start() {
		onStarted();
		__activated = true;
		info("START SERVICE", buildgen("zero-", getName(), " (", __executorSize, ")"));
	}

	@Override
	public void shutdown() {
		__halting();
	}

	public boolean isActivated() {
		return __activated;
	}

	@Override
	public String getName() {
		return __name;
	}

	@Override
	public void setName(String name) {
		__name = name;
	}

}
