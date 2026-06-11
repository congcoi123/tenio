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

package com.tenio.core.network.entity.outbound.packet.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.outbound.packet.policy.OutboundQueuePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OutboundQueueImplTest {

  private OutboundQueueImpl queue;
  private OutboundQueuePolicy policy;

  @BeforeEach
  void setUp() {
    queue = OutboundQueueImpl.newInstance();
    policy = mock(OutboundQueuePolicy.class);
    queue.configureMaxSize(10);
    queue.configureOutboundQueuePolicy(policy);
  }

  @Test
  void testNewInstance() {
    OutboundQueueImpl actualNewInstanceResult = OutboundQueueImpl.newInstance();
    actualNewInstanceResult.configureMaxSize(3);
    actualNewInstanceResult.configureOutboundQueuePolicy(mock(OutboundQueuePolicy.class));
    assertEquals(0, actualNewInstanceResult.getSnapshotSize());
  }

  @Test
  void testGetPercentageUsed() {
    assertEquals(0.0f, OutboundQueueImpl.newInstance().getPercentageUsed());
  }

  @Test
  void testGetPercentageUsed2() {
    OutboundQueueImpl newInstanceResult = OutboundQueueImpl.newInstance();
    newInstanceResult.configureMaxSize(3);
    assertEquals(0.0f, newInstanceResult.getPercentageUsed());
  }

  @Test
  void testClear() {
    OutboundQueueImpl newInstanceResult = OutboundQueueImpl.newInstance();
    newInstanceResult.clear();
    assertEquals("OutboundQueue{queue=[], outboundQueuePolicy=null, maxSize=0, snapshotSize=0}", newInstanceResult.toString());
    assertTrue(newInstanceResult.isSnapshotEmpty());
  }

  @Test
  void testIsSnapshotEmptyOnNewQueue() {
    assertTrue(queue.isSnapshotEmpty());
  }

  @Test
  void testPeekOnEmptyQueueReturnsNull() {
    assertNull(queue.peek());
  }

  @Test
  void testTakeOnEmptyQueueReturnsNull() {
    assertNull(queue.take());
  }

  @Test
  void testPutThenPeekReturnsSamePacket() {
    Packet packet = mock(Packet.class);
    queue.put(packet);
    assertEquals(packet, queue.peek());
    // peek does not remove
    assertEquals(1, queue.getSnapshotSize());
  }

  @Test
  void testPutThenTakeReturnsSamePacket() {
    Packet packet = mock(Packet.class);
    queue.put(packet);
    assertEquals(packet, queue.take());
    // take removes from queue
    assertTrue(queue.isSnapshotEmpty());
  }

  @Test
  void testIsSnapshotEmptyFalseAfterPut() {
    queue.put(mock(Packet.class));
    assertFalse(queue.isSnapshotEmpty());
  }

  @Test
  void testIsSnapshotFullWhenSizeEqualsMaxSize() {
    OutboundQueueImpl q = OutboundQueueImpl.newInstance();
    q.configureMaxSize(1);
    q.configureOutboundQueuePolicy(mock(OutboundQueuePolicy.class));
    q.put(mock(Packet.class));
    assertTrue(q.isSnapshotFull());
  }

  @Test
  void testGetPercentageUsedAfterPut() {
    // maxSize=10, 1 packet → 10%
    queue.put(mock(Packet.class));
    assertEquals(10.0f, queue.getPercentageUsed(), 0.01f);
  }

  @Test
  void testClearAfterPutEmptiesQueue() {
    queue.put(mock(Packet.class));
    queue.put(mock(Packet.class));
    queue.clear();
    assertTrue(queue.isSnapshotEmpty());
    assertEquals(0, queue.getSnapshotSize());
  }

  @Test
  void testPutInvokesPolicyApplyPolicy() {
    Packet packet = mock(Packet.class);
    queue.put(packet);
    verify(policy).applyPolicy(queue, packet);
  }

  @Test
  void testGetSnapshotSizeReflectsQueueSnapshotSize() {
    assertEquals(0, queue.getSnapshotSize());
    queue.put(mock(Packet.class));
    assertEquals(1, queue.getSnapshotSize());
    queue.put(mock(Packet.class));
    assertEquals(2, queue.getSnapshotSize());
    queue.take();
    assertEquals(1, queue.getSnapshotSize());
  }
}
