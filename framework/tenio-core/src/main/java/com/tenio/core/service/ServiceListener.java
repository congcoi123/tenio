package com.tenio.core.service;

import com.tenio.core.exceptions.ServiceRuntimeException;

public interface ServiceListener {

	void onInitialized() throws ServiceRuntimeException;

	void onStarted() throws ServiceRuntimeException;

	void onResumed();

	void onRunning();

	void onPaused();

	void onHalted() throws ServiceRuntimeException;

	void onDestroyed();

}
