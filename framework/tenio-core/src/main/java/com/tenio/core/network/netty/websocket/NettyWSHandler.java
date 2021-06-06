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
package com.tenio.core.network.netty.websocket;

import java.io.IOException;

import com.tenio.common.data.utilities.ZeroDataSerializerUtility;
import com.tenio.common.loggers.SystemLogger;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.data.ServerMessage;
import com.tenio.core.entities.defines.modes.ConnectionDisconnectMode;
import com.tenio.core.entities.defines.modes.PlayerDisconnectMode;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.RefusedConnectionAddressException;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistics.NetworkReaderStatistic;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * Receive all messages sent from clients. It converts serialize data to a
 * system's object for convenience and easy to use. It also handles the logic
 * for the processing of players and connections.
 */
public final class NettyWSHandler extends ChannelInboundHandlerAdapter {

	private final EventManager __eventManager;
	private final SessionManager __sessionManager;
	private final ConnectionFilter __connectionFilter;
	private final NetworkReaderStatistic __networkReaderStatistic;
	private final PrivateLogger __logger;

	public static NettyWSHandler newInstance(EventManager eventManager, SessionManager sessionManager,
			ConnectionFilter connectionFilter, NetworkReaderStatistic networkReaderStatistic) {
		return new NettyWSHandler(eventManager, sessionManager, connectionFilter, networkReaderStatistic);
	}

	private NettyWSHandler(EventManager eventManager, SessionManager sessionManager, ConnectionFilter connectionFilter,
			NetworkReaderStatistic networkReaderStatistic) {
		__eventManager = eventManager;
		__sessionManager = sessionManager;
		__connectionFilter = connectionFilter;
		__networkReaderStatistic = networkReaderStatistic;
		__logger = new PrivateLogger();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		try {
			String address = ctx.channel().remoteAddress().toString();
			__connectionFilter.validateAndAddAddress(address);

			Session session = __sessionManager.createWebSocketSession(ctx.channel());
			__eventManager.emit(ServerEvent.SESSION_CREATED, session);
		} catch (RefusedConnectionAddressException e) {
			__logger.error(e, "Refused connection with address: ", e.getMessage());

			ctx.channel().close();
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Session session = __sessionManager.getSessionByWebSocket(ctx.channel());
		if (session == null) {
			return;
		}

		try {
			session.close(ConnectionDisconnectMode.LOST, PlayerDisconnectMode.CONNECTION_LOST);
		} catch (IOException e) {
			__logger.error(e, "Session: ", session.toString());
			__eventManager.emit(ServerEvent.SESSION_OCCURED_EXCEPTION, session, e);
		} finally {
			session = null;
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msgRaw) throws Exception {
		// only allow this type of frame
		if (msgRaw instanceof BinaryWebSocketFrame) {
			// convert the BinaryWebSocketFrame to bytes' array
			var buffer = ((BinaryWebSocketFrame) msgRaw).content();
			var binary = new byte[buffer.readableBytes()];
			buffer.getBytes(buffer.readerIndex(), binary);
			buffer.release();

			Session session = __sessionManager.getSessionByWebSocket(ctx.channel());

			if (session == null) {
				__logger.debug("WEBSOCKET READ CHANNEL", "Reader handle a null session with the web socket channel: ",
						ctx.channel().toString());
				return;
			}

			session.addReadBytes(binary.length);
			__networkReaderStatistic.updateReadBytes(binary.length);
			__networkReaderStatistic.updateReadPackets(1);

			var data = ZeroDataSerializerUtility.binaryToElement(binary);
			var message = ServerMessage.newInstance().setData(data);

			if (!session.isConnected()) {
				__eventManager.emit(ServerEvent.SESSION_REQUEST_CONNECTION, session, message);
			} else {
				__eventManager.emit(ServerEvent.SESSION_READ_MESSAGE, session, message);
			}

		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Session session = __sessionManager.getSessionByWebSocket(ctx.channel());
		if (session != null) {
			__logger.error(cause, "Session: ", session.toString());
			__eventManager.emit(ServerEvent.SESSION_OCCURED_EXCEPTION, session, cause);
		} else {
			__logger.error(cause, "Exception was occured on channel: %s", ctx.channel().toString());
		}
	}

	private final class PrivateLogger extends SystemLogger {

	}

}
