package com.tenio.core.network.zero.engine.implement;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.logger.SystemLogger;
import com.tenio.common.utility.StringUtility;
import com.tenio.core.network.entity.session.SessionManager;
import com.tenio.core.network.zero.engine.ZeroEngine;
import com.tenio.core.network.zero.handler.DatagramIOHandler;
import com.tenio.core.network.zero.handler.SocketIOHandler;
import com.tenio.core.server.Service;

public abstract class AbstractZeroEngine extends SystemLogger implements ZeroEngine, Service, Runnable {

	private volatile int __id;
	private ExecutorService __threadPool;
	private Configuration __configuration;
	private SocketIOHandler __socketIOHandler;
	private DatagramIOHandler __datagramIOHandler;
	private SessionManager __sessionManager;
	private int __threadPoolSize;
	private volatile boolean __activated;

	public AbstractZeroEngine(int numberWorkers) {
		setActivated(false);
		__threadPoolSize = numberWorkers;
		__id = 0;
	}

	private void __initializeWorkers() {
		__threadPool = Executors.newFixedThreadPool(__threadPoolSize);
	}

	private void __runWorkers() {
		for (int i = 0; i < __threadPoolSize; i++) {
			__threadPool.execute(this);
		}
	}

	public void setup() {
		__initializeWorkers();
		onSetup();
	}

	public void start() {
		__runWorkers();
	}

	public void stop() {
		onStop();
		var leftOvers = __threadPool.shutdownNow();
		_info("ENGINE STOPPED", _buildgen("engine-", getEngineName(), " -> Unprocessed workers: ", leftOvers.size()));
	}

	public boolean isActivated() {
		return __activated;
	}

	public void setActivated(boolean activated) {
		__activated = activated;
	}

	public void setConfiguration(Configuration configuration) {
		__configuration = configuration;
	}

	public Configuration getConfiguration() {
		return __configuration;
	}

	public void setSocketIOHandler(SocketIOHandler socketIOHandler) {
		__socketIOHandler = socketIOHandler;
	}

	public SocketIOHandler getSocketIOHandler() {
		return __socketIOHandler;
	}

	public void setDatagramIOHandler(DatagramIOHandler datagramIOHandler) {
		__datagramIOHandler = datagramIOHandler;
	}

	public DatagramIOHandler getDatagramIOHandler() {
		return __datagramIOHandler;
	}

	public void setSessionManager(SessionManager sessionManager) {
		__sessionManager = sessionManager;
	}

	public SessionManager getSessionManager() {
		return __sessionManager;
	}

	@Override
	public void run() {
		__id++;
		_info("ENGINE START", _buildgen("engine-", getEngineName(), "-", __id));
		__setThreadName();

		if (isActivated()) {
			onRun();
		}

		_info("ENGINE STOPPING", _buildgen("engine-", getEngineName(), "-", __id));
	}

	private void __setThreadName() {
		Thread.currentThread().setName(StringUtility.strgen("engine-", getEngineName(), "-", __id));
	}

	public abstract void onSetup();

	public abstract void onRun();

	public abstract void onStop();

	public abstract String getEngineName();

}
