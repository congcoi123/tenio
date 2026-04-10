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
import static org.mockito.Mockito.verify;

import com.tenio.common.data.DataCollection;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.zero.handler.DatagramIoHandler;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For DatagramIoHandlerImpl")
class DatagramIoHandlerImplTest {

  private DatagramIoHandler handler;
  private EventManager eventManager;

  @BeforeEach
  void setUp() {
    eventManager = mock(EventManager.class);
    handler = DatagramIoHandlerImpl.newInstance(eventManager);
    handler.setSessionManager(mock(SessionManager.class));
    handler.setNetworkReaderStatistic(mock(NetworkReaderStatistic.class));
  }

  @Test
  @DisplayName("newInstance creates a non-null handler")
  void testNewInstanceCreatesHandler() {
    assertNotNull(handler);
  }

  @Test
  @DisplayName("channelRead emits DATAGRAM_CHANNEL_READ_MESSAGE_FIRST_TIME")
  void testChannelReadEmitsDatagramChannelReadMessageFirstTime() {
    DatagramChannel datagramChannel = mock(DatagramChannel.class);
    SocketAddress remoteAddress = mock(SocketAddress.class);
    DataCollection message = mock(DataCollection.class);

    handler.channelRead(datagramChannel, remoteAddress, message);

    verify(eventManager).emit(ServerEvent.DATAGRAM_CHANNEL_REQUEST_ACCESS,
        datagramChannel, remoteAddress, message);
  }

  @Test
  @DisplayName("sessionRead emits SESSION_READ_MESSAGE")
  void testSessionReadEmitsSessionReadMessage() {
    Session session = mock(Session.class);
    DataCollection message = mock(DataCollection.class);

    handler.sessionRead(session, message);

    verify(session).enqueueInbound(message);
  }

  @Test
  @DisplayName("channelException does not throw and emits no event")
  void testChannelExceptionDoesNothing() {
    DatagramChannel datagramChannel = mock(DatagramChannel.class);
    Exception exception = new RuntimeException("udp error");

    assertDoesNotThrow(() -> handler.channelException(datagramChannel, exception));
  }

  @Test
  @DisplayName("sessionException closes session")
  void testSessionExceptionEmitsSessionOccurredException() throws IOException {
    Session session = mock(Session.class);
    Exception exception = new RuntimeException("session error");

    handler.sessionException(session, exception);

    verify(session).close();
  }
}
