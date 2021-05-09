package com.tenio.core.network.zero.engine.implement;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.logger.SystemLogger;
import com.tenio.common.utility.StringUtility;
import com.tenio.core.network.entity.session.SessionManager;
import com.tenio.core.network.zero.engine.ZeroEngine;
import com.tenio.core.network.zero.handler.DatagramIOHandler;
import com.tenio.core.network.zero.handler.SocketIOHandler;

public abstract class AbstractZeroEngine extends SystemLogger implements ZeroEngine, Runnable {

	private volatile int __id;
	private String __name;

	private ExecutorService __executor;
	private int __executorSize;

	private Configuration __configuration;

	private SocketIOHandler __socketIOHandler;
	private DatagramIOHandler __datagramIOHandler;
	private SessionManager __sessionManager;

	private volatile boolean __activated;

	public AbstractZeroEngine(int numberWorkers) {
		__executorSize = numberWorkers;
		__activated = false;
		__id = 0;
	}

	private void __initializeWorkers() {
		__executor = Executors.newFixedThreadPool(__executorSize);
		for (int i = 0; i < __executorSize; i++) {
			__executor.execute(this);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (__executor != null && !__executor.isShutdown()) {
					__stop();
				}
			}
		});
	}

	private void __stop() {
		pause();
		onStopped();
		__executor.shutdown();
		while (true) {
			try {
				if (__executor.awaitTermination(5, TimeUnit.SECONDS)) {
					break;
				}
			} catch (InterruptedException e) {
				_error(e);
			}
		}
		_info("ENGINE STOPPED", _buildgen("engine-", getName(), "-", __id));
		destroy();
		onDestroyed();
		_info("ENGINE DESTROYED", _buildgen("engine-", getName(), "-", __id));
	}

	@Override
	public void run() {
		__id++;
		_info("ENGINE START", _buildgen("engine-", getName(), "-", __id));
		__setThreadName();

		if (__activated) {
			onRunning();
		}

		_info("ENGINE STOPPING", _buildgen("engine-", getName(), "-", __id));
	}

	private void __setThreadName() {
		Thread.currentThread().setName(StringUtility.strgen("engine-", getName(), "-", __id));
	}

	@Override
	public void setConfiguration(Configuration configuration) {
		__configuration = configuration;
	}

	@Override
	public Configuration getConfiguration() {
		return __configuration;
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
	public void initialize() {
		__initializeWorkers();
		onInitialized();
	}

	@Override
	public void start() {
		__activated = true;
	}

	@Override
	public void resume() {
		__activated = true;
	}

	@Override
	public void pause() {
		__activated = false;
	}

	@Override
	public void stop() {
		__stop();
	}

	@Override
	public void destroy() {
		__executor = null;
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
