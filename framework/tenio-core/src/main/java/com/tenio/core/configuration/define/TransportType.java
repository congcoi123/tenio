package com.tenio.core.configuration.define;

import java.util.HashMap;
import java.util.Map;

public enum TransportType {
	
	TCP("tcp"),
	UDP("udp"),
	WEB_SOCKET("websocket"),
	HTTP("http");

	// Reverse-lookup map for getting a type from a value
	private static final Map<String, TransportType> lookup = new HashMap<String, TransportType>();

	static {
		for (var type : TransportType.values()) {
			lookup.put(type.getValue(), type);
		}
	}

	private final String value;

	private TransportType(final String value) {
		this.value = value;
	}

	public final String getValue() {
		return this.value;
	}

	@Override
	public final String toString() {
		return this.name();
	}

	public static TransportType getByValue(String value) {
		return lookup.get(value);
	}

}
