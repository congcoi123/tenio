package com.tenio.examples.example4.constant;

public enum Deceleration {

	SLOW(3),

	NORMAL(2),

	FAST(1);

	private int __value;

	private Deceleration(final int value) {
		__value = value;
	}

	public int get() {
		return __value;
	}

	@Override
	public String toString() {
		return name();
	}

}
