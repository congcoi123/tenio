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
package com.tenio.core.network.entity.session.implement;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.SessionManager;

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
	private final Map<InetSocketAddress, Session> __sessionByDatagrams;
	@GuardedBy("this")
	private final Map<Channel, Session> __sessionByWebSockets;

	public static SessionManager newInstance() {
		return new SessionManagerImpl();
	}

	private SessionManagerImpl() {
		__sessionByIds = new HashMap<Long, Session>();
		__sessionBySockets = new HashMap<SocketChannel, Session>();
		__sessionByDatagrams = new HashMap<InetSocketAddress, Session>();
		__sessionByWebSockets = new HashMap<Channel, Session>();
	}

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInitialized() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStarted() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResumed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRunning() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPaused() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopped() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroyed() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isActivated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

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
		return __sessionBySockets.get(socketChannel);
	}

	@Override
	public Session createDatagramSession(DatagramChannel datagramChannel) {
		Session session = SessionImpl.newInstance();
		session.setDatagramChannel(datagramChannel);
		session.setSessionManager(this);
		synchronized (this) {
			__sessionByIds.put(session.getId(), session);
			__sessionByDatagrams.put(session.getClientInetSocketAddress(), session);
		}
		return session;
	}

	@Override
	public void removeSessionByDatagram(InetSocketAddress remoteAddress) {
		Session session = getSessionByDatagram(remoteAddress);
		removeSession(session);
	}

	@Override
	public Session getSessionByDatagram(InetSocketAddress remoteAddress) {
		return __sessionByDatagrams.get(remoteAddress);
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
		return __sessionByWebSockets.get(webSocketChannel);
	}

	@Override
	public void removeSessionById(long id) {
		Session session = __sessionByIds.get(id);
		removeSession(session);
	}

	@Override
	public void removeSession(Session session) {
		synchronized (this) {
			switch (session.getTransportType()) {
			case TCP:
				__sessionBySockets.remove(session.getSocketChannel());
				break;

			case UDP:
				__sessionByDatagrams.remove(session.getClientInetSocketAddress());
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
