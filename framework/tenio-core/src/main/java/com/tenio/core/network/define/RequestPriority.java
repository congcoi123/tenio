package com.tenio.core.network.define;

public enum RequestPriority {

	LOWEST(1), LOW(2), NORMAL(3), QUICKEST(4);

	private final int value;

	private RequestPriority(int value) {
		this.value = value;
	}

	public final int getValue() {
		return this.value;
	}

	@Override
	public final String toString() {
		return this.name();
	}

}
