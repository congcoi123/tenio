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
package com.tenio.network.netty.datagram;

import com.tenio.configuration.BaseConfiguration;
import com.tenio.configuration.constant.ErrorMsg;
import com.tenio.configuration.constant.LEvent;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entity.AbstractPlayer;
import com.tenio.event.IEventManager;
import com.tenio.message.codec.MsgPackConverter;
import com.tenio.network.Connection;
import com.tenio.network.netty.BaseNettyHandler;
import com.tenio.network.netty.NettyConnection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.AttributeKey;

/**
 * In this server, a UDP connection is treated as a sub-connection. That means
 * you need to create one main connection between one client and the server
 * first (a TCP connection). When it's finished, that client can send a request
 * for making a link.
 * 
 * @see BaseNettyHandler
 * 
 * @author kong
 * 
 */
public final class NettyDatagramHandler extends BaseNettyHandler {

	public NettyDatagramHandler(int index, IEventManager eventManager, BaseConfiguration configuration) {
		super(eventManager, index);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// get the message's content
		byte[] content;
		DatagramPacket datagram;
		if (msg instanceof DatagramPacket) {
			// get the packet and sender data, convert it to a bytes' array
			datagram = (DatagramPacket) msg;
			var buffer = datagram.content();
			int readableBytes = buffer.readableBytes();
			content = new byte[readableBytes];
			buffer.readBytes(content);
		} else {
			return;
		}

		// create a game object
		var message = MsgPackConverter.unserialize(content);
		if (message == null) {
			return;
		}

		var player = __getPlayer(ctx.channel(), datagram.sender().toString());
		// the condition for creating sub-connection
		if (player == null) {
			player = (AbstractPlayer) _eventManager.getExternal().emit(TEvent.ATTACH_UDP_REQUEST, message);

			if (player == null) {
				_eventManager.getExternal().emit(TEvent.ATTACH_UDP_FAILED, message, ErrorMsg.PLAYER_NOT_FOUND);
			} else if (!player.hasConnection()) {
				_eventManager.getExternal().emit(TEvent.ATTACH_UDP_FAILED, message, ErrorMsg.MAIN_CONNECTION_NOT_FOUND);
			} else {
				__savePlayerRemote(ctx.channel(), datagram.sender().toString(), player.getName());
				var connection = NettyConnection.newInstance(_eventManager, Connection.Type.DATAGRAM, ctx.channel());
				connection.setSockAddress(datagram.sender());
				player.setSubConnection(connection);
				_eventManager.getExternal().emit(TEvent.ATTACH_UDP_SUCCESS, player);
			}

		} else {
			_eventManager.getInternal().emit(LEvent.DATAGRAM_HANDLE, player, message);
		}

	}

	/**
	 * Retrieve a player by special credentials.
	 * 
	 * @param channel a channel, see {@link Channel}
	 * @param remote  the remote's address
	 * @return a player, see {@link AbstractPlayer}
	 */
	private AbstractPlayer __getPlayer(Channel channel, String remote) {
		return channel.attr(AttributeKey.valueOf(remote)).get() != null ? (AbstractPlayer) _eventManager.getInternal()
				.emit(LEvent.GET_PLAYER, (String) channel.attr(AttributeKey.valueOf(remote)).get()) : null;
	}

	/**
	 * Save a player to one channel by special credentials.
	 * 
	 * @param channel a channel, see {@link Channel}
	 * @param remote  the remote's address
	 * @param name    the player's name, see {@link AbstractPlayer#getName()}}
	 */
	private void __savePlayerRemote(Channel channel, String remote, String name) {
		channel.attr(AttributeKey.valueOf(remote)).set(name);
	}

}
