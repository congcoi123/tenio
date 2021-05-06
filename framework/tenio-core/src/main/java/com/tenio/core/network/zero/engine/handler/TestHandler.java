package com.tenio.core.network.zero.engine.handler;

import java.nio.channels.SelectionKey;

public final class TestHandler {

	public static void main(String[] args) {
		int reader = SelectionKey.OP_READ;
		System.out.println(reader);
		int writer = SelectionKey.OP_WRITE;
		System.out.println(writer);
		System.err.println(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}

}
