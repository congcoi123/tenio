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

import com.tenio.core.configuration.defines.InternalEvent;
import com.tenio.core.events.EventManager;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.zero.codec.decoder.PacketDecoder;
import com.tenio.core.network.zero.codec.decoder.PacketDecoderResultListener;
import com.tenio.core.network.zero.handlers.SocketIOHandler;

/**
 * @author kong
 */
public final class SocketIOHandlerImpl extends AbstractIOHandler
		implements SocketIOHandler, PacketDecoderResultListener {

	private PacketDecoder __packetDecoder;

	public static SocketIOHandler newInstance(EventManager eventManager) {
		return new SocketIOHandlerImpl(eventManager);
	}

	private SocketIOHandlerImpl(EventManager eventManager) {
		super(eventManager);
	}

	@Override
	public void resultFrame(Session session, byte[] binary) {
		if (!session.isConnected()) {
			__getInternalEvent().emit(InternalEvent.SESSION_REQUESTS_CONNECTION, session, binary);
		} else {
			__getInternalEvent().emit(InternalEvent.SESSION_READ_BINARY, session, binary);
		}
	}

	@Override
	public void updateDroppedPackets(long numberPackets) {
		__networkReaderStatistic.updateDroppedPackets(numberPackets);
	}

	@Override
	public void updateReadPackets(long numberPackets) {
		__networkReaderStatistic.updateReadPackets(numberPackets);
	}

	@Override
	public void channelActive(SocketChannel socketChannel, SelectionKey selectionKey) {
		Session session = __sessionManager.createSocketSession(socketChannel, selectionKey);
		__getInternalEvent().emit(InternalEvent.SESSION_WAS_CREATED, session);
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
			session.close();
		} catch (IOException e) {
			error(e, "Session: ", session.toString());
			__getInternalEvent().emit(InternalEvent.SESSION_OCCURED_EXCEPTION, session, e);
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
		__getInternalEvent().emit(InternalEvent.SESSION_OCCURED_EXCEPTION, session, exception);
	}

	@Override
	public void setPacketDecoder(PacketDecoder packetDecoder) {
		__packetDecoder = packetDecoder;
		__packetDecoder.setResultListener(this);
	}

}
