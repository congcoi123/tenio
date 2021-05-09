/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.core.network.netty;

import java.net.InetSocketAddress;

import com.tenio.common.data.element.CommonObject;
import com.tenio.common.msgpack.ByteArrayInputStream;
import com.tenio.common.pool.ElementsPool;
import com.tenio.core.configuration.define.InternalEvent;
import com.tenio.core.event.EventManager;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.session.Connection;
import com.tenio.core.network.netty.option.NettyConnectionOption;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;

/**
 * Use <a href="https://netty.io/">Netty</a> to handle message. Base on the
 * messages' content. You can handle your own logic here.
 * 
 * @author kong
 * 
 */
public abstract class BaseNettyHandler extends ChannelInboundHandlerAdapter {

	private final ElementsPool<CommonObject> __commmonObjectPool;
	private final ElementsPool<ByteArrayInputStream> __byteArrayInputPool;
	private final EventManager __eventManager;
	private Connection __connection;
	private final TransportType __transportType;
	private final int __connectionIndex;

	public BaseNettyHandler(EventManager eventManager, ElementsPool<CommonObject> commonObjectPool,
			ElementsPool<ByteArrayInputStream> byteArrayInputPool, int connectionIndex, TransportType transportType) {
		__eventManager = eventManager;
		__commmonObjectPool = commonObjectPool;
		__byteArrayInputPool = byteArrayInputPool;
		__connectionIndex = connectionIndex;
		__transportType = transportType;
	}

	/**
	 * Retrieve a connection by its channel
	 * 
	 * @param channel,     see {@link Channel}
	 * @param remoteAdress the current address (in use for Datagram channel)
	 * @return a connection
	 */
	private Connection __getConnection(Channel channel, InetSocketAddress remoteAdress) {
		if (remoteAdress == null) {
			return channel.attr(NettyConnectionOption.CONNECTION).get();
		}
		return (Connection) channel.attr(AttributeKey.valueOf(remoteAdress.toString())).get();
	}

	/**
	 * Handle in-coming messages for the channel
	 *
	 * @param datagramChannelWorkers group of datagram channels
	 * @param ctx                    the channel, see {@link ChannelHandlerContext}
	 * @param message                the message, see {@link MessageObject}
	 * @param remoteAdress           the current remote address (in use for Datagram
	 *                               channel)
	 */
	protected void _channelRead(ChannelGroup datagramChannelWorkers, ChannelHandlerContext ctx, CommonObject message,
			InetSocketAddress remoteAdress) {
		var connection = __getConnection(ctx.channel(), remoteAdress);

		if (connection == null) {
			__connection = NettyConnection.newInstance(__connectionIndex, __eventManager, __transportType,
					ctx.channel(), datagramChannelWorkers);
			__connection.setRemote(remoteAdress);
			__connection.setThis();
		}

		__eventManager.getInternal().emit(InternalEvent.MESSAGE_HANDLED_IN_CHANNEL, __connectionIndex, connection,
				message, __connection);
	}

	/**
	 * When a client is disconnected from your server for any reason, you can handle
	 * it in this event (only for TCP and WebSocket)
	 * 
	 * @param ctx the channel, see {@link ChannelHandlerContext}
	 */
	protected void _channelInactive(ChannelHandlerContext ctx) {
		// get the connection first
		var connection = __getConnection(ctx.channel(), null);
		__eventManager.getInternal().emit(InternalEvent.CONNECTION_WAS_CLOSED, connection);
		connection = null;
	}

	/**
	 * Record the exceptions (only for TCP and WebSocket)
	 * 
	 * @param ctx   the channel, see {@link ChannelHandlerContext}
	 * @param cause the exception will occur
	 */
	protected void _exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// get the connection first
		var connection = __getConnection(ctx.channel(), null);
		__eventManager.getInternal().emit(InternalEvent.CONNECTION_MESSAGE_HANDLED_EXCEPTION,
				ctx.channel().id().asLongText(), connection, cause);
	}

	/**
	 * @return the message object manager pool instance
	 */
	protected ElementsPool<CommonObject> getCommonObjectPool() {
		return __commmonObjectPool;
	}

	/**
	 * @return the byte array input steam manager pool instance
	 */
	protected ElementsPool<ByteArrayInputStream> getByteArrayInputPool() {
		return __byteArrayInputPool;
	}

}
