/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.core.server.service;

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.DataType;
import com.tenio.common.utility.TimeUtility;
import com.tenio.core.api.ServerApi;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.configuration.kcp.KcpConfiguration;
import com.tenio.core.controller.AbstractController;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerLeaveRoomMode;
import com.tenio.core.entity.define.result.AccessDatagramChannelResult;
import com.tenio.core.entity.define.result.ConnectionEstablishedResult;
import com.tenio.core.entity.define.result.PlayerReconnectedResult;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.kcp.Ukcp;
import com.tenio.core.network.entity.protocol.Request;
import com.tenio.core.network.entity.protocol.implement.DatagramRequestImpl;
import com.tenio.core.network.entity.protocol.implement.SessionRequestImpl;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.writer.implement.KcpWriterHandler;
import com.tenio.core.network.zero.handler.KcpIoHandler;
import com.tenio.core.network.zero.handler.implement.KcpIoHandlerImpl;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The implementation for the processor service.
 *
 * @see InternalProcessorService
 */
public final class InternalProcessorServiceImpl extends AbstractController
    implements InternalProcessorService {

  private final ServerApi serverApi;
  private NetworkWriterStatistic networkWriterStatistic;
  private DataType dataType;
  private SessionManager sessionManager;
  private PlayerManager playerManager;
  private KcpIoHandler kcpIoHandler;
  private AtomicInteger udpConvId;
  private AtomicInteger kcpConvId;
  private boolean enabledUdp;
  private boolean enabledKcp;
  private int maxNumberPlayers;
  private boolean keepPlayerOnDisconnection;

  private InternalProcessorServiceImpl(EventManager eventManager, ServerApi serverApi) {
    super(eventManager);
    this.serverApi = serverApi;
  }

  /**
   * Retrieves a new instance of internal processor.
   *
   * @param eventManager an instance of {@link EventManager}
   * @param serverApi    an instance of {@link ServerApi}
   * @return a new instance of {@link InternalProcessorService}
   */
  public static InternalProcessorServiceImpl newInstance(EventManager eventManager,
                                                         ServerApi serverApi) {
    return new InternalProcessorServiceImpl(eventManager, serverApi);
  }

  @Override
  public void initialize() {
    super.initialize();
    if (enabledUdp) {
      udpConvId = new AtomicInteger(0);
      if (enabledKcp) {
        kcpIoHandler = KcpIoHandlerImpl.newInstance(eventManager);
        kcpIoHandler.setDataType(dataType);
        kcpConvId = new AtomicInteger(0);
      }
    }
  }

  @Override
  public void subscribe() {

    eventManager.on(ServerEvent.SESSION_REQUEST_CONNECTION, params -> {
      var request =
          SessionRequestImpl.newInstance().setEvent(ServerEvent.SESSION_REQUEST_CONNECTION);
      request.setSender(params[0]);
      request.setMessage((DataCollection) params[1]);
      enqueueRequest(request);

      return null;
    });

    eventManager.on(ServerEvent.SESSION_OCCURRED_EXCEPTION, params -> {
      eventManager.emit(ServerEvent.SERVER_EXCEPTION, params);

      return null;
    });

    eventManager.on(ServerEvent.SESSION_WILL_BE_CLOSED, params -> {
      processSessionWillBeClosed((Session) params[0],
          (PlayerDisconnectMode) params[2]);

      return null;
    });

    eventManager.on(ServerEvent.SESSION_READ_MESSAGE, params -> {
      var session = (Session) params[0];
      var request =
          SessionRequestImpl.newInstance().setEvent(ServerEvent.SESSION_READ_MESSAGE);
      request.setSender(session);
      request.setMessage((DataCollection) params[1]);
      session.setLastReadTime(TimeUtility.currentTimeMillis());
      session.increaseReadMessages();
      enqueueRequest(request);

      return null;
    });

    eventManager.on(ServerEvent.DATAGRAM_CHANNEL_READ_MESSAGE_FIRST_TIME, params -> {
      var request =
          DatagramRequestImpl.newInstance()
              .setEvent(ServerEvent.DATAGRAM_CHANNEL_READ_MESSAGE_FIRST_TIME);
      request.setSender(params[0]);
      request.setRemoteSocketAddress((SocketAddress) params[1]);
      request.setMessage((DataCollection) params[2]);
      enqueueRequest(request);

      return null;
    });
  }

  @Override
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  @Override
  public void processRequest(Request request) {
    switch (request.getEvent()) {
      case SESSION_REQUEST_CONNECTION -> processSessionRequestsConnection(request);
      case SESSION_READ_MESSAGE -> processSessionReadMessage(request);
      case DATAGRAM_CHANNEL_READ_MESSAGE_FIRST_TIME -> processDatagramChannelReadMessageForTheFirstTime(
          request);
      default -> {
        // do nothing
      }
    }
  }

  private synchronized void processSessionRequestsConnection(Request request) {
    // Check if it's a reconnection request first
    var session = (Session) request.getSender();
    // We only consider the fresh session
    if (!session.isAssociatedToPlayer(Session.AssociatedState.NONE)) {
      return;
    }

    // Processing
    session.setAssociatedToPlayer(Session.AssociatedState.DOING);

    var message = request.getMessage();

    // When it gets disconnected from client side, the server may not recognise it. In this
    // case, the player is remained on the server side, so we should always check this event
    Object reconnectedObject = null;
    try {
      reconnectedObject = eventManager.emit(ServerEvent.PLAYER_RECONNECT_REQUEST_HANDLE,
          session, message);
    } catch (Exception exception) {
      if (isErrorEnabled()) {
        error(exception, request);
      }
    }

    // check reconnected case
    if (Objects.nonNull(reconnectedObject)) {
      Optional<?> playerOptional = (Optional<?>) reconnectedObject;
      if (playerOptional.isPresent()) {
        Player player = (Player) playerOptional.get();
        Optional<Session> optionalSession = player.getSession();
        if (optionalSession.isPresent()) {
          Session currentSession = optionalSession.get();
          // This is to ensure that by any chance, the current session is not the one who is
          // trying to reconnect
          if (currentSession.isActivated() && !currentSession.equals(session)) {
            try {
              // Detach the current session from its player
              currentSession.setName("STALE-" + currentSession.getName());
              currentSession.setAssociatedToPlayer(Session.AssociatedState.NONE);
              currentSession.close(ConnectionDisconnectMode.RECONNECTION,
                  PlayerDisconnectMode.RECONNECTION);
            } catch (IOException exception) {
              if (isErrorEnabled()) {
                error(exception, "Error while closing old session: ", currentSession);
              }
            }
          }
        }

        // In case the server does not want to hold the player when he gets disconnected, the
        // player should be removed from his current room
        // In case the server wants to keep this player, all the current information (room
        // related data) has to be sent to client to get up-to-date
        if (!keepPlayerOnDisconnection) {
          // player should leave room (if applicable) first
          if (player.isInRoom()) {
            serverApi.leaveRoom(player, PlayerLeaveRoomMode.DEFAULT);
          }
        }

        // connect the player to a new session
        player.setSession(session);
        player.setLastReadTime(now());
        player.setLastWriteTime(now());
        player.setLastActivityTime(now());
        eventManager.emit(ServerEvent.PLAYER_RECONNECTED_RESULT, player, session,
            PlayerReconnectedResult.SUCCESS);
      } else {
        establishNewPlayerConnection(session, message);
      }
    } else {
      establishNewPlayerConnection(session, message);
    }
  }

  private void establishNewPlayerConnection(Session session, DataCollection message) {
    // check the number of current players
    if (playerManager.getPlayerCount() >= maxNumberPlayers) {
      eventManager.emit(ServerEvent.CONNECTION_ESTABLISHED_RESULT, session, message,
          ConnectionEstablishedResult.REACHED_MAX_CONNECTION);
      try {
        session.close(ConnectionDisconnectMode.REACHED_MAX_CONNECTION,
            PlayerDisconnectMode.CONNECTION_LOST);
      } catch (IOException exception) {
        if (isErrorEnabled()) {
          error(exception, "Session closed with error: ", session.toString());
        }
      }
    } else {
      eventManager.emit(ServerEvent.CONNECTION_ESTABLISHED_RESULT, session, message,
          ConnectionEstablishedResult.SUCCESS);
    }

  }

  private synchronized void processSessionWillBeClosed(Session session,
                                                       PlayerDisconnectMode playerDisconnectMode) {
    if (session.isAssociatedToPlayer(Session.AssociatedState.DONE)) {
      var player = playerManager.getPlayerByName(session.getName());
      // the player maybe existed
      if (Objects.nonNull(player)) {
        // player should leave room (if applicable) first
        if (player.isInRoom()) {
          serverApi.leaveRoom(player, PlayerLeaveRoomMode.DEFAULT);
        }
        eventManager.emit(ServerEvent.DISCONNECT_PLAYER, player, playerDisconnectMode);
        player.setSession(null);
        // When it gets disconnected from client side, the server may not recognise it. In this
        // case, the player is remained on the server side
        if (!keepPlayerOnDisconnection) {
          playerManager.removePlayerByName(player.getName());
          player.clean();
        }
      } else {
        if (isDebugEnabled()) {
          debug("SESSION WILL BE REMOVED", "The player ", session.getName(), " should be " +
              "presented, but it was not");
        }
      }
    }
    session.setName(null);
    session.setAssociatedToPlayer(Session.AssociatedState.NONE);
    session.remove();
  }

  // In this phase, the session must be bound with a player, a free session can only be accepted
  // when it is being handled in the connection established phase
  private void processSessionReadMessage(Request request) {
    var session = (Session) request.getSender();

    if (session.isAssociatedToPlayer(Session.AssociatedState.DONE)) {
      var player = playerManager.getPlayerByName(session.getName());
      if (Objects.isNull(player)) {
        var illegalValueException = new IllegalArgumentException(
            String.format("Unable to find player for the session: %s", session));
        if (isErrorEnabled()) {
          error(illegalValueException);
        }
        eventManager.emit(ServerEvent.SERVER_EXCEPTION, illegalValueException);
        return;
      }
      var message = request.getMessage();
      eventManager.emit(ServerEvent.RECEIVED_MESSAGE_FROM_PLAYER, player, message);
    }
  }

  private synchronized void processDatagramChannelReadMessageForTheFirstTime(Request request) {
    var message = request.getMessage();

    // verify the datagram channel accessing request
    Object checkingPlayer = null;
    try {
      checkingPlayer = eventManager.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION,
          message);
    } catch (Exception exception) {
      if (isErrorEnabled()) {
        error(exception, request);
      }
    }

    if (!(checkingPlayer instanceof Optional<?> optionalPlayer)) {
      return;
    }

    if (optionalPlayer.isEmpty()) {
      eventManager.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT,
          optionalPlayer,
          Session.EMPTY_DATAGRAM_CONVEY_ID, Session.EMPTY_DATAGRAM_CONVEY_ID,
          AccessDatagramChannelResult.PLAYER_NOT_FOUND);
    } else {
      Player player = (Player) optionalPlayer.get();
      if (!player.containsSession() || player.getSession().isEmpty()) {
        eventManager.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT,
            optionalPlayer,
            Session.EMPTY_DATAGRAM_CONVEY_ID, Session.EMPTY_DATAGRAM_CONVEY_ID,
            AccessDatagramChannelResult.SESSION_NOT_FOUND);
      } else {
        Session session = player.getSession().get();
        if (!session.isTcp()) {
          eventManager.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT,
              optionalPlayer,
              Session.EMPTY_DATAGRAM_CONVEY_ID, Session.EMPTY_DATAGRAM_CONVEY_ID,
              AccessDatagramChannelResult.INVALID_SESSION_PROTOCOL);
        } else {
          var udpConvey = udpConvId.getAndIncrement();
          var datagramChannel = (DatagramChannel) request.getSender();

          var sessionInstance = ((Player) optionalPlayer.get()).getSession().get();
          sessionInstance.setDatagramRemoteSocketAddress(request.getRemoteSocketAddress());
          sessionManager.addDatagramForSession(datagramChannel, udpConvey, sessionInstance);

          if (enabledKcp) {
            if (sessionInstance.containsKcp()) {
              // TODO: reconnect

            } else {
              // initialize a KCP connection
              initializeKcp(sessionInstance, Optional.of(player), udpConvey);
            }
          } else {
            eventManager.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT,
                optionalPlayer,
                udpConvey,
                Session.EMPTY_DATAGRAM_CONVEY_ID, AccessDatagramChannelResult.SUCCESS);
          }
        }
      }
    }
  }

  private void initializeKcp(Session session, Optional<Player> optionalPlayer, int udpConvey) {
    var kcpWriter = new KcpWriterHandler(session.getDatagramChannel(),
        session.getDatagramRemoteSocketAddress());
    var kcpConv = kcpConvId.getAndIncrement();
    var ukcp = new Ukcp(kcpConv, KcpConfiguration.PROFILE, session, kcpIoHandler, kcpWriter,
        networkWriterStatistic);
    ukcp.getKcpIoHandler().channelActiveIn(session);

    eventManager.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT, optionalPlayer,
        udpConvey, kcpConv, AccessDatagramChannelResult.SUCCESS);
  }

  private long now() {
    return TimeUtility.currentTimeMillis();
  }

  @Override
  public String getName() {
    return "internal";
  }

  @Override
  public void setMaxNumberPlayers(int maxNumberPlayers) {
    this.maxNumberPlayers = maxNumberPlayers;
  }

  @Override
  public void setKeepPlayerOnDisconnection(boolean keepPlayerOnDisconnection) {
    this.keepPlayerOnDisconnection = keepPlayerOnDisconnection;
  }

  @Override
  public void setEnabledUdp(boolean enabledUdp) {
    this.enabledUdp = enabledUdp;
  }

  @Override
  public void setEnabledKcp(boolean enabledKcp) {
    this.enabledKcp = enabledKcp;
  }

  @Override
  public void setSessionManager(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @Override
  public void setPlayerManager(PlayerManager playerManager) {
    this.playerManager = playerManager;
  }

  @Override
  public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
    // do nothing
  }

  @Override
  public void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic) {
    this.networkWriterStatistic = networkWriterStatistic;
  }

  @Override
  public void onInitialized() {
    // do nothing
  }

  @Override
  public void onStarted() {
    // do nothing
  }

  @Override
  public void onRunning() {
    // do nothing
  }

  @Override
  public void onShutdown() {
    // do nothing
  }

  @Override
  public void onDestroyed() {
    // do nothing
  }
}
