package com.tenio.core.network.entity.session;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

import io.netty.channel.Channel;

public interface SessionManager {

	Session createSocketSession(SocketChannel socketChannel);

	void removeSessionBySocket(SocketChannel socketChannel);

	Session getSessionBySocket(SocketChannel socketChannel);

	Session createDatagramSession(DatagramChannel datagramChannel);

	void removeSessionByDatagram(String remoteAddress);

	Session getSessionByDatagram(String remoteAddress);

	Session createWebSocketSession(Channel webSocketChannel);

	void removeSessionByWebSocket(Channel webSocketChannel);

	Session getSessionByWebSocket(Channel webSocketChannel);

	void removeSessionById(long id);

	void removeSession(Session session);

}
