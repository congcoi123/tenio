/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.core.bootstrap.event.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.tenio.common.data.DataCollection;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.result.AccessDatagramChannelResult;
import com.tenio.core.entity.define.result.ConnectionEstablishedResult;
import com.tenio.core.event.handler.implement.ConnectionEventHandler;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.RefusedConnectionAddressException;
import com.tenio.core.handler.event.EventAccessDatagramChannelRequestValidation;
import com.tenio.core.handler.event.EventAccessDatagramChannelRequestValidationResult;
import com.tenio.core.handler.event.EventConnectionEstablishedResult;
import com.tenio.core.handler.event.EventSocketConnectionRefused;
import com.tenio.core.handler.event.EventWebSocketConnectionRefused;
import com.tenio.core.handler.event.EventWriteMessageToConnection;
import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.session.Session;
import io.netty.channel.Channel;
import java.lang.reflect.Field;
import java.nio.channels.SocketChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ConnectionEventHandler")
class ConnectionEventHandlerTest {

  @Test
  @DisplayName("Test initialize with all null event listeners does not throw")
  void testInitialize() {
    ConnectionEventHandler connectionEventHandler = new ConnectionEventHandler();
    connectionEventHandler.initialize(EventManager.newInstance());
  }

  @Test
  @DisplayName("Test SOCKET_CONNECTION_REFUSED event dispatches to listener")
  void testSocketConnectionRefusedEvent() throws Exception {
    ConnectionEventHandler handler = new ConnectionEventHandler();
    EventManager em = EventManager.newInstance();
    EventSocketConnectionRefused mockEvent = mock(EventSocketConnectionRefused.class);
    Field field = ConnectionEventHandler.class.getDeclaredField("eventSocketConnectionRefused");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    SocketChannel channel = mock(SocketChannel.class);
    RefusedConnectionAddressException ex = new RefusedConnectionAddressException("blocked", "127.0.0.1");
    em.emit(ServerEvent.SOCKET_CONNECTION_REFUSED, channel, ex);
    verify(mockEvent).onSocketConnectionRefused(channel, ex);
  }

  @Test
  @DisplayName("Test WEBSOCKET_CONNECTION_REFUSED event dispatches to listener")
  void testWebSocketConnectionRefusedEvent() throws Exception {
    ConnectionEventHandler handler = new ConnectionEventHandler();
    EventManager em = EventManager.newInstance();
    EventWebSocketConnectionRefused mockEvent = mock(EventWebSocketConnectionRefused.class);
    Field field = ConnectionEventHandler.class.getDeclaredField("eventWebSocketConnectionRefused");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Channel channel = mock(Channel.class);
    RefusedConnectionAddressException ex = new RefusedConnectionAddressException("blocked", "127.0.0.1");
    em.emit(ServerEvent.WEBSOCKET_CONNECTION_REFUSED, channel, ex);
    verify(mockEvent).onWebSocketConnectionRefused(channel, ex);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test CONNECTION_ESTABLISHED_RESULT event dispatches to listener")
  void testConnectionEstablishedResultEvent() throws Exception {
    ConnectionEventHandler handler = new ConnectionEventHandler();
    EventManager em = EventManager.newInstance();
    EventConnectionEstablishedResult<DataCollection> mockEvent =
        mock(EventConnectionEstablishedResult.class);
    Field field = ConnectionEventHandler.class.getDeclaredField("eventConnectionEstablishedResult");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Session session = mock(Session.class);
    DataCollection message = mock(DataCollection.class);
    em.emit(ServerEvent.CONNECTION_ESTABLISHED_RESULT, session, message,
        ConnectionEstablishedResult.SUCCESS);
    verify(mockEvent).onConnectionEstablishedResult(session, message,
        ConnectionEstablishedResult.SUCCESS);
  }

  @Test
  @DisplayName("Test SESSION_WRITE_MESSAGE event dispatches to listener")
  void testSessionWriteMessageEvent() throws Exception {
    ConnectionEventHandler handler = new ConnectionEventHandler();
    EventManager em = EventManager.newInstance();
    EventWriteMessageToConnection mockEvent = mock(EventWriteMessageToConnection.class);
    Field field = ConnectionEventHandler.class.getDeclaredField("eventWriteMessageToConnection");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Session session = mock(Session.class);
    Packet packet = mock(Packet.class);
    em.emit(ServerEvent.SESSION_WRITE_MESSAGE, session, packet);
    verify(mockEvent).onWriteMessageToConnection(session, packet);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION event dispatches to listener")
  void testAccessDatagramChannelRequestValidationEvent() throws Exception {
    ConnectionEventHandler handler = new ConnectionEventHandler();
    EventManager em = EventManager.newInstance();
    EventAccessDatagramChannelRequestValidation<DataCollection> mockEvent =
        mock(EventAccessDatagramChannelRequestValidation.class);
    Field field = ConnectionEventHandler.class
        .getDeclaredField("eventAccessDatagramChannelRequestValidation");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    DataCollection message = mock(DataCollection.class);
    em.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION, message);
    verify(mockEvent).onAccessDatagramChannelRequestValidation(message);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT event dispatches to listener")
  void testAccessDatagramChannelRequestValidationResultEvent() throws Exception {
    ConnectionEventHandler handler = new ConnectionEventHandler();
    EventManager em = EventManager.newInstance();
    EventAccessDatagramChannelRequestValidationResult<Player> mockEvent =
        mock(EventAccessDatagramChannelRequestValidationResult.class);
    Field field = ConnectionEventHandler.class
        .getDeclaredField("eventAccessDatagramChannelRequestValidationResult");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Player player = mock(Player.class);
    em.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT, player, 7,
        AccessDatagramChannelResult.SUCCESS);
    verify(mockEvent).onAccessDatagramChannelRequestValidationResult(player, 7,
        AccessDatagramChannelResult.SUCCESS);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT event with null player")
  void testAccessDatagramChannelRequestValidationResultWithNullPlayer() throws Exception {
    ConnectionEventHandler handler = new ConnectionEventHandler();
    EventManager em = EventManager.newInstance();
    EventAccessDatagramChannelRequestValidationResult<Player> mockEvent =
        mock(EventAccessDatagramChannelRequestValidationResult.class);
    Field field = ConnectionEventHandler.class
        .getDeclaredField("eventAccessDatagramChannelRequestValidationResult");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    em.emit(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT, null, 0,
        AccessDatagramChannelResult.PLAYER_NOT_FOUND);
    verify(mockEvent).onAccessDatagramChannelRequestValidationResult(null, 0,
        AccessDatagramChannelResult.PLAYER_NOT_FOUND);
  }
}
