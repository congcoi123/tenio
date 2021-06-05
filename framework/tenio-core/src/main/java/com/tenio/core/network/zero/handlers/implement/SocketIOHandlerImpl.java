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
package com.tenio.core.network.zero.handlers.implement;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.tenio.common.data.utilities.ZeroDataSerializerUtility;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.data.ServerMessage;
import com.tenio.core.entities.defines.modes.ConnectionDisconnectMode;
import com.tenio.core.entities.defines.modes.PlayerDisconnectMode;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.decoder.PacketDecoderResultListener;
import com.tenio.core.network.zero.handlers.SocketIOHandler;

public final class SocketIOHandlerImpl extends AbstractIOHandler
		implements SocketIOHandler, PacketDecoderResultListener {

	private BinaryPacketDecoder __packetDecoder;

	public static SocketIOHandler newInstance(EventManager eventManager) {
		return new SocketIOHandlerImpl(eventManager);
	}

	private SocketIOHandlerImpl(EventManager eventManager) {
		super(eventManager);
	}

	@Override
	public void resultFrame(Session session, byte[] binary) {
		var data = ZeroDataSerializerUtility.binaryToElement(binary);
		var message = ServerMessage.newInstance().setData(data);

		if (!session.isConnected()) {
			__eventManager.emit(ServerEvent.SESSION_REQUEST_CONNECTION, session, message);
		} else {
			__eventManager.emit(ServerEvent.SESSION_READ_MESSAGE, session, message);
		}
	}

	@Override
	public void updateDroppedPackets(long numberPackets) {
		__networkReaderStatistic.updateReadDroppedPackets(numberPackets);
	}

	@Override
	public void updateReadPackets(long numberPackets) {
		__networkReaderStatistic.updateReadPackets(numberPackets);
	}

	@Override
	public void channelActive(SocketChannel socketChannel, SelectionKey selectionKey) {
		Session session = __sessionManager.createSocketSession(socketChannel, selectionKey);
		__eventManager.emit(ServerEvent.SESSION_CREATED, session);
	}

	@Override
	public void sessionRead(Session session, byte[] binary) {
		__packetDecoder.decode(session, binary);
	}

	@Override
	public void channelInactive(SocketChannel socketChannel) {
		Session session = __sessionManager.getSessionBySocket(socketChannel);
		if (session == null) {
			return;
		}

		try {
			session.close(ConnectionDisconnectMode.LOST, PlayerDisconnectMode.CONNECTION_LOST);
		} catch (IOException e) {
			error(e, "Session closed with error: ", session.toString());
			__eventManager.emit(ServerEvent.SESSION_OCCURED_EXCEPTION, session, e);
		} finally {
			session = null;
		}
	}

	@Override
	public void channelException(SocketChannel socketChannel, Exception exception) {
		// do nothing, the exception was already logged
	}

	@Override
	public void sessionException(Session session, Exception exception) {
		__eventManager.emit(ServerEvent.SESSION_OCCURED_EXCEPTION, session, exception);
	}

	@Override
	public void setPacketDecoder(BinaryPacketDecoder packetDecoder) {
		__packetDecoder = packetDecoder;
		__packetDecoder.setResultListener(this);
	}

}
