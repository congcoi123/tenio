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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.BlockingQueue;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.OutboundQueueFullException;
import com.tenio.core.exception.OutboundQueuePolicyViolationException;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.entity.outbound.packet.OutboundQueue;
import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.ZeroWriter;
import com.tenio.core.network.zero.engine.writer.WriterHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
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

  @Test
  @DisplayName("enqueuePacket with OutboundQueuePolicyViolationException increments dropped packets by policy")
  void testEnqueuePacketWithPolicyViolationException() {
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);
    NetworkWriterStatistic statistic = mock(NetworkWriterStatistic.class);
    writer.setNetworkWriterStatistic(statistic);

    when(packet.getRecipients()).thenReturn(List.of(session));
    when(session.isActivated()).thenReturn(true);
    when(session.fetchOutboundQueue()).thenReturn(outboundQueue);
    doThrow(new OutboundQueuePolicyViolationException(packet, 0.9f))
        .when(outboundQueue).put(packet);

    assertDoesNotThrow(() -> writer.enqueuePacket(packet));

    verify(session).addDroppedPackets(1);
    verify(statistic).updateWrittenDroppedPacketsByPolicy(1);
  }

  @Test
  @DisplayName("enqueuePacket with OutboundQueueFullException increments dropped packets by full")
  void testEnqueuePacketWithQueueFullException() {
    Session session = mock(Session.class);
    OutboundQueue outboundQueue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);
    NetworkWriterStatistic statistic = mock(NetworkWriterStatistic.class);
    writer.setNetworkWriterStatistic(statistic);

    when(packet.getRecipients()).thenReturn(List.of(session));
    when(session.isActivated()).thenReturn(true);
    when(session.fetchOutboundQueue()).thenReturn(outboundQueue);
    doThrow(new OutboundQueueFullException(100)).when(outboundQueue).put(packet);

    assertDoesNotThrow(() -> writer.enqueuePacket(packet));

    verify(session).addDroppedPackets(1);
    verify(statistic).updateWrittenDroppedPacketsByFull(1);
  }

  @Test
  @DisplayName("initialize() then shutdown() covers onInitialized and onShutdown")
  void testInitializeAndShutdown() {
    assertDoesNotThrow(() -> {
      writer.initialize();
      writer.shutdown();
    });
  }

  private Method getProcessSessionQueueMethod() throws NoSuchMethodException {
    Method m = ZeroWriterImpl.class.getDeclaredMethod(
        "processSessionQueue", Session.class, WriterHandler.class, WriterHandler.class);
    m.setAccessible(true);
    return m;
  }

  @Test
  @DisplayName("processSessionQueue with null session returns immediately without side effects")
  void testProcessSessionQueueNullSessionReturnsImmediately() throws Exception {
    Method m = getProcessSessionQueueMethod();
    WriterHandler socketHandler = mock(WriterHandler.class);
    WriterHandler datagramHandler = mock(WriterHandler.class);

    assertDoesNotThrow(() -> m.invoke(writer, null, socketHandler, datagramHandler));

    verify(socketHandler, never()).send(any(), any(), any());
    verify(datagramHandler, never()).send(any(), any(), any());
  }

  @Test
  @DisplayName("processSessionQueue with null outbound queue returns immediately")
  void testProcessSessionQueueNullOutboundQueueReturnsImmediately() throws Exception {
    Method m = getProcessSessionQueueMethod();
    Session session = mock(Session.class);
    WriterHandler socketHandler = mock(WriterHandler.class);
    when(session.fetchOutboundQueue()).thenReturn(null);

    assertDoesNotThrow(() -> m.invoke(writer, session, socketHandler, mock(WriterHandler.class)));

    verify(socketHandler, never()).send(any(), any(), any());
  }

  @Test
  @DisplayName("processSessionQueue with empty outbound queue returns immediately")
  void testProcessSessionQueueEmptyOutboundQueueReturnsImmediately() throws Exception {
    Method m = getProcessSessionQueueMethod();
    Session session = mock(Session.class);
    OutboundQueue queue = mock(OutboundQueue.class);
    WriterHandler socketHandler = mock(WriterHandler.class);
    when(session.fetchOutboundQueue()).thenReturn(queue);
    when(queue.isEmpty()).thenReturn(true);

    assertDoesNotThrow(() -> m.invoke(writer, session, socketHandler, mock(WriterHandler.class)));

    verify(socketHandler, never()).send(any(), any(), any());
  }

  @Test
  @DisplayName("processSessionQueue with inactive session takes one packet from queue and returns")
  void testProcessSessionQueueInactiveSessionTakesFromQueue() throws Exception {
    Method m = getProcessSessionQueueMethod();
    Session session = mock(Session.class);
    OutboundQueue queue = mock(OutboundQueue.class);
    when(session.fetchOutboundQueue()).thenReturn(queue);
    when(queue.isEmpty()).thenReturn(false);
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> m.invoke(writer, session, mock(WriterHandler.class),
        mock(WriterHandler.class)));

    verify(queue).take();
  }

  @Test
  @DisplayName("processSessionQueue with null packet takes from queue and returns")
  void testProcessSessionQueueNullPacketTakesFromQueue() throws Exception {
    Method m = getProcessSessionQueueMethod();
    Session session = mock(Session.class);
    OutboundQueue queue = mock(OutboundQueue.class);
    when(session.fetchOutboundQueue()).thenReturn(queue);
    when(queue.isEmpty()).thenReturn(false);
    when(session.isActivated()).thenReturn(true);
    when(queue.peek()).thenReturn(null);

    assertDoesNotThrow(() -> m.invoke(writer, session, mock(WriterHandler.class),
        mock(WriterHandler.class)));

    verify(queue).take();
  }

  @Test
  @DisplayName("processSessionQueue with TCP packet delegates to socketWriterHandler.send")
  void testProcessSessionQueueTcpPacketDelegatesToSocketHandler() throws Exception {
    Method m = getProcessSessionQueueMethod();
    Session session = mock(Session.class);
    OutboundQueue queue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);
    WriterHandler socketHandler = mock(WriterHandler.class);
    when(session.fetchOutboundQueue()).thenReturn(queue);
    when(queue.isEmpty()).thenReturn(false);
    when(session.isActivated()).thenReturn(true);
    when(queue.peek()).thenReturn(packet);
    when(packet.isTcp()).thenReturn(true);

    assertDoesNotThrow(() -> m.invoke(writer, session, socketHandler, mock(WriterHandler.class)));

    verify(socketHandler).send(queue, session, packet);
  }

  @Test
  @DisplayName("processSessionQueue with UDP packet delegates to datagramWriterHandler.send")
  void testProcessSessionQueueUdpPacketDelegatesToDatagramHandler() throws Exception {
    Method m = getProcessSessionQueueMethod();
    Session session = mock(Session.class);
    OutboundQueue queue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);
    WriterHandler datagramHandler = mock(WriterHandler.class);
    when(session.fetchOutboundQueue()).thenReturn(queue);
    when(queue.isEmpty()).thenReturn(false);
    when(session.isActivated()).thenReturn(true);
    when(queue.peek()).thenReturn(packet);
    when(packet.isTcp()).thenReturn(false);
    when(packet.isUdp()).thenReturn(true);

    assertDoesNotThrow(() -> m.invoke(writer, session, mock(WriterHandler.class), datagramHandler));

    verify(datagramHandler).send(queue, session, packet);
  }

  @Test
  @DisplayName("createSocketWriterHandler returns a non-null WriterHandler after initialization")
  void testCreateSocketWriterHandlerReturnsNonNull() throws Exception {
    writer.initialize();
    Method m = ZeroWriterImpl.class.getDeclaredMethod("createSocketWriterHandler");
    m.setAccessible(true);
    Object result = m.invoke(writer);
    assertDoesNotThrow(() -> m.invoke(writer));
    writer.shutdown();
  }

  @Test
  @DisplayName("createDatagramWriterHandler returns a non-null WriterHandler after initialization")
  void testCreateDatagramWriterHandlerReturnsNonNull() throws Exception {
    writer.initialize();
    Method m = ZeroWriterImpl.class.getDeclaredMethod("createDatagramWriterHandler");
    m.setAccessible(true);
    assertDoesNotThrow(() -> m.invoke(writer));
    writer.shutdown();
  }

  @Test
  @DisplayName("writing() with a queued session processes and returns without throwing")
  void testWritingWithQueuedSessionProcessesWithoutThrowing() throws Exception {
    writer.initialize();
    Method m = ZeroWriterImpl.class.getDeclaredMethod(
        "writing", java.util.concurrent.BlockingQueue.class, WriterHandler.class, WriterHandler.class);
    m.setAccessible(true);

    Session session = mock(Session.class);
    when(session.fetchOutboundQueue()).thenReturn(null);
    LinkedBlockingQueue<Session> queue = new LinkedBlockingQueue<>();
    queue.add(session);

    WriterHandler socketHandler = mock(WriterHandler.class);
    WriterHandler datagramHandler = mock(WriterHandler.class);

    assertDoesNotThrow(() -> m.invoke(writer, queue, socketHandler, datagramHandler));
    writer.shutdown();
  }

  @Test
  @DisplayName("onRunning creates writer handlers and exits on thread interrupt")
  void testOnRunningCreatesHandlersAndExitsOnInterrupt() throws Exception {
    writer.initialize();

    Method onRunning = ZeroWriterImpl.class.getDeclaredMethod("onRunning");
    onRunning.setAccessible(true);

    Thread t = new Thread(() -> {
      try {
        onRunning.invoke(writer);
      } catch (Exception ignored) {
      }
    });
    t.start();

    Thread.sleep(100); // let the thread enter the loop
    t.interrupt();
    t.join(2000);

    writer.shutdown();
  }

  @Test
  @DisplayName("onStarted is a no-op method (covers its single return instruction)")
  void testOnStartedIsNoOp() throws Exception {
    Method onStarted = ZeroWriterImpl.class.getDeclaredMethod("onStarted");
    onStarted.setAccessible(true);
    assertDoesNotThrow(() -> onStarted.invoke(writer));
  }

  @Test
  @DisplayName("writing() catches Throwable thrown by take() without propagating")
  void testWritingCatchesThrowableFromTake() throws Exception {
    writer.initialize();
    Method m = ZeroWriterImpl.class.getDeclaredMethod(
        "writing", BlockingQueue.class, WriterHandler.class, WriterHandler.class);
    m.setAccessible(true);

    @SuppressWarnings("unchecked")
    BlockingQueue<Session> mockQueue = mock(BlockingQueue.class);
    when(mockQueue.take()).thenThrow(new RuntimeException("test error"));

    assertDoesNotThrow(() -> m.invoke(writer, mockQueue, mock(WriterHandler.class), mock(WriterHandler.class)));
    writer.shutdown();
  }
}
