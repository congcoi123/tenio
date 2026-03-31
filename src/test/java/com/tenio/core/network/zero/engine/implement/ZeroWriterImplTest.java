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

package com.tenio.core.network.zero.engine.implement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.entity.outbound.packet.OutboundQueue;
import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.ZeroWriter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ZeroWriterImpl")
class ZeroWriterImplTest {

  private ZeroWriter writer;

  @BeforeEach
  void setUp() {
    NetworkWriterStatistic writerStatistic = mock(NetworkWriterStatistic.class);
    writer = ZeroWriterImpl.newInstance(mock(EventManager.class));
    writer.setNetworkWriterStatistic(writerStatistic);
  }

  @Test
  @DisplayName("newInstance creates writer with name 'writer'")
  void testNewInstanceCreatesWithNameWriter() {
    assertEquals("writer", writer.getName());
  }

  @Test
  @DisplayName("setNetworkWriterStatistic and getNetworkWriterStatistic work correctly")
  void testSetAndGetNetworkWriterStatistic() {
    NetworkWriterStatistic statistic = mock(NetworkWriterStatistic.class);
    writer.setNetworkWriterStatistic(statistic);
    assertEquals(statistic, writer.getNetworkWriterStatistic());
  }

  @Test
  @DisplayName("setPacketEncoder does not throw")
  void testSetPacketEncoderDoesNotThrow() {
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    assertDoesNotThrow(() -> writer.setPacketEncoder(encoder));
  }

  @Test
  @DisplayName("enqueuePacket with null recipients does nothing")
  void testEnqueuePacketWithNullRecipientsDoesNothing() {
    Packet packet = mock(Packet.class);
    when(packet.getRecipients()).thenReturn(null);
    assertDoesNotThrow(() -> writer.enqueuePacket(packet));
  }

  @Test
  @DisplayName("enqueuePacket with single inactive session skips the session")
  void testEnqueuePacketWithSingleInactiveSessionSkipsIt() {
    Session session = mock(Session.class);
    Packet packet = mock(Packet.class);
    when(packet.getRecipients()).thenReturn(List.of(session));
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> writer.enqueuePacket(packet));

    // outboundQueue.put() should never be called
    verify(session, never()).fetchOutboundQueue();
  }

  @Test
  @DisplayName("enqueuePacket with active session but null outbound queue does nothing")
  void testEnqueuePacketWithNullOutboundQueueDoesNothing() {
    Session session = mock(Session.class);
    Packet packet = mock(Packet.class);
    when(packet.getRecipients()).thenReturn(List.of(session));
    when(session.isActivated()).thenReturn(true);
    when(session.fetchOutboundQueue()).thenReturn(null);

    assertDoesNotThrow(() -> writer.enqueuePacket(packet));

    verify(session).fetchOutboundQueue();
  }

  @Test
  @DisplayName("enqueuePacket with active session adds packet to outbound queue")
  void testEnqueuePacketWithActiveSessionAddsPacketToQueue() {
    // Initialize so sessionTicketsQueueManager is available
    writer.initialize();

    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);
    when(packet.getRecipients()).thenReturn(List.of(session));
    when(session.isActivated()).thenReturn(true);
    when(session.fetchOutboundQueue()).thenReturn(outboundQueue);
    when(session.getId()).thenReturn(0L);

    writer.enqueuePacket(packet);

    verify(outboundQueue).put(packet);
    verify(packet).setRecipients(null);

    writer.shutdown();
  }

  @Test
  @DisplayName("enqueuePacket with multiple recipients deep-copies the packet per session")
  void testEnqueuePacketWithMultipleRecipientsDeepCopiesPacket() {
    Session session1 = mock(Session.class);
    Session session2 = mock(Session.class);
    Packet packet = mock(Packet.class);
    Packet packetCopy = mock(Packet.class);
    when(packet.getRecipients()).thenReturn(List.of(session1, session2));
    when(packet.deepCopy()).thenReturn(packetCopy);
    // Both sessions are inactive so we can avoid initializing sessionTicketsQueueManager
    when(session1.isActivated()).thenReturn(false);
    when(session2.isActivated()).thenReturn(false);

    writer.enqueuePacket(packet);

    // deepCopy() is called once per session in the multi-recipient path (2 sessions = 2 calls)
    verify(packet, times(2)).deepCopy();
  }
}
