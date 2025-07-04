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

package com.tenio.core.server.service;

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.DataType;
import com.tenio.common.utility.TimeUtility;
import com.tenio.core.api.ServerApi;
import com.tenio.core.configuration.define.ServerEvent;
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
import com.tenio.core.network.define.RequestPriority;
import com.tenio.core.network.entity.protocol.Request;
import com.tenio.core.network.entity.protocol.implement.DatagramRequestImpl;
import com.tenio.core.network.entity.protocol.implement.SessionRequestImpl;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.manager.DatagramChannelManager;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Objects;
import java.util.Optional;

/**
 * The implementation for the processor service.
 *
 * @see InternalProcessorService
 */
public final class InternalProcessorServiceImpl extends AbstractController
    implements InternalProcessorService {

  private final ServerApi serverApi;
  private final DatagramChannelManager datagramChannelManager;
  private SessionManager sessionManager;
  private PlayerManager playerManager;
  private int maxNumberPlayers;
  private boolean keepPlayerOnDisconnection;

  private InternalProcessorServiceImpl(EventManager eventManager, ServerApi serverApi,
                                       DatagramChannelManager datagramChannelManager) {
    super(eventManager);
    this.serverApi = serverApi;
    this.datagramChannelManager = datagramChannelManager;
  }

  /**
   * Retrieves a new instance of internal processor.
   *
   * @param eventManager           an instance of {@link EventManager}
   * @param serverApi              an instance of {@link ServerApi}
   * @param datagramChannelManager an instance of {@link DatagramChannelManager}
   * @return a new instance of {@link InternalProcessorService}
   */
  public static InternalProcessorServiceImpl newInstance(EventManager eventManager,
                                                         ServerApi serverApi,
                                                         DatagramChannelManager datagramChannelManager) {
    return new InternalProcessorServiceImpl(eventManager, serverApi, datagramChannelManager);
  }

  @Override
  public void initialize() {
    super.initialize();
  }

  @Override
  public void subscribe() {

    eventManager.on(ServerEvent.SESSION_REQUEST_CONNECTION, params -> {
      var request =
          SessionRequestImpl.newInstance().setEvent(ServerEvent.SESSION_REQUEST_CONNECTION);
      request.setSender(params[0]);
      request.setMessage((DataCollection) params[1]);
      request.setPriority(RequestPriority.HIGHEST);
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
      var request = DatagramRequestImpl.newInstance()
              .setEvent(ServerEvent.DATAGRAM_CHANNEL_READ_MESSAGE_FIRST_TIME);
      request.setSender(params[0]);
      request.setRemoteSocketAddress((SocketAddress) params[1]);
      request.setMessage((DataCollection) params[2]);
      request.setPriority(RequestPriority.HIGHEST);
      enqueueRequest(request);

      return null;
    });
  }

  @Override
  public void setDataType(DataType dataType) {
  }

  @Override
  public void processRequest(Request request) {
    switch (request.getEvent()) {
      case SESSION_REQUEST_CONNECTION -> processSessionRequestsConnection(request);
      case SESSION_READ_MESSAGE -> processSessionReadMessage(request);
      case DATAGRAM_CHANNEL_READ_MESSAGE_FIRST_TIME ->
          processDatagramChannelReadMessageForTheFirstTime(request);
      default -> {
        // do nothing
      }
    }
  }

  private void processSessionRequestsConnection(Request request) {
    // Check if it's a reconnection request first
    var session = (Session) request.getSender();

    // We only consider the fresh session
    if (!session.transitionAssociatedState(Session.AssociatedState.NONE, Session.AssociatedState.DOING)) {
      return;
    }

    var message = request.getMessage();

    // When it gets disconnected from client side, the server may not recognise it. In this
    // case, the player is remained on the server side, so we should always check this event
    Object reconnectedObject = null;
    try {
      reconnectedObject = eventManager.emit(ServerEvent.PLAYER_RECONNECT_REQUEST_HANDLE,
          session, message);
    } catch (Exception exception) {
      error(exception, request);
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
              error(exception, "Error while closing old session: ", currentSession);
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
        error(exception, "Session closed with error: ", session.toString());
      }
    } else {
      eventManager.emit(ServerEvent.CONNECTION_ESTABLISHED_RESULT, session, message,
          ConnectionEstablishedResult.SUCCESS);
    }
  }

  private void processSessionWillBeClosed(Session session,
                                                       PlayerDisconnectMode playerDisconnectMode) {
    if (session.isAssociatedToPlayer(Session.AssociatedState.DONE)) {
      var player = playerManager.getPlayerByIdentity(session.getName());
      // the player maybe existed
      if (Objects.nonNull(player)) {
        // unsubscribe it from all channels
        serverApi.unsubscribeFromAllChannels(player);
        // player should leave room (if applicable) first
        if (player.isInRoom()) {
          serverApi.leaveRoom(player, PlayerLeaveRoomMode.DEFAULT);
        }
        eventManager.emit(ServerEvent.DISCONNECT_PLAYER, player, playerDisconnectMode);
        player.setSession(null);
        // When it gets disconnected from client side, the server may not recognise it. In this
        // case, the player is remained on the server side
        if (!keepPlayerOnDisconnection) {
          playerManager.removePlayerByIdentity(player.getIdentity());
          player.clean();
        }
      } else {
        debug("SESSION WILL BE REMOVED", "The player ", session.getName(), " should be presented, but it was not");
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
      var player = playerManager.getPlayerByIdentity(session.getName());
      if (Objects.isNull(player)) {
        var illegalValueException = new IllegalArgumentException(
            String.format("Unable to find player for the session: %s", session));
        error(illegalValueException);
        eventManager.emit(ServerEvent.SERVER_EXCEPTION, illegalValueException);
        return;
      }
      var message = request.getMessage();
      eventManager.emit(ServerEvent.RECEIVED_MESSAGE_FROM_PLAYER, player, message);
    }
  }

  private void processDatagramChannelReadMessageForTheFirstTime(Request request) {
    var message = request.getMessage();

    // verify the datagram channel accessing request
    Object checkingPlayer = null;
    try {
      checkingPlayer = eventManager.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION,
          message);
    } catch (Exception exception) {
      error(exception, request);
    }

    if (!(checkingPlayer instanceof Optional<?> optionalPlayer)) {
      return;
    }

    if (optionalPlayer.isEmpty()) {
      eventManager.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT,
          null,
          Session.EMPTY_DATAGRAM_CONVEY_ID,
          AccessDatagramChannelResult.PLAYER_NOT_FOUND);
    } else {
      Player player = (Player) optionalPlayer.get();
      if (!player.containsSession() || player.getSession().isEmpty()) {
        eventManager.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT,
            player,
            Session.EMPTY_DATAGRAM_CONVEY_ID,
            AccessDatagramChannelResult.SESSION_NOT_FOUND);
      } else {
        Session session = player.getSession().get();
        if (!session.isTcp()) {
          eventManager.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT,
              player,
              Session.EMPTY_DATAGRAM_CONVEY_ID,
              AccessDatagramChannelResult.INVALID_SESSION_PROTOCOL);
        } else {
          var udpConvey = datagramChannelManager.getCurrentUdpConveyId();
          var datagramChannel = (DatagramChannel) request.getSender();

          session.setDatagramRemoteSocketAddress(request.getRemoteSocketAddress());
          sessionManager.addDatagramForSession(datagramChannel, udpConvey, session);

          eventManager.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT,
              player,
              udpConvey,
              AccessDatagramChannelResult.SUCCESS);
        }
      }
    }
  }

  private long now() {
    return TimeUtility.currentTimeMillis();
  }

  @Override
  public String getName() {
    return "internal-processor";
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
