package com.tenio.core.network.zero.handler.implement;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.zero.handler.SocketIOHandler;

public final class DefaultSocketIOHandler implements SocketIOHandler {

	@Override
	public void channelActive(SocketChannel socketChannel, SelectionKey selectionKey) {
		ISession session = this.sessionManager.createSession(connection);
        session.setSystemProperty("SessionSelectionKey", selectionKey);
        this.sessionManager.addSession(session);
        // this.logger.info("Session created: " + session + " on Server port: " + connection.socket().getLocalPort() + " <---> " + session.getClientPort());
	}

	@Override
	public void channelRead(Session session, byte[] binaryData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void channelWrite(Session session, byte[] binaryData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void channelInactive(SocketChannel socketChannel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void channelException(Session session, Exception exception) {
		// TODO Auto-generated method stub
		
	}

}
