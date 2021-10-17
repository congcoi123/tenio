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

import com.tenio.common.data.utility.ZeroDataSerializerUtility;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.RefusedConnectionAddressException;
import com.tenio.core.network.entity.session.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import java.io.IOException;

/**
 * Receive all messages sent from clients. It converts serialize data to a
 * system's object for convenience and easy to use. It also handles the logic
 * for the processing of players and connections.
 */
public final class NettyWsHandler extends ChannelInboundHandlerAdapter {

  private final EventManager eventManager;
  private final SessionManager sessionManager;
  private final ConnectionFilter connectionFilter;
  private final NetworkReaderStatistic networkReaderStatistic;
  private final PrivateLogger logger;

  private NettyWsHandler(EventManager eventManager, SessionManager sessionManager,
                         ConnectionFilter connectionFilter,
                         NetworkReaderStatistic networkReaderStatistic) {
    this.eventManager = eventManager;
    this.sessionManager = sessionManager;
    this.connectionFilter = connectionFilter;
    this.networkReaderStatistic = networkReaderStatistic;
    logger = new PrivateLogger();
  }

  public static NettyWsHandler newInstance(EventManager eventManager, SessionManager sessionManager,
                                           ConnectionFilter connectionFilter,
                                           NetworkReaderStatistic networkReaderStatistic) {
    return new NettyWsHandler(eventManager, sessionManager, connectionFilter,
        networkReaderStatistic);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    try {
      var address = ctx.channel().remoteAddress().toString();
      connectionFilter.validateAndAddAddress(address);

      var session = sessionManager.createWebSocketSession(ctx.channel());
      eventManager.emit(ServerEvent.SESSION_CREATED, session);
    } catch (RefusedConnectionAddressException e) {
      logger.error(e, "Refused connection with address: ", e.getMessage());

      ctx.channel().close();
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    var session = sessionManager.getSessionByWebSocket(ctx.channel());
    if (session == null) {
      return;
    }

    try {
      session.close(ConnectionDisconnectMode.LOST, PlayerDisconnectMode.CONNECTION_LOST);
    } catch (IOException e) {
      logger.error(e, "Session: ", session.toString());
      eventManager.emit(ServerEvent.SESSION_OCCURRED_EXCEPTION, session, e);
    }
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msgRaw) {
    // only allow this type of frame
    if (msgRaw instanceof BinaryWebSocketFrame) {
      // convert the BinaryWebSocketFrame to bytes' array
      var buffer = ((BinaryWebSocketFrame) msgRaw).content();
      var binary = new byte[buffer.readableBytes()];
      buffer.getBytes(buffer.readerIndex(), binary);
      buffer.release();

      var session = sessionManager.getSessionByWebSocket(ctx.channel());

      if (session == null) {
        logger.debug("WEBSOCKET READ CHANNEL",
            "Reader handle a null session with the web socket channel: ",
            ctx.channel().toString());
        return;
      }

      session.addReadBytes(binary.length);
      networkReaderStatistic.updateReadBytes(binary.length);
      networkReaderStatistic.updateReadPackets(1);

      var data = ZeroDataSerializerUtility.binaryToElement(binary);
      var message = ServerMessage.newInstance().setData(data);

      if (!session.isConnected()) {
        eventManager.emit(ServerEvent.SESSION_REQUEST_CONNECTION, session, message);
      } else {
        eventManager.emit(ServerEvent.SESSION_READ_MESSAGE, session, message);
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    var session = sessionManager.getSessionByWebSocket(ctx.channel());
    if (session != null) {
      logger.error(cause, "Session: ", session.toString());
      eventManager.emit(ServerEvent.SESSION_OCCURRED_EXCEPTION, session, cause);
    } else {
      logger.error(cause, "Exception was occured on channel: %s", ctx.channel().toString());
    }
  }

  private final class PrivateLogger extends SystemLogger {
  }
}
