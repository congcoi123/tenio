package com.tenio.core.network.define;

public enum ResponsePriority {

	NON_GUARANTEED(1), NORMAL(2), GUARANTEED(3), GUARANTEED_QUICKEST(4);

	private final int value;

	private ResponsePriority(int value) {
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
