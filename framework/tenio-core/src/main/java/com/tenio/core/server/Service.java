package com.tenio.core.server;

public interface Service {

	void init(Object var1);

	void destroy(Object var1);

	void handleMessage(Object var1);

	String getName();

	void setName(String var1);

}
