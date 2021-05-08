package com.tenio.core.network.zero.engine.listener;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

public interface ZeroReaderListener {

	void acceptDatagramChannel(DatagramChannel datagramChannel) throws ClosedChannelException;
	
	void acceptSocketChannel(SocketChannel socketChannel) throws ClosedChannelException;

}
