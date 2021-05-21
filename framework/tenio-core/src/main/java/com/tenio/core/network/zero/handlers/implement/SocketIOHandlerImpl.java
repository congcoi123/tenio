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
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;
import com.tenio.core.network.zero.codec.decoder.PacketDecoder;
import com.tenio.core.network.zero.codec.decoder.PacketDecoderResultListener;
import com.tenio.core.network.zero.handlers.SocketIOHandler;

/**
 * @author kong
 */
public final class SocketIOHandlerImpl extends BaseZeroHandler implements SocketIOHandler, PacketDecoderResultListener {

	private PacketDecoder __packetDecoder;

	public static SocketIOHandler newInstance(EventManager eventManager, SessionManager sessionManager,
			NetworkReaderStatistic networkReaderStatistic, NetworkWriterStatistic networkWriterStatistic) {
		return new SocketIOHandlerImpl(eventManager, sessionManager, networkReaderStatistic, networkWriterStatistic);
	}

	private SocketIOHandlerImpl(EventManager eventManager, SessionManager sessionManager,
			NetworkReaderStatistic networkReaderStatistic, NetworkWriterStatistic networkWriterStatistic) {
		super(eventManager, sessionManager, networkReaderStatistic, networkWriterStatistic);
	}

	@Override
	public void resultFrame(Session session, byte[] data) {
		if (!session.isConnected()) {
			session.setConnected(true);
			session.activate();
			__getInternalEvent().emit(InternalEvent.SESSION_WAS_CONNECTED, session);
		} else {
			__getInternalEvent().emit(InternalEvent.SESSION_READ_BINARY, session, data);
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
	public void channelRead(SocketChannel socketChannel, byte[] binary) {
		// TODO Auto-generated method stub

	}

	@Override
	public void channelRead(Session session, byte[] binary) {
		__packetDecoder.decode(session, binary);
	}

	@Override
	public void channelInactive(SocketChannel socketChannel) {
		Session session = __sessionManager.getSessionBySocket(socketChannel);
		try {
			session.close();
		} catch (IOException e) {
			error(e, "Session: ", session.toString());
		} finally {
			session = null;
		}
	}

	@Override
	public void channelException(SocketChannel socketChannel, Exception exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void channelException(Session session, Exception exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPacketDecoder(PacketDecoder packetDecoder) {
		__packetDecoder = packetDecoder;
		__packetDecoder.setResultListener(this);
	}

}
