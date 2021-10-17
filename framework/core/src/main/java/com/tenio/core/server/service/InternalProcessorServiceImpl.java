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

package com.tenio.core.server.service;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.controller.AbstractController;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.entity.define.result.AttachedConnectionResult;
import com.tenio.core.entity.define.result.ConnectionEstablishedResult;
import com.tenio.core.entity.define.result.PlayerReconnectedResult;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.protocol.Request;
import com.tenio.core.network.entity.protocol.implement.RequestImpl;
import com.tenio.core.network.entity.session.Session;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * The implementation for the processor service.
 *
 * @see InternalProcessorService
 */
public final class InternalProcessorServiceImpl extends AbstractController
    implements InternalProcessorService {

  private static final String EVENT_KEY_DATAGRAM_CHANNEL = "datagram-channel";
  private static final String EVENT_KEY_CONNECTION_DISCONNECTED_MODE =
      "connection-disconnected-mode";
  private static final String EVENT_KEY_PLAYER_DISCONNECTED_MODE = "player-disconnected-mode";
  private static final String EVENT_KEY_SERVER_MESSAGE = "server-message";
  private static final String EVENT_KEY_DATAGRAM_REMOTE_ADDRESS = "datagram-remote-address";

  private PlayerManager playerManager;
  private int maxNumberPlayers;
  private boolean keepPlayerOnDisconnection;

  private InternalProcessorServiceImpl(EventManager eventManager) {
    super(eventManager);
  }

  public static InternalProcessorServiceImpl newInstance(EventManager eventManager) {
    return new InternalProcessorServiceImpl(eventManager);
  }

  @Override
  public void subscribe() {

    eventManager.on(ServerEvent.SESSION_CREATED, params -> {
      // do nothing
      return null;
    });

    eventManager.on(ServerEvent.SESSION_REQUEST_CONNECTION, params -> {
      var request =
          createRequest(ServerEvent.SESSION_REQUEST_CONNECTION, (Session) params[0]);
      request.setAttribute(EVENT_KEY_SERVER_MESSAGE, params[1]);
      enqueueRequest(request);

      return null;
    });

    eventManager.on(ServerEvent.SESSION_OCCURRED_EXCEPTION, params -> {
      eventManager.emit(ServerEvent.SERVER_EXCEPTION, params);
      return null;
    });

    eventManager.on(ServerEvent.SESSION_WILL_BE_CLOSED, params -> {
      var request = createRequest(ServerEvent.SESSION_WILL_BE_CLOSED, (Session) params[0]);
      request.setAttribute(EVENT_KEY_CONNECTION_DISCONNECTED_MODE, params[1]);
      request.setAttribute(EVENT_KEY_PLAYER_DISCONNECTED_MODE, params[2]);
      enqueueRequest(request);

      return null;
    });

    eventManager.on(ServerEvent.SESSION_READ_MESSAGE, params -> {
      var request = createRequest(ServerEvent.SESSION_READ_MESSAGE, (Session) params[0]);
      request.setAttribute(EVENT_KEY_SERVER_MESSAGE, params[1]);
      enqueueRequest(request);

      return null;
    });

    eventManager.on(ServerEvent.DATAGRAM_CHANNEL_READ_MESSAGE, params -> {
      var request = createRequest(ServerEvent.DATAGRAM_CHANNEL_READ_MESSAGE, null);
      request.setAttribute(EVENT_KEY_DATAGRAM_CHANNEL, params[0]);
      request.setAttribute(EVENT_KEY_DATAGRAM_REMOTE_ADDRESS, params[1]);
      request.setAttribute(EVENT_KEY_SERVER_MESSAGE, params[2]);
      enqueueRequest(request);

      return null;
    });
  }

  private Request createRequest(ServerEvent event, Session session) {
    var request = RequestImpl.newInstance().setEvent(event).setSender(session);
    return request;
  }

  @Override
  public void processRequest(Request request) {
    switch (request.getEvent()) {
      case SESSION_REQUEST_CONNECTION:
        processSessionRequestsConnection(request);
        break;

      case SESSION_WILL_BE_CLOSED:
        processSessionWillBeClosed(request);
        break;

      case SESSION_READ_MESSAGE:
        processSessionReadMessage(request);
        break;

      case DATAGRAM_CHANNEL_READ_MESSAGE:
        processDatagramChannelReadMessage(request);
        break;

      default:
        break;
    }
  }

  private void processSessionRequestsConnection(Request request) {
    // check if it's reconnection request first
    var session = request.getSender();
    var message = request.getAttribute(EVENT_KEY_SERVER_MESSAGE);

    Player player = null;

    if (keepPlayerOnDisconnection) {
      player =
          (Player) eventManager.emit(ServerEvent.PLAYER_RECONNECT_REQUEST_HANDLE, session, message);
    }

    // check reconnected case
    if (player != null) {
      if (!(player instanceof Player)) {
        var castException = new ClassCastException(
            String.format("Unable to cast the object: %s to class Player", player.toString()));
        error(castException);
        eventManager.emit(ServerEvent.SERVER_EXCEPTION, castException);
        eventManager.emit(ServerEvent.PLAYER_RECONNECTED_RESULT, player,
            PlayerReconnectedResult.INVALID_PLAYER_FORMAT);
      } else {
        session.setName(player.getName());
        player.setSession(session);
        eventManager.emit(ServerEvent.PLAYER_RECONNECTED_RESULT, player,
            PlayerReconnectedResult.SUCCESS);
      }
      // check new reconnection
    } else {
      // check the number of current players
      if (playerManager.getPlayerCount() >= maxNumberPlayers) {
        eventManager.emit(ServerEvent.CONNECTION_ESTABLISHED_RESULT, session, message,
            ConnectionEstablishedResult.REACHED_MAX_CONNECTION);
        try {
          session.close(ConnectionDisconnectMode.REACHED_MAX_CONNECTION,
              PlayerDisconnectMode.CONNECTION_LOST);
        } catch (IOException e) {
          error(e, "Session closed with error: ", session.toString());
        }
      } else {
        eventManager.emit(ServerEvent.CONNECTION_ESTABLISHED_RESULT, session, message,
            ConnectionEstablishedResult.SUCCESS);
      }
    }
  }

  private void processSessionWillBeClosed(Request request) {
    var session = request.getSender();
    var connectionClosedMode = (ConnectionDisconnectMode) request
        .getAttribute(EVENT_KEY_CONNECTION_DISCONNECTED_MODE);
    var playerClosedMode =
        (PlayerDisconnectMode) request.getAttribute(EVENT_KEY_PLAYER_DISCONNECTED_MODE);

    var player = playerManager.getPlayerBySession(session);
    // the player maybe existed
    if (player != null) {
      eventManager.emit(ServerEvent.DISCONNECT_PLAYER, player, playerClosedMode);
      player.setSession(null);
      if (!keepPlayerOnDisconnection) {
        playerManager.removePlayerByName(player.getName());
        player.clean();
      }
    }
    eventManager.emit(ServerEvent.DISCONNECT_CONNECTION, session, connectionClosedMode);
  }

  private void processSessionReadMessage(Request request) {
    var session = request.getSender();

    var player = playerManager.getPlayerBySession(session);
    if (player == null) {
      var illegalValueException = new IllegalArgumentException(
          String.format("Unable to find player for the session: %s", session.toString()));
      error(illegalValueException);
      eventManager.emit(ServerEvent.SERVER_EXCEPTION, illegalValueException);

      return;
    }

    var message = request.getAttribute(EVENT_KEY_SERVER_MESSAGE);

    eventManager.emit(ServerEvent.RECEIVED_MESSAGE_FROM_PLAYER, player, message);
  }

  private void processDatagramChannelReadMessage(Request request) {
    var datagramChannel = request.getAttribute(EVENT_KEY_DATAGRAM_CHANNEL);
    var remoteAddress = request.getAttribute(EVENT_KEY_DATAGRAM_REMOTE_ADDRESS);
    var message = request.getAttribute(EVENT_KEY_SERVER_MESSAGE);

    // the condition for creating sub-connection
    var player =
        (Player) eventManager.emit(ServerEvent.ATTACH_CONNECTION_REQUEST_VALIDATION, message);

    if (player == null) {
      eventManager.emit(ServerEvent.ATTACHED_CONNECTION_RESULT, null,
          AttachedConnectionResult.PLAYER_NOT_FOUND);
    } else if (!player.containsSession()) {
      eventManager.emit(ServerEvent.ATTACHED_CONNECTION_RESULT, player,
          AttachedConnectionResult.SESSION_NOT_FOUND);
    } else if (!player.getSession().isTcp()) {
      eventManager.emit(ServerEvent.ATTACHED_CONNECTION_RESULT, player,
          AttachedConnectionResult.INVALID_SESSION_PROTOCOL);
    } else {
      var session = player.getSession();
      var sessionManager = session.getSessionManager();
      sessionManager.addDatagramForSession((DatagramChannel) datagramChannel,
          (SocketAddress) remoteAddress,
          session);
      eventManager.emit(ServerEvent.ATTACHED_CONNECTION_RESULT, player,
          AttachedConnectionResult.SUCCESS);
    }
  }

  @Override
  public String getName() {
    return "internal";
  }

  @Override
  public void setMaxNumberPlayers(int maxPlayers) {
    maxNumberPlayers = maxPlayers;
  }

  @Override
  public void setKeepPlayerOnDisconnection(boolean keep) {
    keepPlayerOnDisconnection = keep;
  }

  @Override
  public void setPlayerManager(PlayerManager playerManager) {
    this.playerManager = playerManager;
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
