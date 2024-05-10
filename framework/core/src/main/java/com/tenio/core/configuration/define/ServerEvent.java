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

package com.tenio.core.configuration.define;

import com.tenio.core.bootstrap.annotation.Asynchronous;
import com.tenio.core.handler.event.EventAccessDatagramChannelRequestValidation;
import com.tenio.core.handler.event.EventAccessDatagramChannelRequestValidationResult;
import com.tenio.core.handler.event.EventAccessKcpChannelRequestValidation;
import com.tenio.core.handler.event.EventAccessKcpChannelRequestValidationResult;
import com.tenio.core.handler.event.EventConnectionEstablishedResult;
import com.tenio.core.handler.event.EventDisconnectPlayer;
import com.tenio.core.handler.event.EventFetchedBandwidthInfo;
import com.tenio.core.handler.event.EventFetchedCcuInfo;
import com.tenio.core.handler.event.EventPlayerAfterLeftRoom;
import com.tenio.core.handler.event.EventPlayerBeforeLeaveRoom;
import com.tenio.core.handler.event.EventPlayerJoinedRoomResult;
import com.tenio.core.handler.event.EventPlayerLoggedinResult;
import com.tenio.core.handler.event.EventPlayerReconnectRequestHandle;
import com.tenio.core.handler.event.EventPlayerReconnectedResult;
import com.tenio.core.handler.event.EventReceivedMessageFromPlayer;
import com.tenio.core.handler.event.EventRoomCreatedResult;
import com.tenio.core.handler.event.EventRoomWillBeRemoved;
import com.tenio.core.handler.event.EventSendMessageToPlayer;
import com.tenio.core.handler.event.EventServerException;
import com.tenio.core.handler.event.EventServerInitialization;
import com.tenio.core.handler.event.EventServerTeardown;
import com.tenio.core.handler.event.EventSocketConnectionRefused;
import com.tenio.core.handler.event.EventSwitchParticipantToSpectatorResult;
import com.tenio.core.handler.event.EventSwitchSpectatorToParticipantResult;
import com.tenio.core.handler.event.EventSystemMonitoring;
import com.tenio.core.handler.event.EventWebSocketConnectionRefused;
import com.tenio.core.handler.event.EventWriteMessageToConnection;
import com.tenio.core.network.zero.engine.implement.ZeroReaderImpl;

/**
 * All supported events could be emitted on the server.
 */
public enum ServerEvent {

