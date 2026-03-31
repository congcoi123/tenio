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

package com.tenio.core.network.zero.handler.implement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.common.data.DataCollection;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.RefusedConnectionAddressException;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For SocketIoHandlerImpl")
class SocketIoHandlerImplTest {

  private SocketIoHandlerImpl handler;
  private EventManager eventManager;
  private SessionManager sessionManager;
  private NetworkReaderStatistic readerStatistic;

  @BeforeEach
  void setUp() {
    eventManager = mock(EventManager.class);
    sessionManager = mock(SessionManager.class);
    readerStatistic = mock(NetworkReaderStatistic.class);
    handler = (SocketIoHandlerImpl) SocketIoHandlerImpl.newInstance(eventManager);
    handler.setSessionManager(sessionManager);
    handler.setNetworkReaderStatistic(readerStatistic);
    handler.setPacketDecoder(mock(BinaryPacketDecoder.class));
  }

  @Test
  @DisplayName("newInstance creates a non-null handler")
  void testNewInstanceCreatesHandler() {
    assertNotNull(handler);
  }

  @Test
  @DisplayName("getPacketDecoder returns the decoder after setPacketDecoder")
  void testSetAndGetPacketDecoder() {
    BinaryPacketDecoder decoder = mock(BinaryPacketDecoder.class);
    handler.setPacketDecoder(decoder);
    assertNotNull(handler.getPacketDecoder());
  }

  @Test
  @DisplayName("channelActive delegates to sessionManager.createSocketSession")
  void testChannelActiveCreatesSocketSession() {
    SocketChannel socketChannel = mock(SocketChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);

    handler.channelActive(socketChannel, selectionKey);

    verify(sessionManager).createSocketSession(socketChannel, selectionKey);
  }

  @Test
  @DisplayName("channelException with RefusedConnectionAddressException emits SOCKET_CONNECTION_REFUSED")
  void testChannelExceptionWithRefusedConnectionEmitsEvent() {
    SocketChannel socketChannel = mock(SocketChannel.class);
    RefusedConnectionAddressException exception = mock(RefusedConnectionAddressException.class);

    handler.channelException(socketChannel, exception);

    verify(eventManager).emit(ServerEvent.SOCKET_CONNECTION_REFUSED, socketChannel, exception);
  }

  @Test
  @DisplayName("channelException with generic exception does not emit any event")
  void testChannelExceptionWithGenericExceptionDoesNotEmitEvent() {
    SocketChannel socketChannel = mock(SocketChannel.class);
    RuntimeException exception = new RuntimeException("generic error");

    handler.channelException(socketChannel, exception);

    verify(eventManager, never()).emit(ServerEvent.SOCKET_CONNECTION_REFUSED, socketChannel,
        exception);
  }

  @Test
  @DisplayName("onFramedResult with null message does not update statistics or emit events")
  void testOnFramedResultWithNullMessageDoesNothing() {
    Session session = mock(Session.class);

    handler.onFramedResult(session, null);

    verify(readerStatistic, never()).updateReadPackets(1);
    verify(eventManager, never()).emit(ServerEvent.SESSION_REQUEST_CONNECTION, session, null);
  }

  @Test
  @DisplayName("onFramedResult with NONE state emits SESSION_REQUEST_CONNECTION")
  void testOnFramedResultWithNoneStateEmitsSessionRequestConnection() {
    Session session = mock(Session.class);
    DataCollection message = mock(DataCollection.class);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DOING)).thenReturn(false);
    when(session.isAssociatedToPlayer(Session.AssociatedState.NONE)).thenReturn(true);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(false);

    handler.onFramedResult(session, message);

    verify(readerStatistic).updateReadPackets(1);
    verify(eventManager).emit(ServerEvent.SESSION_REQUEST_CONNECTION, session, message);
  }

  @Test
  @DisplayName("onFramedResult with DONE state emits SESSION_READ_MESSAGE")
  void testOnFramedResultWithDoneStateEmitsSessionReadMessage() {
    Session session = mock(Session.class);
    DataCollection message = mock(DataCollection.class);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DOING)).thenReturn(false);
    when(session.isAssociatedToPlayer(Session.AssociatedState.NONE)).thenReturn(false);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(true);

    handler.onFramedResult(session, message);

    verify(readerStatistic).updateReadPackets(1);
    verify(eventManager).emit(ServerEvent.SESSION_READ_MESSAGE, session, message);
  }

  @Test
  @DisplayName("onFramedResult with DOING state rejects the message without emitting an event")
  void testOnFramedResultWithDoingStateRejectsMessage() {
    Session session = mock(Session.class);
    DataCollection message = mock(DataCollection.class);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DOING)).thenReturn(true);

    handler.onFramedResult(session, message);

    verify(readerStatistic).updateReadPackets(1);
    verify(eventManager, never()).emit(ServerEvent.SESSION_REQUEST_CONNECTION, session, message);
    verify(eventManager, never()).emit(ServerEvent.SESSION_READ_MESSAGE, session, message);
  }

  @Test
  @DisplayName("sessionException emits SESSION_OCCURRED_EXCEPTION")
  void testSessionExceptionEmitsSessionOccurredException() {
    Session session = mock(Session.class);
    Exception exception = new RuntimeException("test error");

    handler.sessionException(session, exception);

    verify(eventManager).emit(ServerEvent.SESSION_OCCURRED_EXCEPTION, session, exception);
  }

  @Test
  @DisplayName("channelInactive with no session found closes the socket directly")
  void testChannelInactiveWithNoSessionClosesSocket() {
    SocketChannel socketChannel = mock(SocketChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);
    when(sessionManager.getSessionBySocket(socketChannel)).thenReturn(null);

    assertDoesNotThrow(() ->
        handler.channelInactive(socketChannel, selectionKey, ConnectionDisconnectMode.SERVER_DOWN));
  }

  @Test
  @DisplayName("channelInactive with inactive session closes the socket directly")
  void testChannelInactiveWithInactiveSessionClosesSocket() {
    SocketChannel socketChannel = mock(SocketChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);
    Session session = mock(Session.class);
    when(sessionManager.getSessionBySocket(socketChannel)).thenReturn(session);
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() ->
        handler.channelInactive(socketChannel, selectionKey, ConnectionDisconnectMode.SERVER_DOWN));
  }
}
