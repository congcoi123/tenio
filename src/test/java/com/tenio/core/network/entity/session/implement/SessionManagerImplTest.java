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

package com.tenio.core.network.entity.session.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.outbound.packet.policy.OutboundQueuePolicy;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.entity.session.manager.SessionManagerImpl;
import com.tenio.core.network.security.filter.ConnectionFilter;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For SessionManagerImpl")
class SessionManagerImplTest {

  private SessionManager sessionManager;

  @BeforeEach
  void setUp() {
    sessionManager = SessionManagerImpl.newInstance(EventManager.newInstance());
  }

  @Test
  @DisplayName("Test newInstance creates a non-null session manager")
  void testNewInstance() {
    assertNotNull(sessionManager);
  }

  @Test
  @DisplayName("Test getSnapshotSessionsList returns empty list initially")
  void testGetSnapshotSessionsListInitiallyEmpty() {
    assertNotNull(sessionManager.getSnapshotSessionsList());
    assertTrue(sessionManager.getSnapshotSessionsList().isEmpty());
  }

  @Test
  @DisplayName("Test getSnapshotSessionCount returns 0 initially")
  void testGetSnapshotSessionCountInitiallyZero() {
    assertEquals(0, sessionManager.getSnapshotSessionCount());
  }

  @Test
  @DisplayName("Test computeSessions with empty manager calls consumer")
  void testComputeSessionsWithEmptyManager() {
    AtomicInteger count = new AtomicInteger(0);
    sessionManager.computeSessions(it -> {
      while (it.hasNext()) {
        it.next();
        count.incrementAndGet();
      }
    });
    assertEquals(0, count.get());
  }

  @Test
  @DisplayName("Test getSessionBySocket returns null for unknown socket")
  void testGetSessionBySocketReturnsNullForUnknown() {
    assertNull(sessionManager.getSessionBySocket(null));
  }

  @Test
  @DisplayName("Test getSessionByWebSocket returns null for unknown channel")
  void testGetSessionByWebSocketReturnsNullForUnknown() {
    assertNull(sessionManager.getSessionByWebSocket(null));
  }

  @Test
  @DisplayName("Test getSessionByDatagram returns null for unknown convey")
  void testGetSessionByDatagramReturnsNullForUnknown() {
    assertNull(sessionManager.getSessionByDatagram(999));
  }

  @Test
  @DisplayName("Test addDatagramForSession throws when session is not TCP")
  void testAddDatagramForSessionThrowsWhenNotTcp() {
    Session session = mock(Session.class);
    when(session.isTcp()).thenReturn(false);
    assertThrows(IllegalArgumentException.class,
        () -> sessionManager.addDatagramForSession(null, 1, session));
  }

  @Test
  @DisplayName("Test configureConnectionFilter does not throw")
  void testConfigureConnectionFilter() {
    ConnectionFilter filter = mock(ConnectionFilter.class);
    sessionManager.configureConnectionFilter(filter);
    // no exception expected
  }

  @Test
  @DisplayName("Test configureOutboundQueuePolicy does not throw")
  void testConfigureOutboundQueuePolicy() {
    OutboundQueuePolicy policy = mock(OutboundQueuePolicy.class);
    sessionManager.configureOutboundQueuePolicy(policy);
  }

  @Test
  @DisplayName("Test configureInboundQueueSize does not throw")
  void testConfigureInboundQueueSize() {
    sessionManager.configureInboundQueueSize(128);
  }

  @Test
  @DisplayName("Test configureOutboundQueueSize does not throw")
  void testConfigureOutboundQueueSize() {
    sessionManager.configureOutboundQueueSize(256);
  }

  @Test
  @DisplayName("Test configureSlowConsumingInboundQueueWarningThreshold does not throw")
  void testConfigureSlowConsumingInboundQueueWarningThreshold() {
    sessionManager.configureSlowConsumingInboundQueueWarningThreshold(50);
  }

  @Test
  @DisplayName("Test configureSlowConsumingOutboundQueueWarningThreshold does not throw")
  void testConfigureSlowConsumingOutboundQueueWarningThreshold() {
    sessionManager.configureSlowConsumingOutboundQueueWarningThreshold(100);
  }

  @Test
  @DisplayName("Test configureMaxIdleTimeInSeconds does not throw")
  void testConfigureMaxIdleTimeInSeconds() {
    sessionManager.configureMaxIdleTimeInSeconds(60);
  }

  @Test
  @DisplayName("Test emitEvent does not throw when eventManager has no subscribers")
  void testEmitEvent() {
    sessionManager.emitEvent(ServerEvent.SERVER_INITIALIZATION, "test");
  }

  @Test
  @DisplayName("Test removeSession with UNKNOWN transport type does not throw")
  void testRemoveSessionWithUnknownTransport() {
    Session session = mock(Session.class);
    when(session.getTransportType()).thenReturn(TransportType.UNKNOWN);
    when(session.getId()).thenReturn(99L);
    sessionManager.removeSession(session);
    assertEquals(0, sessionManager.getSnapshotSessionCount());
  }

