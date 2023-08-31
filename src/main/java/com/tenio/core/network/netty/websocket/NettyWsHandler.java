/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

import com.tenio.common.data.DataType;
import com.tenio.common.data.DataUtility;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.RefusedConnectionAddressException;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Receive all messages sent from clients side. It converts serialize data to a system's object
 * for convenience and easy to use. It also handles the logic for the processing of players and
 * connections.
 */
public final class NettyWsHandler extends ChannelInboundHandlerAdapter {

  private final EventManager eventManager;
  private final SessionManager sessionManager;
  private final ConnectionFilter connectionFilter;
  private final NetworkReaderStatistic networkReaderStatistic;
  private final DataType dataType;
  private final PrivateLogger logger;

  private NettyWsHandler(EventManager eventManager, SessionManager sessionManager,
                         ConnectionFilter connectionFilter, DataType dataType,
                         NetworkReaderStatistic networkReaderStatistic) {
    this.eventManager = eventManager;
    this.sessionManager = sessionManager;
    this.connectionFilter = connectionFilter;
    this.dataType = dataType;
    this.networkReaderStatistic = networkReaderStatistic;
    logger = new PrivateLogger();
  }

  /**
   * Creates a new instance of the websocket handler.
   *
   * @param eventManager           the instance of {@link EventManager}
   * @param sessionManager         the instance of {@link SessionManager}
   * @param connectionFilter       the instance of {@link ConnectionFilter}
   * @param dataType               the {@link DataType}
   * @param networkReaderStatistic the instance of {@link NetworkReaderStatistic}
   * @return a new instance of {@link NettyWsHandler}
   */
  public static NettyWsHandler newInstance(EventManager eventManager, SessionManager sessionManager,
                                           ConnectionFilter connectionFilter, DataType dataType,
                                           NetworkReaderStatistic networkReaderStatistic) {
    return new NettyWsHandler(eventManager, sessionManager, connectionFilter, dataType,
        networkReaderStatistic);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    var session = sessionManager.getSessionByWebSocket(ctx.channel());
    if (Objects.isNull(session)) {
      return;
    }

    try {
      session.close(ConnectionDisconnectMode.LOST, PlayerDisconnectMode.CONNECTION_LOST);
    } catch (IOException exception) {
      if (logger.isErrorEnabled()) {
        logger.error(exception, "Session: ", session.toString());
      }
      eventManager.emit(ServerEvent.SESSION_OCCURRED_EXCEPTION, session, exception);
    }
  }

  @Override
  public void channelRead(@Nonnull ChannelHandlerContext ctx, @Nonnull Object raw) {
    // only allow this type of frame
    if (raw instanceof BinaryWebSocketFrame) {
      // convert the BinaryWebSocketFrame to bytes' array
      var buffer = ((BinaryWebSocketFrame) raw).content();
      var binary = new byte[buffer.readableBytes()];
      buffer.getBytes(buffer.readerIndex(), binary);
      buffer.release();

      var session = sessionManager.getSessionByWebSocket(ctx.channel());

      if (Objects.isNull(session)) {
        try {
          var address = ctx.channel().remoteAddress().toString();
          connectionFilter.validateAndAddAddress(address);
        } catch (RefusedConnectionAddressException exception) {
          if (logger.isErrorEnabled()) {
            logger.error(exception, "Refused connection with address: ", exception.getMessage());
          }
          // handle refused connection, it should send to the client the reason before closing connection
          eventManager.emit(ServerEvent.WEBSOCKET_CONNECTION_REFUSED, ctx.channel(), exception);
          ctx.channel().close();
        }

        session = sessionManager.createWebSocketSession(ctx.channel());
        session.activate();
      }

      if (!session.isActivated()) {
        if (logger.isDebugEnabled()) {
          logger.debug("READ WEBSOCKET CHANNEL", "Session is inactivated: ", session.toString());
        }
        return;
      }

      if (session.isAssociatedToPlayer(Session.AssociatedState.DOING)) {
        if (logger.isDebugEnabled()) {
          logger.debug("READ WEBSOCKET CHANNEL", "Session is associating to a player, rejects " +
              "message: ", session.toString());
        }
        return;
      }

      session.addReadBytes(binary.length);
      networkReaderStatistic.updateReadBytes(binary.length);
      networkReaderStatistic.updateReadPackets(1);

      var message = DataUtility.binaryToCollection(dataType, binary);

      if (session.isAssociatedToPlayer(Session.AssociatedState.NONE)) {
        eventManager.emit(ServerEvent.SESSION_REQUEST_CONNECTION, session, message);
      } else if (session.isAssociatedToPlayer(Session.AssociatedState.DONE)) {
        eventManager.emit(ServerEvent.SESSION_READ_MESSAGE, session, message);
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    var session = sessionManager.getSessionByWebSocket(ctx.channel());
    if (Objects.nonNull(session)) {
      if (logger.isErrorEnabled()) {
        logger.error(cause, "Session: ", session.toString());
      }
      eventManager.emit(ServerEvent.SESSION_OCCURRED_EXCEPTION, session, cause);
    } else {
      if (logger.isErrorEnabled()) {
        logger.error(cause, "Exception was occurred on channel: %s", ctx.channel().toString());
      }
    }
  }
}

class PrivateLogger extends SystemLogger {
}
