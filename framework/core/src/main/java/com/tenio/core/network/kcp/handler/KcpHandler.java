/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.kcp.handler;

import com.tenio.common.data.DataCollection;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.result.AccessDatagramChannelResult;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import kcp.KcpListener;
import kcp.Ukcp;

/**
 * Receive all messages sent from clients side. It converts serialize data to a system's object
 * for convenience and easy to use. It also handles the logic for the processing of players and
 * connections.
 */
public class KcpHandler implements KcpListener {

  private final EventManager eventManager;
  private final SessionManager sessionManager;
  private final BinaryPacketDecoder binaryPacketDecoder;
  private final NetworkReaderStatistic networkReaderStatistic;
  private final KcpHandlerPrivateLogger logger;

  /**
   * Constructor.
   *
   * @param eventManager           {@link EventManager} instance
   * @param sessionManager         {@link SessionManager} instance
   * @param binaryPacketDecoder    {@link BinaryPacketDecoder} instance
   * @param networkReaderStatistic {@link NetworkReaderStatistic} instance
   */
  public KcpHandler(EventManager eventManager, SessionManager sessionManager,
                    BinaryPacketDecoder binaryPacketDecoder,
                    NetworkReaderStatistic networkReaderStatistic) {
    this.eventManager = eventManager;
    this.sessionManager = sessionManager;
    this.binaryPacketDecoder = binaryPacketDecoder;
    this.networkReaderStatistic = networkReaderStatistic;
    logger = new KcpHandlerPrivateLogger();
  }

  @Override
  public void onConnected(Ukcp ukcp) {
    if (logger.isDebugEnabled()) {
      logger.debug("KCP CHANNEL CONNECTED", ukcp);
    }
  }

  @Override
  public void handleReceive(ByteBuf byteBuf, Ukcp ukcp) {
    var binaries = new byte[byteBuf.readableBytes()];
    byteBuf.getBytes(byteBuf.readerIndex(), binaries);
    var dataCollection = binaryPacketDecoder.decode(binaries);

    Session session = sessionManager.getSessionByKcp(ukcp);
    if (session == null) {
      processDatagramChannelReadMessageForTheFirstTime(ukcp, dataCollection);
    } else {
      if (session.isActivated()) {
        session.addReadBytes(binaries.length);
        networkReaderStatistic.updateReadBytes(binaries.length);
        networkReaderStatistic.updateReadPackets(1);
        eventManager.emit(ServerEvent.SESSION_READ_MESSAGE, session, dataCollection);
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug("READ KCP CHANNEL", "Session is inactivated: ", session.toString());
        }
      }
    }
  }

  @Override
  public void handleException(Throwable cause, Ukcp ukcp) {
    var session = sessionManager.getSessionByKcp(ukcp);
    if (session != null) {
      if (logger.isErrorEnabled()) {
        logger.error(cause, "Session: ", session.toString());
      }
      eventManager.emit(ServerEvent.SESSION_OCCURRED_EXCEPTION, session, cause);
    } else {
      if (logger.isErrorEnabled()) {
        logger.error(cause, "Exception was occurred on channel: ", ukcp.toString());
      }
    }
  }

  @Override
  public void handleClose(Ukcp ukcp) {
    if (logger.isDebugEnabled()) {
      logger.debug("KCP CHANNEL CLOSED", ukcp);
    }
    var session = sessionManager.getSessionByKcp(ukcp);
    if (session != null && session.containsKcp()) {
      session.setKcpChannel(null);
    }
  }

  private void processDatagramChannelReadMessageForTheFirstTime(Ukcp ukcp, DataCollection message) {
    // verify the kcp channel accessing request
    Object checkingPlayer = null;
    try {
      checkingPlayer =
          eventManager.emit(ServerEvent.ACCESS_KCP_CHANNEL_REQUEST_VALIDATION, message);
    } catch (Exception exception) {
      ukcp.close();
      if (logger.isErrorEnabled()) {
        logger.error(exception, message);
      }
    }

    if (!(checkingPlayer instanceof Optional<?> optionalPlayer)) {
      ukcp.close();
      return;
    }

    if (optionalPlayer.isEmpty()) {
      ukcp.close();
      eventManager.emit(ServerEvent.ACCESS_KCP_CHANNEL_REQUEST_VALIDATION_RESULT,
          null,
          AccessDatagramChannelResult.PLAYER_NOT_FOUND);
    } else {
      Player player = (Player) optionalPlayer.get();
      if (!player.containsSession() || player.getSession().isEmpty()) {
        ukcp.close();
        eventManager.emit(ServerEvent.ACCESS_KCP_CHANNEL_REQUEST_VALIDATION_RESULT,
            player,
            AccessDatagramChannelResult.SESSION_NOT_FOUND);
      } else {
        Session session = player.getSession().get();
        if (!session.isTcp()) {
          ukcp.close();
          eventManager.emit(ServerEvent.ACCESS_KCP_CHANNEL_REQUEST_VALIDATION_RESULT,
              player,
              AccessDatagramChannelResult.INVALID_SESSION_PROTOCOL);
        } else {
          sessionManager.addKcpForSession(ukcp, session);

          eventManager.emit(ServerEvent.ACCESS_KCP_CHANNEL_REQUEST_VALIDATION_RESULT,
              player,
              AccessDatagramChannelResult.SUCCESS);
        }
      }
    }
  }
}

class KcpHandlerPrivateLogger extends SystemLogger {
}
