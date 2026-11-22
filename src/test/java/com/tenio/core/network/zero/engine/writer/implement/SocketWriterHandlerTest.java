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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.outbound.packet.OutboundQueue;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.manager.SessionTicketsQueueManager;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For SocketWriterHandler")
class SocketWriterHandlerTest {

  private SocketWriterHandler handler;
  private NetworkWriterStatistic writerStatistic;

  @BeforeEach
  void setUp() {
    handler = SocketWriterHandler.newInstance();
    writerStatistic = mock(NetworkWriterStatistic.class);
    handler.setNetworkWriterStatistic(writerStatistic);
    handler.setSessionTicketsQueueManager(mock(SessionTicketsQueueManager.class));
    handler.allocateBuffer(512);
  }

  @Test
  @DisplayName("newInstance creates a non-null SocketWriterHandler")
  void testNewInstanceNotNull() {
    assertNotNull(handler);
  }

  @Test
  @DisplayName("newInstance returns an AbstractWriterHandler")
  void testNewInstanceIsAbstractWriterHandler() {
    assertInstanceOf(AbstractWriterHandler.class, handler);
  }

  @Test
  @DisplayName("send with null socket channel skips writing and does not throw")
  void testSendWithNullSocketChannelSkipsWriting() {
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    when(session.fetchSocketChannel()).thenReturn(null);
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue).clear();
  }

  @Test
  @DisplayName("send with closed socket channel skips writing and clears queue")
  void testSendWithClosedSocketChannelClearsQueue() {
    var closedChannel = mock(java.nio.channels.SocketChannel.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    when(session.fetchSocketChannel()).thenReturn(closedChannel);
    when(closedChannel.isOpen()).thenReturn(false);
    when(closedChannel.isConnected()).thenReturn(false);
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue).clear();
  }

  @Test
  @DisplayName("send with null packet encoder does not throw when channel is null")
  void testSendWithNullEncoderDoesNotThrowForNullChannel() {
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    when(session.fetchSocketChannel()).thenReturn(null);
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
  }

  @Test
  @DisplayName("send with empty packet data removes packet from queue")
  void testSendWithEmptyDataRemovesPacketFromQueue() {
    SocketChannel channel = mock(SocketChannel.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchSocketChannel()).thenReturn(channel);
    when(channel.isOpen()).thenReturn(true);
    when(channel.isConnected()).thenReturn(true);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.isFragmented()).thenReturn(false);
    when(packet.getData()).thenReturn(new byte[0]);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue).take();
  }

  @Test
  @DisplayName("send successfully writes all data, updates statistics and removes packet from queue")
  void testSendSuccessfullyWritesAllData() throws Exception {
    SocketChannel channel = mock(SocketChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchSocketChannel()).thenReturn(channel);
    when(channel.isOpen()).thenReturn(true);
    when(channel.isConnected()).thenReturn(true);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.isFragmented()).thenReturn(false);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(channel.write(any(ByteBuffer.class))).thenReturn(3);
    when(session.fectchSocketSelectionKey()).thenReturn(selectionKey);
    when(selectionKey.interestOps()).thenReturn(SelectionKey.OP_READ);
    when(packet.isMarkedAsLast()).thenReturn(false);
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue).take();
    verify(writerStatistic).updateWrittenPackets(1);
  }

  @Test
  @DisplayName("send with last packet closes session after writing")
  void testSendWithLastPacketClosesSession() throws Exception {
    SocketChannel channel = mock(SocketChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchSocketChannel()).thenReturn(channel);
    when(channel.isOpen()).thenReturn(true);
    when(channel.isConnected()).thenReturn(true);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.isFragmented()).thenReturn(false);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(channel.write(any(ByteBuffer.class))).thenReturn(3);
    when(session.fectchSocketSelectionKey()).thenReturn(selectionKey);
    when(selectionKey.interestOps()).thenReturn(SelectionKey.OP_READ);
    when(packet.isMarkedAsLast()).thenReturn(true);
    when(session.isActivated()).thenReturn(true);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(session).close(ConnectionDisconnectMode.CLIENT_REQUEST, PlayerDisconnectMode.CLIENT_REQUEST);
  }

  @Test
  @DisplayName("send with IOException clears queue and closes session if activated")
  void testSendWithIOExceptionClearsQueueAndClosesSession() throws Exception {
    SocketChannel channel = mock(SocketChannel.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchSocketChannel()).thenReturn(channel);
    when(channel.isOpen()).thenReturn(true);
    when(channel.isConnected()).thenReturn(true);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.isFragmented()).thenReturn(false);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(channel.write(any(ByteBuffer.class))).thenThrow(new java.io.IOException("write error"));
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue).clear();
  }

  @Test
  @DisplayName("send with null channel and activated session closes session")
  void testSendWithNullChannelAndActivatedSessionClosesSession() throws Exception {
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    when(session.fetchSocketChannel()).thenReturn(null);
    when(session.isActivated()).thenReturn(true);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue).clear();
    verify(session).close(ConnectionDisconnectMode.LOST_IN_WRITTEN, PlayerDisconnectMode.CONNECTION_LOST);
  }

  @Test
  @DisplayName("send with fragmented non-empty packet uses fragment buffer data")
  void testSendWithFragmentedNonEmptyPacketUsesFragmentBuffer() throws Exception {
    SocketChannel channel = mock(SocketChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchSocketChannel()).thenReturn(channel);
    when(channel.isOpen()).thenReturn(true);
    when(channel.isConnected()).thenReturn(true);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.isFragmented()).thenReturn(true);
    when(packet.getFragmentBuffer()).thenReturn(new byte[]{9, 8, 7});
    when(channel.write(any(ByteBuffer.class))).thenReturn(3);
    when(session.fectchSocketSelectionKey()).thenReturn(selectionKey);
    when(selectionKey.interestOps()).thenReturn(SelectionKey.OP_READ);
    when(packet.isMarkedAsLast()).thenReturn(false);
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue).take();
  }

  @Test
  @DisplayName("send with partial write sets fragment buffer on the packet")
  void testSendWithPartialWriteSetsFragmentBuffer() throws Exception {
    SocketChannel channel = mock(SocketChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchSocketChannel()).thenReturn(channel);
    when(channel.isOpen()).thenReturn(true);
    when(channel.isConnected()).thenReturn(true);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.isFragmented()).thenReturn(false);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3, 4, 5});
    when(channel.write(any(ByteBuffer.class))).thenAnswer(inv -> {
      ByteBuffer buf = inv.getArgument(0);
      int written = 2;
      buf.position(buf.position() + written);
      return written;
    });
    when(session.fectchSocketSelectionKey()).thenReturn(selectionKey);
    when(selectionKey.interestOps()).thenReturn(SelectionKey.OP_READ);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(packet).setFragmentBuffer(any(byte[].class));
  }

  @Test
  @DisplayName("send with IOException and activated session closes session")
  void testSendWithIOExceptionAndActivatedSessionClosesSession() throws Exception {
    SocketChannel channel = mock(SocketChannel.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchSocketChannel()).thenReturn(channel);
    when(channel.isOpen()).thenReturn(true);
    when(channel.isConnected()).thenReturn(true);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.isFragmented()).thenReturn(false);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(channel.write(any(ByteBuffer.class))).thenThrow(new java.io.IOException("write error"));
    when(session.isActivated()).thenReturn(true);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(session).close(ConnectionDisconnectMode.LOST_IN_WRITTEN, PlayerDisconnectMode.CONNECTION_LOST);
  }

  @Test
  @DisplayName("send removes OP_WRITE from interest ops when buffer is fully consumed")
  void testSendRemovesOpWriteWhenBufferFullyConsumed() throws Exception {
    SocketChannel channel = mock(SocketChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchSocketChannel()).thenReturn(channel);
    when(channel.isOpen()).thenReturn(true);
    when(channel.isConnected()).thenReturn(true);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.isFragmented()).thenReturn(false);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    // Consume entire buffer so hasRemaining() returns false
    when(channel.write(any(ByteBuffer.class))).thenAnswer(inv -> {
      ByteBuffer buf = inv.getArgument(0);
      int remaining = buf.remaining();
      buf.position(buf.limit());
      return remaining;
    });
    when(session.fectchSocketSelectionKey()).thenReturn(selectionKey);
    // Return OP_READ | OP_WRITE so the "remove OP_WRITE" branch is taken
    when(selectionKey.interestOps()).thenReturn(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    when(packet.isMarkedAsLast()).thenReturn(false);
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(selectionKey).interestOps(SelectionKey.OP_READ); // OP_WRITE removed
  }

  @Test
  @DisplayName("send with IOException on write and session.close() also throws does not propagate")
  void testSendWithIOExceptionOnWriteAndSessionCloseAlsoThrows() throws Exception {
    SocketChannel channel = mock(SocketChannel.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchSocketChannel()).thenReturn(channel);
    when(channel.isOpen()).thenReturn(true);
    when(channel.isConnected()).thenReturn(true);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.isFragmented()).thenReturn(false);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(channel.write(any(ByteBuffer.class))).thenThrow(new java.io.IOException("write error"));
    when(session.isActivated()).thenReturn(true);
    doThrow(new java.io.IOException("close error")).when(session)
        .close(ConnectionDisconnectMode.LOST_IN_WRITTEN, PlayerDisconnectMode.CONNECTION_LOST);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
  }

  @Test
  @DisplayName("send adds session back to tickets queue when queue is not empty")
  void testSendAddsSessionBackToTicketsQueue() throws Exception {
    SocketChannel channel = mock(SocketChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);
    SessionTicketsQueueManager queueManager = mock(SessionTicketsQueueManager.class);
    BlockingQueue<Session> sessionQueue = new LinkedBlockingQueue<>();

    handler.setPacketEncoder(encoder);
    handler.setSessionTicketsQueueManager(queueManager);
    when(session.fetchSocketChannel()).thenReturn(channel);
    when(channel.isOpen()).thenReturn(true);
    when(channel.isConnected()).thenReturn(true);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.isFragmented()).thenReturn(false);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(channel.write(any(ByteBuffer.class))).thenReturn(3);
    when(session.fectchSocketSelectionKey()).thenReturn(selectionKey);
    when(selectionKey.interestOps()).thenReturn(SelectionKey.OP_READ);
    when(packet.isMarkedAsLast()).thenReturn(false);
    when(session.isActivated()).thenReturn(true);
    when(session.getId()).thenReturn(1L);
    when(outboundQueue.isSnapshotEmpty()).thenReturn(false);
    when(queueManager.getQueueByElementId(1L)).thenReturn(sessionQueue);

    handler.send(outboundQueue, session, packet);

    assertTrue(sessionQueue.contains(session));
  }

  @Test
  @DisplayName("send with null channel, active session, and IOException on close does not throw")
  void testSendWithNullChannelActiveSessionCloseIOExceptionDoesNotThrow() throws Exception {
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    when(session.fetchSocketChannel()).thenReturn(null);
    when(session.isActivated()).thenReturn(true);
    doThrow(new java.io.IOException("close failed")).when(session)
        .close(ConnectionDisconnectMode.LOST_IN_WRITTEN, PlayerDisconnectMode.CONNECTION_LOST);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
  }

  @Test
  @DisplayName("send with last packet and IOException on session.close does not throw")
  void testSendWithLastPacketAndSessionCloseIOExceptionDoesNotThrow() throws Exception {
    SocketChannel channel = mock(SocketChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchSocketChannel()).thenReturn(channel);
    when(channel.isOpen()).thenReturn(true);
    when(channel.isConnected()).thenReturn(true);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.isFragmented()).thenReturn(false);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(channel.write(any(ByteBuffer.class))).thenAnswer(inv -> {
      ByteBuffer buf = inv.getArgument(0);
      int remaining = buf.remaining();
      buf.position(buf.limit());
      return remaining;
    });
    when(session.fectchSocketSelectionKey()).thenReturn(selectionKey);
    when(selectionKey.interestOps()).thenReturn(SelectionKey.OP_READ);
    when(packet.isMarkedAsLast()).thenReturn(true);
    when(session.isActivated()).thenReturn(true);
    doThrow(new java.io.IOException("close failed")).when(session)
        .close(ConnectionDisconnectMode.CLIENT_REQUEST, PlayerDisconnectMode.CLIENT_REQUEST);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
  }

  @Test
  @DisplayName("send with data larger than buffer reallocates buffer and writes successfully")
  void testSendWithLargeDataReallocatesBuffer() throws Exception {
    SocketChannel channel = mock(SocketChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);

    handler.setPacketEncoder(encoder);
    when(session.fetchSocketChannel()).thenReturn(channel);
    when(channel.isOpen()).thenReturn(true);
    when(channel.isConnected()).thenReturn(true);
    when(encoder.encode(packet)).thenReturn(packet);
    when(packet.isFragmented()).thenReturn(false);
    when(packet.getData()).thenReturn(new byte[600]); // 600 > 512 (default buffer)
    when(channel.write(any(ByteBuffer.class))).thenAnswer(inv -> {
      ByteBuffer buf = inv.getArgument(0);
      int remaining = buf.remaining();
      buf.position(buf.limit());
      return remaining;
    });
    when(session.fectchSocketSelectionKey()).thenReturn(selectionKey);
    when(selectionKey.interestOps()).thenReturn(SelectionKey.OP_READ);
    when(packet.isMarkedAsLast()).thenReturn(false);
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> handler.send(outboundQueue, session, packet));
    verify(outboundQueue).take();
    verify(writerStatistic).updateWrittenPackets(1);
  }
}
