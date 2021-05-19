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
package com.tenio.core.network.entities.session.implement;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.entities.session.SessionManager;

import io.netty.channel.Channel;

/**
 * @author kong
 */
// TODO: Add description
public final class SessionManagerImpl implements SessionManager {

	@GuardedBy("this")
	private final Map<Long, Session> __sessionByIds;
	@GuardedBy("this")
	private final Map<SocketChannel, Session> __sessionBySockets;
	@GuardedBy("this")
	private final Map<Channel, Session> __sessionByWebSockets;
	@GuardedBy("this")
	private final Map<InetSocketAddress, Session> __sessionByDatagrams;

	public static SessionManager newInstance() {
		return new SessionManagerImpl();
	}

	private SessionManagerImpl() {
		__sessionByIds = new HashMap<Long, Session>();
		__sessionBySockets = new HashMap<SocketChannel, Session>();
		__sessionByWebSockets = new HashMap<Channel, Session>();
		__sessionByDatagrams = new HashMap<InetSocketAddress, Session>();
	}

	@Override
	public Session createSocketSession(SocketChannel socketChannel, SelectionKey selectionKey) {
		Session session = SessionImpl.newInstance();
		session.setSocketChannel(socketChannel);
		session.setSelectionKey(selectionKey);
		session.setSessionManager(this);
		synchronized (this) {
			__sessionByIds.put(session.getId(), session);
			__sessionBySockets.put(session.getSocketChannel(), session);
		}
		return session;
	}

	@Override
	public void removeSessionBySocket(SocketChannel socketChannel) {
		Session session = getSessionBySocket(socketChannel);
		removeSession(session);
	}

	@Override
	public Session getSessionBySocket(SocketChannel socketChannel) {
		synchronized (__sessionBySockets) {
			return __sessionBySockets.get(socketChannel);
		}
	}

	@Override
	public void addDatagramForSession(DatagramChannel datagramChannel, Session session) {
		synchronized (__sessionByDatagrams) {
			session.setDatagramChannel(datagramChannel);
			__sessionByDatagrams.put(session.getDatagramInetSocketAddress(), session);
		}
	}

	@Override
	public Session getSessionByDatagram(InetSocketAddress remoteAddress) {
		synchronized (__sessionByDatagrams) {
			return __sessionByDatagrams.get(remoteAddress);
		}
	}

	@Override
	public void removeSessionByDatagram(Session session) {
		if (!session.containsUdp()) {
			throw new IllegalArgumentException("Session does not contain UDP channel");
		}
		synchronized (__sessionByDatagrams) {
			__sessionByDatagrams.remove(session.getDatagramInetSocketAddress());
			session.setDatagramChannel(null);
		}
	}

	@Override
	public Session createWebSocketSession(Channel webSocketChannel) {
		Session session = SessionImpl.newInstance();
		session.setWebSocketChannel(webSocketChannel);
		session.setSessionManager(this);
		synchronized (this) {
			__sessionByIds.put(session.getId(), session);
			__sessionByWebSockets.put(webSocketChannel, session);
		}
		return session;
	}

	@Override
	public void removeSessionByWebSocket(Channel webSocketChannel) {
		Session session = getSessionByWebSocket(webSocketChannel);
		removeSession(session);
	}

	@Override
	public Session getSessionByWebSocket(Channel webSocketChannel) {
		synchronized (__sessionByWebSockets) {
			return __sessionByWebSockets.get(webSocketChannel);
		}
	}

	@Override
	public void removeSessionById(long id) {
		Session session = null;
		synchronized (__sessionByIds) {
			session = __sessionByIds.get(id);
		}
		removeSession(session);
	}

	@Override
	public void removeSession(Session session) {
		if (session == null) {
			throw new NullPointerException("Session does not exist");
		}
		synchronized (this) {
			switch (session.getTransportType()) {
			case TCP:
				if (session.containsUdp()) {
					__sessionByDatagrams.remove(session.getDatagramInetSocketAddress());
					session.setDatagramChannel(null);
				}
				__sessionBySockets.remove(session.getSocketChannel());
				break;

			case WEB_SOCKET:
				__sessionByWebSockets.remove(session.getWebSocketChannel());
				break;

			default:
				break;
			}
			__sessionByIds.remove(session.getId());
		}
	}

}
