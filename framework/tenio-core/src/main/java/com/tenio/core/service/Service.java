package com.tenio.core.service;

public interface Service {

	void initialize() throws Exception;

	void start() throws Exception;

	void resume();

	void pause();

	void stop() throws Exception;

	void destroy();

	void onInitialized() throws Exception;

	void onStarted() throws Exception;

	void onResumed();

	void onRunning();

	void onPaused();

	void onStopped() throws Exception;

	void onDestroyed();
	
	boolean isActivated();

	String getName();

	void setName(String name);

}
