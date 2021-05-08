package com.tenio.core.controller;

import com.tenio.core.exception.RequestQueueFullException;
import com.tenio.core.network.entity.protocol.Request;

public interface Controller {
	
	Object getId();

	void setId(Object var1);

	void enqueueRequest(Request var1) throws RequestQueueFullException;

	int getQueueSize();

	int getMaxQueueSize();

	void setMaxQueueSize(int var1);

	int getThreadPoolSize();

	void setThreadPoolSize(int var1);

}
