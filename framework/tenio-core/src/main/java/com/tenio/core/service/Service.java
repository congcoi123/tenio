package com.tenio.core.service;

import com.tenio.core.exceptions.ServiceRuntimeException;

public interface Service {

	void initialize() throws ServiceRuntimeException;

	void start() throws ServiceRuntimeException;

	void resume();

	void pause();

	void halt() throws ServiceRuntimeException;

	void destroy();

	boolean isActivated();

	String getName();

	void setName(String name);

}
