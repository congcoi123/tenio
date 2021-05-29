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
package com.tenio.core.network.entities.session;

import java.lang.reflect.InvocationTargetException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.manager.Manager;
import com.tenio.core.network.entities.packet.policy.PacketQueuePolicy;

import io.netty.channel.Channel;

public interface SessionManager extends Manager {

	Session createSocketSession(SocketChannel socketChannel, SelectionKey selectionKey);

	void removeSessionBySocket(SocketChannel socketChannel) throws NullPointerException;

	Session getSessionBySocket(SocketChannel socketChannel);

	Session createWebSocketSession(Channel webSocketChannel);

	void removeSessionByWebSocket(Channel webSocketChannel) throws NullPointerException;

	Session getSessionByWebSocket(Channel webSocketChannel);

	void addDatagramForSession(DatagramChannel datagramChannel, SocketAddress remoteAddress, Session session)
			throws IllegalArgumentException;

	Session getSessionByDatagram(SocketAddress remoteAddress);

	void emitEvent(ServerEvent event, Object... params);

	void setPacketQueuePolicy(Class<? extends PacketQueuePolicy> clazz)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException;

	void setPacketQueueSize(int queueSize);

	/**
	 * Remove session from its manager, this method should not be called. Call
	 * instead the method {@link Session#close()} to completely eliminate the
	 * session.
	 * 
	 * @param session the removing session
	 */
	void removeSession(Session session);

	int getSessionCount();

}
