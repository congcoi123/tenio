package com.tenio.core.network.entity.session.implement;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.SessionManager;
import com.tenio.core.server.Service;

import io.netty.channel.Channel;

public final class SessionManagerImpl implements SessionManager, Service {

	@Override
	public Session createSocketSession(SocketChannel socketChannel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeSessionBySocket(SocketChannel socketChannel) {
		// TODO Auto-generated method stub

	}

	@Override
	public Session getSessionBySocket(SocketChannel socketChannel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Session createDatagramSession(DatagramChannel datagramChannel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeSessionByDatagram(String remoteAddress) {
		// TODO Auto-generated method stub

	}

	@Override
	public Session getSessionByDatagram(String remoteAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Session createWebSocketSession(Channel webSocketChannel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeSessionByWebSocket(Channel webSocketChannel) {
		// TODO Auto-generated method stub

	}

	@Override
	public Session getSessionByWebSocket(Channel webSocketChannel) {
		// TODO Auto-generated method stub
		return null;
	}

}
