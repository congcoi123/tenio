package com.tenio.core.network.netty;

import com.tenio.core.network.IConnection;

import io.netty.util.AttributeKey;

public final class NettyConnectionOption {

	/**
	 * Save this connection itself to its channel. In case of Datagram channel, we
	 * use {@link NettyConnection#getAddress()} as a key for the current connection
	 */
	public static final AttributeKey<IConnection> CONNECTION = AttributeKey.valueOf("netty-key-connection");
	
}
