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

package com.tenio.core.bootstrap.event.handlers;

import com.tenio.common.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.common.bootstrap.annotation.Component;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.result.AttachedConnectionResult;
import com.tenio.core.entity.define.result.ConnectionEstablishedResult;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.handler.event.EventAttachConnectionRequestValidation;
import com.tenio.core.handler.event.EventAttachedConnectionResult;
import com.tenio.core.handler.event.EventConnectionEstablishedResult;
import com.tenio.core.handler.event.EventDisconnectConnection;
import com.tenio.core.handler.event.EventWriteMessageToConnection;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.Session;
import java.util.Optional;

/**
 * Dispatching all events related to connections.
 */
@Component
public final class ConnectionEventHandler {

  @AutowiredAcceptNull
  private EventConnectionEstablishedResult eventConnectionEstablishedResult;

  @AutowiredAcceptNull
  private EventWriteMessageToConnection eventWriteMessageToConnection;

  @AutowiredAcceptNull
  private EventAttachConnectionRequestValidation eventAttachConnectionRequestValidation;

  @AutowiredAcceptNull
  private EventAttachedConnectionResult eventAttachedConnectionResult;

  @AutowiredAcceptNull
  private EventDisconnectConnection eventDisconnectConnection;

  /**
   * Initialization.
   *
   * @param eventManager the event manager
   */
  public void initialize(EventManager eventManager) {

    final var eventConnectionEstablishedResultOp =
        Optional.ofNullable(eventConnectionEstablishedResult);
    final var eventWriteMessageToConnectionOp =
        Optional.ofNullable(eventWriteMessageToConnection);

    final var eventAttachConnectionRequestValidationOp =
        Optional.ofNullable(eventAttachConnectionRequestValidation);
    final var eventAttachedConnectionResultOp =
        Optional.ofNullable(eventAttachedConnectionResult);

    final var eventDisconnectConnectionOp =
        Optional.ofNullable(eventDisconnectConnection);

    eventConnectionEstablishedResultOp.ifPresent(
        event -> eventManager.on(ServerEvent.CONNECTION_ESTABLISHED_RESULT, params -> {
          var session = (Session) params[0];
          var message = (ServerMessage) params[1];
          var result = (ConnectionEstablishedResult) params[2];

          event.handle(session, message, result);

          return null;
        }));

    eventWriteMessageToConnectionOp.ifPresent(
        event -> eventManager.on(ServerEvent.SESSION_WRITE_MESSAGE, params -> {
          var session = (Session) params[0];
          var packet = (Packet) params[1];

          event.handle(session, packet);

          return null;
        }));

    eventAttachConnectionRequestValidationOp.ifPresent(
        event -> eventManager.on(ServerEvent.ATTACH_CONNECTION_REQUEST_VALIDATION, params -> {
          var message = (ServerMessage) params[0];

          return event.handle(message);
        }));

    eventAttachedConnectionResultOp.ifPresent(
        event -> eventManager.on(ServerEvent.ATTACHED_CONNECTION_RESULT, params -> {
          var player = (Optional<Player>) params[0];
          var kcpConv = (int) params[1];
          var result = (AttachedConnectionResult) params[2];

          event.handle(player, kcpConv, result);

          return null;
        }));

    eventDisconnectConnectionOp.ifPresent(
        event -> eventManager.on(ServerEvent.DISCONNECT_CONNECTION, params -> {
          var session = (Session) params[0];
          var mode = (ConnectionDisconnectMode) params[1];

          event.handle(session, mode);

          return null;
        }));
  }
}
