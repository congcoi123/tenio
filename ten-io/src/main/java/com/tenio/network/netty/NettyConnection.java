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

import java.net.InetSocketAddress;

import com.tenio.configuration.constant.LEvent;
import com.tenio.entity.AbstractPlayer;
import com.tenio.entity.element.TObject;
import com.tenio.event.IEventManager;
import com.tenio.message.codec.MsgPackConverter;
import com.tenio.network.Connection;

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
	 * Save this connection itself to its channel. In case of Datagram channel, we
	 * use {@link #__address} as a key for the current connection
	 */
	public static final AttributeKey<Connection> KEY_CONNECTION = AttributeKey.valueOf(KEY_STR_CONNECTION);

	/**
	 * @see Channel
	 */
	private Channel __channel;
	/**
	 * @see AbstractPlayer#getName()
	 */
	private String __username;
	/**
	 * Save the client's address, in Datagram connection it is used for saving as a
	 * key of the {@link #__username}
	 */
	private String __address;
	/**
	 * Determine if the remote address was assigned or not (only use for the
	 * Datagram connection)
	 */
	private boolean __hasRemoteAddress;

	private NettyConnection(int index, IEventManager eventManager, Type type, Channel channel) {
		super(eventManager, type, index);
		__hasRemoteAddress = false;
		__channel = channel;
		// set fix address in a TCP and WebSocket instance
		// in case of Datagram connection, this value will be set later (when you
		// receive a message from client)
		// in the Datagram connection there is only one channel existed
		if (!isType(Type.DATAGRAM)) {
			__address = ((InetSocketAddress) __channel.remoteAddress()).toString();
		}
		__username = null;
	}

	public static NettyConnection newInstance(int index, IEventManager eventManager, Type type, Channel channel) {
		return new NettyConnection(index, eventManager, type, channel);
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
						new DatagramPacket(Unpooled.wrappedBuffer(MsgPackConverter.serialize(message)), _remote));
			}
		}
	}

	@Override
	public void close() {
		// this channel will be closed in the future
		__channel.close();
		// need to push event now
		_eventManager.getInternal().emit(LEvent.MANUALY_CLOSE_CONNECTION, __username);
	}

	@Override
	public String getUsername() {
		return __username;
	}

	@Override
	public void setUsername(String username) {
		__username = username;
	}

	@Override
	public void removeUsername() {
		__username = null;
	}

	@Override
	public Connection getThis() {
		if (isType(Type.DATAGRAM)) {
			return (Connection) __channel.attr(AttributeKey.valueOf(__address)).get();
		}
		return __channel.attr(KEY_CONNECTION).get();
	}

	@Override
	public void setThis() {
		if (isType(Type.DATAGRAM)) {
			__channel.attr(AttributeKey.valueOf(__address)).set(this);
		} else {
			__channel.attr(KEY_CONNECTION).set(this);
		}
	}

	@Override
	public void removeThis() {
		if (isType(Type.DATAGRAM)) {
			__channel.attr(AttributeKey.valueOf(__address)).set(null);
		} else {
			__channel.attr(KEY_CONNECTION).set(null);
		}
	}

	@Override
	public String getAddress() {
		return __address;
	}

	@Override
	public void setRemote(InetSocketAddress remote) {
		// only need for the Datagram connection
		if (isType(Type.DATAGRAM)) {
			__hasRemoteAddress = true;
			_remote = remote;
			__address = _remote.toString();
		}
	}

	@Override
	public void clean() {
		// only need for WebSocket and Socket
		removeUsername();
		removeThis();
		__channel = null;
	}

}
