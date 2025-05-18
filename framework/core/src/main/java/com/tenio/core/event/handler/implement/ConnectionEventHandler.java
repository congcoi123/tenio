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

package com.tenio.core.event.handler.implement;

import com.tenio.common.data.DataCollection;
import com.tenio.common.utility.TimeUtility;
import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.result.AccessDatagramChannelResult;
import com.tenio.core.entity.define.result.ConnectionEstablishedResult;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.RefusedConnectionAddressException;
import com.tenio.core.handler.event.EventAccessDatagramChannelRequestValidation;
import com.tenio.core.handler.event.EventAccessDatagramChannelRequestValidationResult;
import com.tenio.core.handler.event.EventAccessKcpChannelRequestValidation;
import com.tenio.core.handler.event.EventAccessKcpChannelRequestValidationResult;
import com.tenio.core.handler.event.EventConnectionEstablishedResult;
import com.tenio.core.handler.event.EventSocketConnectionRefused;
import com.tenio.core.handler.event.EventWebSocketConnectionRefused;
import com.tenio.core.handler.event.EventWriteMessageToConnection;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.Session;
import io.netty.channel.Channel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Optional;

/**
 * Dispatching all events related to connections.
 */
@Component
public final class ConnectionEventHandler {

  @AutowiredAcceptNull
  private EventSocketConnectionRefused eventSocketConnectionRefused;

  @AutowiredAcceptNull
  private EventWebSocketConnectionRefused eventWebSocketConnectionRefused;

  @AutowiredAcceptNull
  private EventConnectionEstablishedResult eventConnectionEstablishedResult;

  @AutowiredAcceptNull
  private EventWriteMessageToConnection eventWriteMessageToConnection;

  @AutowiredAcceptNull
  private EventAccessDatagramChannelRequestValidation eventAccessDatagramChannelRequestValidation;

  @AutowiredAcceptNull
  private EventAccessDatagramChannelRequestValidationResult<Player> eventAccessDatagramChannelRequestValidationResult;

  @AutowiredAcceptNull
  private EventAccessKcpChannelRequestValidation eventAccessKcpChannelRequestValidation;

  @AutowiredAcceptNull
  private EventAccessKcpChannelRequestValidationResult<Player> eventAccessKcpChannelRequestValidationResult;

  /**
   * Initialization.
   *
   * @param eventManager the event manager
   */
  public void initialize(EventManager eventManager) {

    final var eventSocketConnectionRefusedOp = Optional.ofNullable(eventSocketConnectionRefused);
    final var eventWebSocketConnectionRefusedOp =
        Optional.ofNullable(eventWebSocketConnectionRefused);

    final var eventConnectionEstablishedResultOp =
        Optional.ofNullable(eventConnectionEstablishedResult);
    final var eventWriteMessageToConnectionOp =
        Optional.ofNullable(eventWriteMessageToConnection);

    final var eventAccessDatagramChannelRequestValidationOp =
        Optional.ofNullable(eventAccessDatagramChannelRequestValidation);
    final var eventAccessDatagramChannelRequestValidationResultOp =
        Optional.ofNullable(eventAccessDatagramChannelRequestValidationResult);

    final var eventAccessKcpChannelRequestValidationOp =
        Optional.ofNullable(eventAccessKcpChannelRequestValidation);
    final var eventAccessKcpChannelRequestValidationResultOp =
        Optional.ofNullable(eventAccessKcpChannelRequestValidationResult);

    eventSocketConnectionRefusedOp.ifPresent(
        event -> eventManager.on(ServerEvent.SOCKET_CONNECTION_REFUSED, params -> {
          var socketChannel = (SocketChannel) params[0];
          var exception = (RefusedConnectionAddressException) params[1];

          event.handle(socketChannel, exception);

          return null;
        }));

    eventWebSocketConnectionRefusedOp.ifPresent(
        event -> eventManager.on(ServerEvent.WEBSOCKET_CONNECTION_REFUSED, params -> {
          var channel = (Channel) params[0];
          var exception = (RefusedConnectionAddressException) params[1];

          event.handle(channel, exception);

          return null;
        }));

    eventConnectionEstablishedResultOp.ifPresent(
        event -> eventManager.on(ServerEvent.CONNECTION_ESTABLISHED_RESULT, params -> {
          var session = (Session) params[0];
          var message = (DataCollection) params[1];
          var result = (ConnectionEstablishedResult) params[2];

          event.handle(session, message, result);

          return null;
        }));

    eventWriteMessageToConnectionOp.ifPresent(
        event -> eventManager.on(ServerEvent.SESSION_WRITE_MESSAGE, params -> {
          var session = (Session) params[0];
          var packet = (Packet) params[1];
          session.setLastWriteTime(TimeUtility.currentTimeMillis());

          event.handle(session, packet);

          return null;
        }));

    eventAccessDatagramChannelRequestValidationOp.ifPresent(
        event -> eventManager.on(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION, params -> {
          var message = (DataCollection) params[0];

          return event.handle(message);
        }));

    eventAccessDatagramChannelRequestValidationResultOp.ifPresent(
        event -> eventManager.on(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT, params -> {
          var player = Objects.isNull(params[0]) ? null : (Player) params[0];
          var udpConv = (int) params[1];
          var result = (AccessDatagramChannelResult) params[2];

          event.handle(player, udpConv, result);

          return null;
        }));

    eventAccessKcpChannelRequestValidationOp.ifPresent(
        event -> eventManager.on(ServerEvent.ACCESS_KCP_CHANNEL_REQUEST_VALIDATION, params -> {
          var message = (DataCollection) params[0];

          return event.handle(message);
        }));

    eventAccessKcpChannelRequestValidationResultOp.ifPresent(
        event -> eventManager.on(ServerEvent.ACCESS_KCP_CHANNEL_REQUEST_VALIDATION_RESULT, params -> {
          var player = Objects.isNull(params[0]) ? null : (Player) params[0];
          var result = (AccessDatagramChannelResult) params[1];

          event.handle(player, result);

          return null;
        }));
  }
}
