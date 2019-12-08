/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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
package com.tenio.net.netty;

import java.net.InetSocketAddress;

import com.tenio.api.PlayerApi;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.element.TObject;
import com.tenio.event.EventManager;
import com.tenio.message.codec.MsgPackConverter;
import com.tenio.net.Connection;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;

/**
 * Use <a href="https://netty.io/">Netty</a> to create a connection
 * instance @see {@link Connection}
 * 
 * @author kong
 * 
 */
public class NettyConnection extends Connection {

	/**
	 * Save this connection itself to its channel
	 */
	public static final AttributeKey<Connection> KEY_THIS = AttributeKey.valueOf("this");
	/**
	 * Save the player's name @see {@link AbstractPlayer#getName()} to its channel
	 */
	public static final AttributeKey<String> KEY_ID = AttributeKey.valueOf("id");

	/**
	 * @see {@link Channel}
	 */
	private Channel __channel;
	/**
	 * @see {@link AbstractPlayer#getName()}
	 */
	private String __id;
	/**
	 * Used for TCP/WS connection, save the client's address
	 */
	private String __address;
	private boolean __hasRemoteAddress;

	private NettyConnection(Type type, Channel channel) {
		super(type);
		__hasRemoteAddress = false;
		__channel = channel;
		// Fix address in a TCP and WebSocket instance
		// and no need to save channel in Datagram connection, because of only one
		// channel existed
		if (!isType(Type.DATAGRAM)) {
			__channel.attr(KEY_THIS).set(this);
			__address = ((InetSocketAddress) __channel.remoteAddress()).toString();
		}
		__id = null;
	}

	public static NettyConnection create(Type type, Channel channel) {
		return new NettyConnection(type, channel);
	}

	@Override
	public void send(TObject message) {
		if (isType(Type.SOCKET)) {
			__channel.writeAndFlush(MsgPackConverter.serialize(message));
		} else if (isType(Type.WEB_SOCKET)) {
			__channel.writeAndFlush(
					new BinaryWebSocketFrame(Unpooled.wrappedBuffer(MsgPackConverter.serialize(message))));
		} else if (isType(Type.DATAGRAM)) {
			if (__hasRemoteAddress) {
				__channel.writeAndFlush(
						new DatagramPacket(Unpooled.wrappedBuffer(MsgPackConverter.serialize(message)), _sockAddress));
			}
		}
	}

	@Override
	public void close() {
		// channel will be closed in the future
		__channel.close();
		// need to push event
		AbstractPlayer player = PlayerApi.getInstance().get(__id);
		if (player != null) {
			EventManager.getInstance().emit(TEvent.DISCONNECT_PLAYER, player);
		}
	}

	@Override
	public String getId() {
		return __id;
	}

	@Override
	public void setId(String id) {
		__id = id;
		__channel.attr(KEY_ID).set(id);
	}

	@Override
	public String getAddress() {
		return __address;
	}

	@Override
	public void setSockAddress(InetSocketAddress sockAddress) {
		__hasRemoteAddress = true;
		_sockAddress = sockAddress;
		__address = _sockAddress.toString();
	}

	@Override
	public void clean() {
		// only need for WebSocket and Socket
		__channel.attr(KEY_THIS).set(null);
		__channel.attr(KEY_ID).set(null);
		__channel = null;
	}

	@Override
	public Object getAttr(String key) {
		return __channel.attr(AttributeKey.valueOf(key)).get();
	}

	@Override
	public void setAttr(String key, Object value) {
		__channel.attr(AttributeKey.valueOf(key)).set(value);
	}

}
