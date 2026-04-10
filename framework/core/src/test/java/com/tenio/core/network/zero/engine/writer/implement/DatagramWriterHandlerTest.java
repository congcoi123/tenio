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

package com.tenio.core.network.zero.engine.writer.implement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.outbound.packet.OutboundQueue;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.manager.SessionTicketsQueueManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For DatagramWriterHandler")
class DatagramWriterHandlerTest {

  private DatagramWriterHandler handler;

  @BeforeEach
  void setUp() {
    handler = DatagramWriterHandler.newInstance();
    handler.setNetworkWriterStatistic(mock(NetworkWriterStatistic.class));
    handler.setSessionTicketsQueueManager(mock(SessionTicketsQueueManager.class));
    handler.allocateBuffer(512);
  }

  @Test
  @DisplayName("newInstance creates a non-null DatagramWriterHandler")
  void testNewInstanceNotNull() {
    assertNotNull(handler);
  }

  @Test
  @DisplayName("newInstance returns an AbstractWriterHandler")
  void testNewInstanceIsAbstractWriterHandler() {
    assertInstanceOf(AbstractWriterHandler.class, handler);
  }

  @Test
  @DisplayName("send with null datagram channel returns early without throwing")
  void testSendWithNullDatagramChannelReturnsEarly() {
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    when(session.fetchDatagramChannel()).thenReturn(null);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    // Queue must not be modified when channel is absent
    verify(outboundQueue, never()).take();
  }

  @Test
  @DisplayName("send with null remote address returns early without throwing")
  void testSendWithNullRemoteAddressReturnsEarly() {
    var datagramChannel = mock(java.nio.channels.DatagramChannel.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    when(session.fetchDatagramChannel()).thenReturn(datagramChannel);
    when(session.getDatagramRemoteAddress()).thenReturn(null);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue, never()).take();
  }
}
