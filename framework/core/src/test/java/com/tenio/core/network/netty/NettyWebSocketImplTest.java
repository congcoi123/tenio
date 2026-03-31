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

package com.tenio.core.network.netty;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For NettyWebSocketImpl")
class NettyWebSocketImplTest {

  private NettyWebSocket webSocket;

  @BeforeEach
  void setUp() {
    webSocket = NettyWebSocketImpl.newInstance(mock(EventManager.class));
  }

  @Test
  @DisplayName("getName returns 'netty-websocket'")
  void testGetNameReturnsNettyWebSocket() {
    assertEquals("netty-websocket", webSocket.getName());
  }

  @Test
  @DisplayName("getMaximumStartingTimeInMilliseconds returns 0")
  void testGetMaximumStartingTimeInMillisecondsReturnsZero() {
    assertEquals(0, webSocket.getMaximumStartingTimeInMilliseconds());
  }

  @Test
  @DisplayName("isActivated throws UnsupportedOperationException")
  void testIsActivatedThrowsUnsupportedOperationException() {
    assertThrows(UnsupportedOperationException.class, () -> webSocket.isActivated());
  }

  @Test
  @DisplayName("setName throws UnsupportedOperationException")
  void testSetNameThrowsUnsupportedOperationException() {
    assertThrows(UnsupportedOperationException.class, () -> webSocket.setName("name"));
  }

  @Test
  @DisplayName("start before initialize does nothing")
  void testStartBeforeInitializeDoesNothing() {
    assertDoesNotThrow(() -> webSocket.start());
  }

  @Test
  @DisplayName("shutdown before initialize does nothing")
  void testShutdownBeforeInitializeDoesNothing() {
    assertDoesNotThrow(() -> webSocket.shutdown());
  }

  @Test
  @DisplayName("activate before initialize does nothing")
  void testActivateBeforeInitializeDoesNothing() {
    assertDoesNotThrow(() -> webSocket.activate());
  }

  @Test
  @DisplayName("initialize does not throw")
  void testInitializeDoesNotThrow() {
    assertDoesNotThrow(() -> webSocket.initialize());
  }

  @Test
  @DisplayName("setSenderBufferSize does not throw")
  void testSetSenderBufferSizeDoesNotThrow() {
    assertDoesNotThrow(() -> webSocket.setSenderBufferSize(2048));
  }

  @Test
  @DisplayName("setReceiverBufferSize does not throw")
  void testSetReceiverBufferSizeDoesNotThrow() {
    assertDoesNotThrow(() -> webSocket.setReceiverBufferSize(2048));
  }

  @Test
  @DisplayName("setProducerWorkerSize does not throw")
  void testSetProducerWorkerSizeDoesNotThrow() {
    assertDoesNotThrow(() -> webSocket.setProducerWorkerSize(4));
  }

  @Test
  @DisplayName("setConsumerWorkerSize does not throw")
  void testSetConsumerWorkerSizeDoesNotThrow() {
    assertDoesNotThrow(() -> webSocket.setConsumerWorkerSize(8));
  }

  @Test
  @DisplayName("setConnectionFilter does not throw")
  void testSetConnectionFilterDoesNotThrow() {
    assertDoesNotThrow(() -> webSocket.setConnectionFilter(mock(ConnectionFilter.class)));
  }

  @Test
  @DisplayName("setPacketEncoder does not throw")
  void testSetPacketEncoderDoesNotThrow() {
    assertDoesNotThrow(() -> webSocket.setPacketEncoder(mock(BinaryPacketEncoder.class)));
  }

  @Test
  @DisplayName("setPacketDecoder does not throw")
  void testSetPacketDecoderDoesNotThrow() {
    assertDoesNotThrow(() -> webSocket.setPacketDecoder(mock(BinaryPacketDecoder.class)));
  }

  @Test
  @DisplayName("setSessionManager does not throw")
  void testSetSessionManagerDoesNotThrow() {
    assertDoesNotThrow(() -> webSocket.setSessionManager(mock(SessionManager.class)));
  }

  @Test
  @DisplayName("setNetworkReaderStatistic does not throw")
  void testSetNetworkReaderStatisticDoesNotThrow() {
    assertDoesNotThrow(
        () -> webSocket.setNetworkReaderStatistic(mock(NetworkReaderStatistic.class)));
  }

  @Test
  @DisplayName("setNetworkWriterStatistic does not throw")
  void testSetNetworkWriterStatisticDoesNotThrow() {
    assertDoesNotThrow(
        () -> webSocket.setNetworkWriterStatistic(mock(NetworkWriterStatistic.class)));
  }

  @Test
  @DisplayName("setWebSocketConfiguration does not throw")
  void testSetWebSocketConfigurationDoesNotThrow() {
    SocketConfiguration config =
        new SocketConfiguration("websocket", TransportType.WEB_SOCKET, 8080, 4);
    assertDoesNotThrow(() -> webSocket.setWebSocketConfiguration(config));
  }

  @Test
  @DisplayName("setUsingSsl does not throw")
  void testSetUsingSslDoesNotThrow() {
    assertDoesNotThrow(() -> webSocket.setUsingSsl(true));
  }

  @Test
  @DisplayName("write skips inactive sessions")
  void testWriteSkipsInactiveSession() {
    Session session = mock(Session.class);
    Packet packet = mock(Packet.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    when(packet.getRecipients()).thenReturn(List.of(session));
    when(packet.isMarkedAsLast()).thenReturn(false);
    when(session.isActivated()).thenReturn(false);

    webSocket.setPacketEncoder(encoder);
    webSocket.setNetworkWriterStatistic(mock(NetworkWriterStatistic.class));

    assertDoesNotThrow(() -> webSocket.write(packet));
    verify(encoder, never()).encode(packet);
  }

  @Test
  @DisplayName("write with packet marked as last closes the active session")
  void testWriteWithLastPacketClosesActiveSession() throws IOException {
    Session session = mock(Session.class);
    Packet packet = mock(Packet.class);
    when(packet.getRecipients()).thenReturn(List.of(session));
    when(packet.isMarkedAsLast()).thenReturn(true);
    when(session.isActivated()).thenReturn(true);

    webSocket.setNetworkWriterStatistic(mock(NetworkWriterStatistic.class));

    webSocket.write(packet);

    verify(session).close(ConnectionDisconnectMode.CLIENT_REQUEST,
        PlayerDisconnectMode.CLIENT_REQUEST);
  }
}
