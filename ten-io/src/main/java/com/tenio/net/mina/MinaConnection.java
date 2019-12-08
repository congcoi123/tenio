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
package com.tenio.net.mina;

import java.net.InetSocketAddress;

import org.apache.mina.core.session.IoSession;

import com.tenio.api.PlayerApi;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.element.TObject;
import com.tenio.event.EventManager;
import com.tenio.message.codec.MsgPackConverter;
import com.tenio.net.Connection;

import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * Use <a href="https://netty.io/">Netty</a> to create a connection
 * instance @see {@link Connection}
 * 
 * @author kong
 * 
 */
public class MinaConnection extends Connection {

	/**
	 * Save this connection itself to its session
	 */
	public static final String KEY_THIS = "this";
	/**
	 * Save the player's name @see {@link AbstractPlayer#getName()} to its channel
	 */
	public static final String KEY_ID = "id";

	/**
	 * @see {@link IoSession}
	 */
	private IoSession __session;
	/**
	 * @see {@link AbstractPlayer#getName()}
	 */
	private String __id;
	/**
	 * Used for UDP connection, save the client's address
	 */
	private String __address;

	private MinaConnection(Type type, IoSession session) {
		super(type);
		__session = session;
		__session.setAttribute(KEY_THIS, this);
		// Fix address in a TCP and WebSocket instance
		// and no need to save channel in Datagram connection, because of only one
		// channel existed
		if (!isType(Type.DATAGRAM)) {
			__address = ((InetSocketAddress) __session.getRemoteAddress()).getAddress().getHostAddress();
		}
		__id = null;
	}

	public static MinaConnection create(Type type, IoSession session) {
		return new MinaConnection(type, session);
	}

	@Override
	public void send(TObject message) {
		if (isType(Type.SOCKET)) {
			__session.write(MsgPackConverter.serialize(message));
		} else if (isType(Type.WEB_SOCKET)) {
			__session.write(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(MsgPackConverter.serialize(message))));
		} else if (isType(Type.DATAGRAM)) {
			__session.write(
					new DatagramPacket(Unpooled.wrappedBuffer(MsgPackConverter.serialize(message)), _sockAddress));
		}
	}

	@Override
	public void close() {
		// the session will be closed in the future
		__session.closeNow();
		// need to push event now
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
		if (id == null) {
			__session.removeAttribute(KEY_ID);
		} else {
			__session.setAttribute(KEY_ID, id);
		}
	}

	@Override
	public String getAddress() {
		return __address;
	}

	@Override
	public void setSockAddress(InetSocketAddress sockAddress) {
		_sockAddress = sockAddress;
		__address = _sockAddress.getAddress().getHostAddress();
	}

	@Override
	public void clean() {
		// only need for WebSocket and Socket
		__session.removeAttribute(KEY_THIS);
		__session.removeAttribute(KEY_ID);
		__session = null;
	}

	@Override
	public Object getAttr(String key) {
		return __session.getAttribute(key);
	}

	@Override
	public void setAttr(String key, Object value) {
		__session.setAttribute(key, value);
	}

}
