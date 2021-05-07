package com.tenio.core.network.entity.packet;

import java.util.HashMap;
import java.util.Map;

public enum PacketHeaderType {

	BINARY(1), BIG_SIZE(4), COMPRESSION(8), ENCRYPTION(16);

	// Reverse-lookup map for getting a type from a value
	private static final Map<Integer, PacketHeaderType> lookup = new HashMap<Integer, PacketHeaderType>();

	static {
		for (var type : PacketHeaderType.values()) {
			lookup.put(type.getValue(), type);
		}
	}

	private final int value;

	private PacketHeaderType(final int value) {
		this.value = value;
	}

	public final int getValue() {
		return this.value;
	}

	@Override
	public final String toString() {
		return this.name();
	}

	public static PacketHeaderType getByValue(int value) {
		return lookup.get(value);
	}

}
