/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.network.netty;

import com.tenio.configuration.constant.LEvent;
import com.tenio.entity.element.TObject;
import com.tenio.event.IEventManager;
import com.tenio.network.Connection;
import com.tenio.network.Connection.Type;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

/**
 * Use <a href="https://netty.io/">Netty</a> to handle message. Base on the
 * messages' content. You can handle your own logic here.
 * 
 * @author kong
 * 
 */
public abstract class BaseNettyHandler extends ChannelInboundHandlerAdapter {

	private IEventManager __eventManager;
	private Connection __connection;
	private Connection.Type __type;
	private int __index;

	public BaseNettyHandler(IEventManager eventManager, int index, Connection.Type type) {
		__eventManager = eventManager;
		__index = index;
		__type = type;
	}

	/**
	 * Retrieve a connection by its channel
	 * 
	 * @param channel,      see {@link Channel}
	 * @param remoteAddress the current address (in use for Datagram channel)
	 * @return a connection
	 */
	protected Connection __getConnection(Channel channel, String remoteAddress) {
		if (remoteAddress == null) {
			return channel.attr(NettyConnection.KEY_CONNECTION).get();
		}
		return (Connection) channel.attr(AttributeKey.valueOf(remoteAddress)).get();
	}

	/**
	 * When a client is first connect to your server for any reason, you can create
	 * a new connection here. But in case of Datagram, this channel is only active
	 * once time
	 * 
	 * @param ctx the channel, see {@link ChannelHandlerContext}
	 */
	protected void _channelActive(ChannelHandlerContext ctx) {
		__connection = NettyConnection.newInstance(__index, __eventManager, __type, ctx.channel());
	}

	/**
	 * Handle in-comming messages for the channel
	 * 
	 * @param ctx     the channel, see {@link ChannelHandlerContext}
	 * @param message the message, see {@link TObject}
	 */
	protected void _channelRead(ChannelHandlerContext ctx, TObject message) {

	}

	/**
	 * When a client is disconnected from your server for any reason, you can handle
	 * it in this event
	 * 
	 * @param ctx the channel, see {@link ChannelHandlerContext}
	 */
	protected void _channelInactive(ChannelHandlerContext ctx) {
		// get the connection first
		var connection = _getConnection(ctx.channel());
		__eventManager.getInternal().emit(LEvent.CONNECTION_CLOSE, connection);
		connection = null;
	}

	/**
	 * Record the exceptions
	 * 
	 * @param ctx   the channel, see {@link ChannelHandlerContext}
	 * @param cause the exception will occur
	 */
	protected void _exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// get the connection first
		var connection = _getConnection(ctx.channel());
		__eventManager.getInternal().emit(LEvent.CONNECTION_EXCEPTION, ctx.channel().id().asLongText(), connection,
				cause);
	}

}
