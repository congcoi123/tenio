package com.tenio.core.network.entity.protocol;

public interface Message {

	long getId();

	void setId(long id);

	byte[] getContent();

	void setContent(byte[] content);

	Object getAttribute(String key);

	void setAttribute(String key, Object value);

}
