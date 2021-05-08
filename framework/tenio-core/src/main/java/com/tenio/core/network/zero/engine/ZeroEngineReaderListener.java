package com.tenio.core.network.zero.engine;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

public interface ZeroEngineReaderListener {

	void acceptDatagramChannel(DatagramChannel datagramChannel) throws ClosedChannelException;
	
	void acceptSocketChannel(SocketChannel socketChannel) throws ClosedChannelException;

}
