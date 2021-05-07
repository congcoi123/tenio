package com.tenio.common.data;

import java.util.HashMap;
import java.util.Map;

public enum ZeroDataType {

	NULL(0), BOOLEAN(1), BYTE(2), SHORT(3), INTEGER(4), LONG(5), FLOAT(6), DOUBLE(7), STRING(8), BOOLEAN_ARRAY(9),
	BYTE_ARRAY(10), SHORT_ARRAY(11), INTEGER_ARRAY(12), LONG_ARRAY(13), FLOAT_ARRAY(14), DOUBLE_ARRAY(15),
	STRING_ARRAY(16), ZERO_ARRAY(17), ZERO_OBJECT(18);

	// Reverse-lookup map for getting a type from a value
	private static final Map<Integer, ZeroDataType> lookup = new HashMap<Integer, ZeroDataType>();

	static {
		for (var type : ZeroDataType.values()) {
			lookup.put(type.getValue(), type);
		}
	}

	private final int value;

	private ZeroDataType(final int value) {
		this.value = value;
	}

	public final int getValue() {
		return this.value;
	}

	@Override
	public final String toString() {
		return this.name();
	}

	public static ZeroDataType getByValue(int value) {
		return lookup.get(value);
	}
	
}