  @Test
  @DisplayName("Test removeSession with TCP transport removes from socket map")
  void testRemoveSessionWithTcpTransport() {
    Session session = mock(Session.class);
    when(session.getTransportType()).thenReturn(TransportType.TCP);
    when(session.containsUdp()).thenReturn(false);
    when(session.fetchSocketChannel()).thenReturn(null);
    when(session.getId()).thenReturn(1L);
    sessionManager.removeSession(session);
    assertEquals(0, sessionManager.getSnapshotSessionCount());
  }

  @Test
  @DisplayName("Test removeSession with TCP+UDP transport removes UDP datagram entry")
  void testRemoveSessionWithTcpAndUdpTransport() {
    Session session = mock(Session.class);
    when(session.getTransportType()).thenReturn(TransportType.TCP);
    when(session.containsUdp()).thenReturn(true);
    when(session.getUdpConveyId()).thenReturn(5);
    when(session.fetchSocketChannel()).thenReturn(null);
    when(session.getId()).thenReturn(2L);
    sessionManager.removeSession(session);
    assertEquals(0, sessionManager.getSnapshotSessionCount());
  }

  @Test
  @DisplayName("Test removeSession with WEB_SOCKET transport removes from websocket map")
  void testRemoveSessionWithWebSocketTransport() {
    Session session = mock(Session.class);
    when(session.getTransportType()).thenReturn(TransportType.WEB_SOCKET);
    when(session.fetchWebSocketChannel()).thenReturn(null);
    when(session.getId()).thenReturn(3L);
    sessionManager.removeSession(session);
    assertEquals(0, sessionManager.getSnapshotSessionCount());
  }

  @Test
  @DisplayName("Test addDatagramForSession happy path when session is TCP")
  void testAddDatagramForSessionWhenTcp() {
    Session session = mock(Session.class);
    when(session.isTcp()).thenReturn(true);
    sessionManager.addDatagramForSession(null, 42, session);
    assertEquals(session, sessionManager.getSessionByDatagram(42));
  }

  @Test
  @DisplayName("createWebSocketSession creates an activated WEB_SOCKET session")
  void testCreateWebSocketSession() {
    io.netty.channel.Channel channel = mock(io.netty.channel.Channel.class);
    when(channel.remoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8080));

    Session session = sessionManager.createWebSocketSession(channel);

    assertNotNull(session);
    assertTrue(session.isActivated());
    assertEquals(TransportType.WEB_SOCKET, session.getTransportType());
    assertEquals(1, sessionManager.getSnapshotSessionCount());
    assertEquals(session, sessionManager.getSessionByWebSocket(channel));
  }

  @Test
  @DisplayName("removeSessionByWebSocket removes the session and decrements count")
  void testRemoveSessionByWebSocket() {
    io.netty.channel.Channel channel = mock(io.netty.channel.Channel.class);
    when(channel.remoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8080));

    sessionManager.createWebSocketSession(channel);
    assertEquals(1, sessionManager.getSnapshotSessionCount());

    sessionManager.removeSessionByWebSocket(channel);
    assertEquals(0, sessionManager.getSnapshotSessionCount());
    assertNull(sessionManager.getSessionByWebSocket(channel));
  }

  private java.nio.channels.SocketChannel mockSocketChannel() {
    java.nio.channels.SocketChannel socketChannel = mock(java.nio.channels.SocketChannel.class);
    java.net.Socket socket = mock(java.net.Socket.class);
    when(socketChannel.socket()).thenReturn(socket);
    when(socket.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 9090));
    return socketChannel;
  }

  @Test
  @DisplayName("createSocketSession creates an activated TCP session")
  void testCreateSocketSession() {
    java.nio.channels.SocketChannel socketChannel = mockSocketChannel();
    java.nio.channels.SelectionKey selectionKey = mock(java.nio.channels.SelectionKey.class);

    Session session = sessionManager.createSocketSession(socketChannel, selectionKey);

    assertNotNull(session);
    assertTrue(session.isActivated());
    assertEquals(com.tenio.core.network.define.TransportType.TCP, session.getTransportType());
    assertEquals(1, sessionManager.getSnapshotSessionCount());
    assertEquals(session, sessionManager.getSessionBySocket(socketChannel));
  }

  @Test
  @DisplayName("removeSessionBySocket removes the session and decrements count")
  void testRemoveSessionBySocket() {
    java.nio.channels.SocketChannel socketChannel = mockSocketChannel();
    java.nio.channels.SelectionKey selectionKey = mock(java.nio.channels.SelectionKey.class);

    sessionManager.createSocketSession(socketChannel, selectionKey);
    assertEquals(1, sessionManager.getSnapshotSessionCount());

    sessionManager.removeSessionBySocket(socketChannel);
    assertEquals(0, sessionManager.getSnapshotSessionCount());
    assertNull(sessionManager.getSessionBySocket(socketChannel));
  }
}
