package com.tenio.example.server;

import java.util.HashMap;
import java.util.Map;

import com.tenio.common.configuration.ConfigurationType;

/**
 * Create your own configurations
 * 
 * @see ConfigurationType
 * 
 * @author kong
 *
 */
public enum ExampleConfigurationType implements ConfigurationType {
	
	CUSTOM_VALUE_1("customValue1"),
	CUSTOM_VALUE_2("customValue2"),
	CUSTOM_VALUE_3("customValue3"),
	CUSTOM_VALUE_4("customValue4");

	// Reverse-lookup map for getting a type from a value
	private static final Map<String, ExampleConfigurationType> lookup = new HashMap<String, ExampleConfigurationType>();

	static {
		for (var configurationType : ExampleConfigurationType.values()) {
			lookup.put(configurationType.getValue(), configurationType);
		}
	}

	private final String value;

	private ExampleConfigurationType(final String value) {
		this.value = value;
	}

	public final String getValue() {
		return this.value;
	}

	@Override
	public final String toString() {
		return this.name();
	}

	public static ExampleConfigurationType getByValue(String value) {
		return lookup.get(value);
	}
}
