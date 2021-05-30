package com.tenio.examples.example4.constant;

public enum SummingMethod {

	WEIGHTED_AVERAGE,

	PRIORITIZED,

	DITHERED;

	@Override
	public String toString() {
		return name();
	}

}
