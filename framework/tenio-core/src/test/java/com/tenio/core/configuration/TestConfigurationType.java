package com.tenio.core.configuration;

import java.util.HashMap;
import java.util.Map;

import com.tenio.common.configuration.ConfigurationType;

/**
 * Create your own configurations
 * 
 * @see CoreConfiguration
 * 
 * @author kong
 *
 */
public enum TestConfigurationType implements ConfigurationType {

	CUSTOM_VALUE_1("customValue1"),
	CUSTOM_VALUE_2("customValue2"),
	CUSTOM_VALUE_3("customValue3"),
	CUSTOM_VALUE_4("customValue4");

	// Reverse-lookup map for getting a type from a value
	private static final Map<String, TestConfigurationType> lookup = new HashMap<String, TestConfigurationType>();

	static {
		for (var configurationType : TestConfigurationType.values()) {
			lookup.put(configurationType.getValue(), configurationType);
		}
	}

	private final String value;

	private TestConfigurationType(final String value) {
		this.value = value;
	}

	public final String getValue() {
		return this.value;
	}

	@Override
	public final String toString() {
		return this.name();
	}

	public static TestConfigurationType getByValue(String value) {
		return lookup.get(value);
	}

}
