package com.tenio.core.service;

public interface Service {

	void initialize() throws Exception;

	void start() throws Exception;

	void resume();

	void pause();

	void stop() throws Exception;

	void destroy();

	boolean isActivated();

	String getName();

	void setName(String name);

}
