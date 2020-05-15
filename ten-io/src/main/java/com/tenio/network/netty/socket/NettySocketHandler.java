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
package com.tenio.network.netty.socket;

import com.tenio.configuration.BaseConfiguration;
import com.tenio.configuration.constant.LEvent;
import com.tenio.event.IEventManager;
import com.tenio.message.codec.MsgPackConverter;
import com.tenio.network.Connection;
import com.tenio.network.netty.BaseNettyHandler;
import com.tenio.network.netty.NettyConnection;

import io.netty.channel.ChannelHandlerContext;

/**
 * Receive all messages sent from clients. It converts serialize data to a
 * system's object for convenience and easy to use. It also handles the logic
 * for the processing of players and connections.
 * 
 * @see BaseNettyHandler
 * 
 * @author kong
 * 
 */
public final class NettySocketHandler extends BaseNettyHandler {

	/**
	 * The maximum number of players that the server can handle
	 */
	private final int __maxPlayer;
	/**
	 * Allow a client can be re-connected or not, see
	 * {@link #_channelInactive(ChannelHandlerContext, boolean)}
	 */
	private final boolean __keepPlayerOnDisconnect;

	public NettySocketHandler(int index, IEventManager eventManager, BaseConfiguration configuration) {
		super(eventManager, index);
		__maxPlayer = configuration.getInt(BaseConfiguration.MAX_PLAYER) - 1;
		__keepPlayerOnDisconnect = configuration.getBoolean(BaseConfiguration.KEEP_PLAYER_ON_DISCONNECT);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		_channelInactive(ctx, __keepPlayerOnDisconnect);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// convert the bytes' array to a game message
		var message = MsgPackConverter.unserialize((byte[]) msg);
		if (message == null) {
			return;
		}

		// get the connection first
		var connection = _getConnection(ctx.channel());
		if (connection == null) { // the new connection
			connection = NettyConnection.newInstance(_eventManager, Connection.Type.SOCKET, ctx.channel());
			_eventManager.getInternal().emit(LEvent.CREATE_NEW_CONNECTION, __maxPlayer, __keepPlayerOnDisconnect,
					connection, message);
		} else {
			_eventManager.getInternal().emit(LEvent.SOCKET_HANDLE, connection, message);
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		_exceptionCaught(ctx, cause);
	}

}
