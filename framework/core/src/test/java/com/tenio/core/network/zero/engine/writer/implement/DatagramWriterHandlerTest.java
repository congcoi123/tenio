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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.outbound.packet.OutboundQueue;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.manager.SessionTicketsQueueManager;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For DatagramWriterHandler")
class DatagramWriterHandlerTest {

  private DatagramWriterHandler handler;
  private NetworkWriterStatistic writerStatistic;

  @BeforeEach
  void setUp() {
    handler = DatagramWriterHandler.newInstance();
    writerStatistic = mock(NetworkWriterStatistic.class);
    handler.setNetworkWriterStatistic(writerStatistic);
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

  @Test
  @DisplayName("send with empty packet data removes packet from queue")
  void testSendWithEmptyDataRemovesPacketFromQueue() {
    DatagramChannel datagramChannel = mock(DatagramChannel.class);
    InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 8080);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchDatagramChannel()).thenReturn(datagramChannel);
    when(session.getDatagramRemoteAddress()).thenReturn(remoteAddress);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.getData()).thenReturn(new byte[0]);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue).take();
  }

  @Test
  @DisplayName("send successfully writes data, updates statistics and removes packet from queue")
  void testSendSuccessfullyWritesData() throws Exception {
    DatagramChannel datagramChannel = mock(DatagramChannel.class);
    InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 8080);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchDatagramChannel()).thenReturn(datagramChannel);
    when(session.getDatagramRemoteAddress()).thenReturn(remoteAddress);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(datagramChannel.send(any(), any())).thenReturn(3);
    when(session.isActivated()).thenReturn(false);
    when(outboundQueue.isEmpty()).thenReturn(true);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue).take();
    verify(writerStatistic).updateWrittenBytes(3);
    verify(writerStatistic).updateWrittenPackets(1);
  }

  @Test
  @DisplayName("send with large data allocates bigger buffer and writes")
  void testSendWithLargeDataAllocatesBiggerBuffer() throws Exception {
    DatagramChannel datagramChannel = mock(DatagramChannel.class);
    InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 8080);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    handler.allocateBuffer(2);
    when(session.fetchDatagramChannel()).thenReturn(datagramChannel);
    when(session.getDatagramRemoteAddress()).thenReturn(remoteAddress);
    when(encoder.encode(packet)).thenReturn(packet);
    byte[] largeData = new byte[]{1, 2, 3, 4, 5};
    when(packet.getData()).thenReturn(largeData);
    when(datagramChannel.send(any(), any())).thenReturn(5);
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue).take();
  }

  @Test
  @DisplayName("send re-queues session when activated and outbound queue is not empty")
  void testSendReQueuesSessionWhenActivatedAndQueueNotEmpty() throws Exception {
    DatagramChannel datagramChannel = mock(DatagramChannel.class);
    InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 8080);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);
    SessionTicketsQueueManager queueManager = mock(SessionTicketsQueueManager.class);
    java.util.concurrent.BlockingQueue<Session> ticketsQueue = new java.util.concurrent.LinkedBlockingQueue<>();

    handler.setPacketEncoder(encoder);
    handler.setSessionTicketsQueueManager(queueManager);
    when(session.fetchDatagramChannel()).thenReturn(datagramChannel);
    when(session.getDatagramRemoteAddress()).thenReturn(remoteAddress);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(datagramChannel.send(any(), any())).thenReturn(3);
    when(session.isActivated()).thenReturn(true);
    when(session.getId()).thenReturn(42L);
    when(outboundQueue.isEmpty()).thenReturn(false);
    when(queueManager.getQueueByElementId(42L)).thenReturn(ticketsQueue);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue).take();
    assertNotNull(ticketsQueue.peek());
  }

  @Test
  @DisplayName("send with IOException from datagramChannel.send does not propagate and skips take")
  void testSendWithIOExceptionFromChannelDoesNotPropagate() throws Exception {
    DatagramChannel datagramChannel = mock(DatagramChannel.class);
    InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 8080);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchDatagramChannel()).thenReturn(datagramChannel);
    when(session.getDatagramRemoteAddress()).thenReturn(remoteAddress);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    doThrow(new IOException("send failed")).when(datagramChannel).send(any(), any());

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue, never()).take();
  }
}
