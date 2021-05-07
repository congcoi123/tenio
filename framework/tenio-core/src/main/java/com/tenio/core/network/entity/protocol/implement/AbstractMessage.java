package com.tenio.core.network.entity.protocol.implement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tenio.core.network.entity.protocol.Message;

public abstract class AbstractMessage implements Message {

	private long __id;
	private byte[] __content;
	private Map<String, Object> __attributes;

	public long getId() {
		return __id;
	}

	public void setId(long id) {
		__id = id;
	}

	public byte[] getContent() {
		return __content;
	}

	public void setContent(byte[] content) {
		__content = content;
	}

	public Object getAttribute(String key) {
		if (__attributes != null) {
			return __attributes.get(key);
		}

		return null;
	}

	public void setAttribute(String key, Object value) {
		if (__attributes == null) {
			__attributes = new ConcurrentHashMap<String, Object>();
		}

		__attributes.put(key, value);
	}

}
