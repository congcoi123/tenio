package com.tenio.core.service;

public interface ServiceListener {

	void onInitialized() throws Exception;

	void onStarted() throws Exception;

	void onResumed();

	void onRunning();

	void onPaused();

	void onStopped() throws Exception;

	void onDestroyed();

}
