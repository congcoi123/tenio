package com.tenio.core.network.zero.handler;

import java.nio.channels.SocketChannel;

public interface IOHandler {

	void channelActive(SocketChannel socketChannel);

	void channelRead(SocketChannel channel, byte[] binaryData);
	
	void channelWrite(SocketChannel channel, byte[] binaryData);
	
	void channelInactive(SocketChannel socketChannel);

}