  /**
   * When an incoming connection is refused to establish a session on the server.
   *
   * @see EventSocketConnectionRefused
   * @since 0.3.1
   */
  SOCKET_CONNECTION_REFUSED,
  /**
   * When an incoming connection is refused to establish a session on the server.
   *
   * @see EventWebSocketConnectionRefused
   * @since 0.3.1
   */
  WEBSOCKET_CONNECTION_REFUSED,
  /**
   * When a new session requests to connect to the server.
   */
  @Asynchronous
  SESSION_REQUEST_CONNECTION,
  /**
   * When there is any issue occurs to a session.
   */
  SESSION_OCCURRED_EXCEPTION,
  /**
   * When a session is going to disconnect to the server.
   */
  SESSION_WILL_BE_CLOSED,
  /**
   * When a message from client side sent to a session.
   */
  @Asynchronous
  SESSION_READ_MESSAGE,
  /**
   * When a message sent to a session.
   *
   * @see EventWriteMessageToConnection
   */
  @Asynchronous
  SESSION_WRITE_MESSAGE,
  /**
   * When a message sent to the sever from client side via datagram channel at the first time,
   * this event is triggered. The system will check if the client's datagram channel was already
   * registered or not to decide which event should be emitted.
   * The implementation can be found in {@link ZeroReaderImpl#getDatagramIoHandler()}, and it has
   * {@code channelRead} or {@code sessionRead} for separated purposes.
   *
   * @see ZeroReaderImpl
   */
  @Asynchronous
  DATAGRAM_CHANNEL_READ_MESSAGE_FIRST_TIME,
  /**
   * When the server finished initialization and is ready.
   *
   * @see EventServerInitialization
   */
  SERVER_INITIALIZATION,
  /**
   * When the server responds a connection request from client side.
   *
   * @see EventConnectionEstablishedResult
   */
  CONNECTION_ESTABLISHED_RESULT,
  /**
   * When the server responds a player logged in request.
   *
   * @see EventPlayerLoggedinResult
   */
  PLAYER_LOGGEDIN_RESULT,
  /**
   * When the server handles a reconnection request.
   *
   * @see EventPlayerReconnectRequestHandle
   */
  PLAYER_RECONNECT_REQUEST_HANDLE,
  /**
   * When the server responds a player reconnected request.
   *
   * @see EventPlayerReconnectedResult
   */
  PLAYER_RECONNECTED_RESULT,
  /**
   * When the server sends a message to client side on behalf of its player.
   *
   * @see EventSendMessageToPlayer
   */
  SEND_MESSAGE_TO_PLAYER,
  /**
   * When the server receives a message from client side on behalf of its player.
   *
   * @see EventReceivedMessageFromPlayer
   */
  RECEIVED_MESSAGE_FROM_PLAYER,
  /**
   * When the server responds a room creation request.
   *
   * @see EventRoomCreatedResult
   */
  ROOM_CREATED_RESULT,
  /**
   * When a room is going to be removed from the management list.
   *
   * @see EventRoomWillBeRemoved
   */
  ROOM_WILL_BE_REMOVED,
  /**
   * When the server responds a request from player regarding joining a room.
   *
   * @see EventPlayerJoinedRoomResult
   */
  PLAYER_JOINED_ROOM_RESULT,
  /**
   * When a player is going to leave its current room.
   *
   * @see EventPlayerBeforeLeaveRoom
   */
  PLAYER_BEFORE_LEAVE_ROOM,
  /**
   * When a player has just left its room.
   *
   * @see EventPlayerAfterLeftRoom
   */
  PLAYER_AFTER_LEFT_ROOM,
  /**
   * When a player attempts to change its role from 'participant' to 'spectator'.
   *
   * @see EventSwitchParticipantToSpectatorResult
   */
  SWITCH_PARTICIPANT_TO_SPECTATOR,
  /**
   * When a player attempts to change its role from 'spectator' to 'participant'.
   *
   * @see EventSwitchSpectatorToParticipantResult
   */
  SWITCH_SPECTATOR_TO_PARTICIPANT,
  /**
   * When a player is going to disconnect from the server.
   *
   * @see EventDisconnectPlayer
   */
  DISCONNECT_PLAYER,
  /**
   * When the server validates a UDP channel accessing request from a player.
   *
   * @see EventAccessDatagramChannelRequestValidation
   */
  ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION,
  /**
   * When the server responds a UDP channel accessing request from a player.
   *
   * @see EventAccessDatagramChannelRequestValidationResult
   */
  ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT,
  /**
   * When the server validates a KCP channel accessing request from a player.
   *
   * @see EventAccessKcpChannelRequestValidation
   */
  ACCESS_KCP_CHANNEL_REQUEST_VALIDATION,
  /**
   * When the server responds a KCP channel accessing request from a player.
   *
   * @see EventAccessKcpChannelRequestValidationResult
   */
  ACCESS_KCP_CHANNEL_REQUEST_VALIDATION_RESULT,
  /**
   * When the server provides information regarding CCU.
   *
   * @see EventFetchedCcuInfo
   */
  FETCHED_CCU_INFO,
  /**
   * When the server provides information regarding bandwidth.
   *
   * @see EventFetchedBandwidthInfo
   */
  FETCHED_BANDWIDTH_INFO,
  /**
   * When the server provides information regarding system.
   *
   * @see EventSystemMonitoring
   */
  SYSTEM_MONITORING,
  /**
   * When there is any exception occurs on the server.
   *
   * @see EventServerException
   */
  SERVER_EXCEPTION,
  /**
   * When the server is going to shut down.
   *
   * @see EventServerTeardown
   */
  SERVER_TEARDOWN;

  @Override
  public String toString() {
    return this.name();
  }
}
