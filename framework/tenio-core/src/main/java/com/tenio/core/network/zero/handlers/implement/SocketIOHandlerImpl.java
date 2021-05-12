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

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.tenio.core.configuration.defines.InternalEvent;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.zero.codec.decoder.PacketDecoder;
import com.tenio.core.network.zero.codec.decoder.PacketDecoderResultListener;
import com.tenio.core.network.zero.handlers.SocketIOHandler;

/**
 * @author kong
 */
// TODO: Add description
public final class SocketIOHandlerImpl extends BaseZeroHandler implements SocketIOHandler, PacketDecoderResultListener {

	private PacketDecoder __packetDecoder;

	@Override
	public void resultFrame(Session session, byte[] data) {
		if (!session.isConnected()) {
			session.setConnected(true);
			session.activate();
			getInternalEventManager().emit(InternalEvent.SESSION_IS_CONNECTED, session);
		} else {
			getInternalEventManager().emit(InternalEvent.MESSAGE_HANDLED_IN_CHANNEL, session, data);
		}
	}

	@Override
	public void updateDroppedPackets(long numberPackets) {
		getNetworkReaderStatistic().updateDroppedPackets(numberPackets);
	}

	@Override
	public void updateReadPackets(long numberPackets) {
		getNetworkReaderStatistic().updateReadPackets(numberPackets);
	}

	@Override
	public void channelActive(SocketChannel socketChannel, SelectionKey selectionKey) {
		try {
			Session session = getSessionManager().createSocketSession(socketChannel, selectionKey);
			getInternalEventManager().emit(InternalEvent.NEW_SESSION_WAS_CREATED, session);
		} catch (Exception e) {
			error(e);
		}
	}

	@Override
	public void channelRead(Session session, byte[] binary) {
		__packetDecoder.decode(session, binary);
	}

	@Override
	public void channelInactive(SocketChannel socketChannel) {
		var connection = __getConnection(ctx.channel(), null);
		__eventManager.getInternal().emit(InternalEvent.SESSION_WAS_CLOSED, connection);
		connection = null;
	}

	@Override
	public void channelException(SocketChannel socketChannel, Exception exception) {

	}

	@Override
	public void setPacketDecoder(PacketDecoder packetDecoder) {
		__packetDecoder = packetDecoder;
		__packetDecoder.setResultListener(this);
	}

}
