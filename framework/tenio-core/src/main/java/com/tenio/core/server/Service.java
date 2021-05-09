package com.tenio.core.server;

public interface Service {

	void initialize();

	void start();

	void resume();

	void pause();

	void stop();

	void destroy();

	void onInitialized();

	void onStarted();

	void onResumed();

	void onRunning();

	void onPaused();

	void onStopped();

	void onDestroyed();

	String getName();

	void setName(String name);

}
