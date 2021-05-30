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

import java.lang.reflect.InvocationTargetException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.entities.packet.PacketQueue;
import com.tenio.core.network.entities.packet.implement.PacketQueueImpl;
import com.tenio.core.network.entities.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.entities.session.SessionManager;

import io.netty.channel.Channel;

public final class SessionManagerImpl extends AbstractManager implements SessionManager {

	private static final int DEFAULT_PACKET_QUEUE_SIZE = 100;

	@GuardedBy("this")
	private final Map<Long, Session> __sessionByIds;
	@GuardedBy("this")
	private final Map<SocketChannel, Session> __sessionBySockets;
	@GuardedBy("this")
	private final Map<Channel, Session> __sessionByWebSockets;
	@GuardedBy("this")
	private final Map<SocketAddress, Session> __sessionByDatagrams;

	private PacketQueuePolicy __packetQueuePolicy;
	private int __packetQueueSize;

	private volatile int __sessionCount;

	public static SessionManager newInstance(EventManager eventManager) {
		return new SessionManagerImpl(eventManager);
	}

	private SessionManagerImpl(EventManager eventManager) {
		super(eventManager);

		__sessionByIds = new HashMap<Long, Session>();
		__sessionBySockets = new HashMap<SocketChannel, Session>();
		__sessionByWebSockets = new HashMap<Channel, Session>();
		__sessionByDatagrams = new HashMap<SocketAddress, Session>();

		__sessionCount = 0;
		__packetQueueSize = DEFAULT_PACKET_QUEUE_SIZE;
		__packetQueuePolicy = null;
	}

	@Override
	public Session createSocketSession(SocketChannel socketChannel, SelectionKey selectionKey) {
		Session session = SessionImpl.newInstance();
		session.setSocketChannel(socketChannel);
		session.setSelectionKey(selectionKey);
		session.setSessionManager(this);
		session.setPacketQueue(__createNewPacketQueue());
		synchronized (this) {
			__sessionByIds.put(session.getId(), session);
			__sessionBySockets.put(session.getSocketChannel(), session);
			__sessionCount = __sessionByIds.size();
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
	public void addDatagramForSession(DatagramChannel datagramChannel, SocketAddress remoteAddress, Session session) {
		if (!session.isTcp()) {
			throw new IllegalArgumentException(
					String.format("Unable to add datagram channel for the non-TCP session: %s", session.toString()));
		}
		synchronized (__sessionByDatagrams) {
			session.setDatagramChannel(datagramChannel, remoteAddress);
			__sessionByDatagrams.put(session.getDatagramRemoteSocketAddress(), session);
		}
	}

	@Override
	public Session getSessionByDatagram(SocketAddress remoteAddress) {
		synchronized (__sessionByDatagrams) {
			return __sessionByDatagrams.get(remoteAddress);
		}
	}

	@Override
	public Session createWebSocketSession(Channel webSocketChannel) {
		Session session = SessionImpl.newInstance();
		session.setWebSocketChannel(webSocketChannel);
		session.setSessionManager(this);
		session.setPacketQueue(__createNewPacketQueue());
		synchronized (this) {
			__sessionByIds.put(session.getId(), session);
			__sessionByWebSockets.put(webSocketChannel, session);
			__sessionCount = __sessionByIds.size();
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

	private PacketQueue __createNewPacketQueue() {
		PacketQueue packetQueue = PacketQueueImpl.newInstance();
		packetQueue.setMaxSize(__packetQueueSize);
		packetQueue.setPacketQueuePolicy(__packetQueuePolicy);

		return packetQueue;
	}

	@Override
	public void setPacketQueuePolicy(Class<? extends PacketQueuePolicy> clazz)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		__packetQueuePolicy = clazz.getDeclaredConstructor().newInstance();
	}

	@Override
	public void setPacketQueueSize(int queueSize) {
		__packetQueueSize = queueSize;
	}

	@Override
	public void removeSession(Session session) {
		synchronized (this) {
			switch (session.getTransportType()) {
			case TCP:
				if (session.containsUdp()) {
					__sessionByDatagrams.remove(session.getDatagramRemoteSocketAddress());
					session.setDatagramChannel(null, null);
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
			__sessionCount = __sessionByIds.size();
		}
	}

	@Override
	public void emitEvent(ServerEvent event, Object... params) {
		__eventManager.emit(event, params);
	}

	@Override
	public int getSessionCount() {
		return __sessionCount;
	}

}
