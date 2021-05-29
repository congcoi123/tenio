package com.tenio.examples.example4.constant;

/**
 * 
 * @author sallyx (https://www.sallyx.org/sally/en/game-ai/)
 *
 */
public enum Deceleration {

	SLOW(3), NORMAL(2), FAST(1);

	private int __value;

	Deceleration(int value) {
		this.__value = value;
	}

	public int get() {
		return __value;
	}

}
