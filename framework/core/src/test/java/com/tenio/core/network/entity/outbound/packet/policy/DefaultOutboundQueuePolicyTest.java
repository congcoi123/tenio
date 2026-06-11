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

package com.tenio.core.network.entity.outbound.packet.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenio.core.exception.OutboundQueueFullException;
import com.tenio.core.exception.OutboundQueuePolicyViolationException;
import com.tenio.core.network.define.ResponseGuarantee;
import com.tenio.core.network.entity.outbound.packet.OutboundQueue;
import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.outbound.packet.implement.OutboundQueueImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultOutboundQueuePolicyTest {

  private DefaultOutboundQueuePolicy policy;

  @BeforeEach
  void setUp() {
    policy = new DefaultOutboundQueuePolicy();
  }

  @Test
  void testApplyPolicy() {
    DefaultOutboundQueuePolicy defaultOutboundQueuePolicy = new DefaultOutboundQueuePolicy();
    OutboundQueueImpl newInstanceResult = OutboundQueueImpl.newInstance();
    assertThrows(OutboundQueueFullException.class, () -> defaultOutboundQueuePolicy.applyPolicy(newInstanceResult, mock(Packet.class)));
  }

  @Test
  void testAlmostFullQueueThrowsOutboundQueueFullException() {
    OutboundQueue queue = mock(OutboundQueue.class);
    when(queue.isSnapshotFull()).thenReturn(true);
    when(queue.getSnapshotSize()).thenReturn(5);

    assertThrows(OutboundQueueFullException.class,
        () -> policy.applyPolicy(queue, mock(Packet.class)));
  }

  @Test
  void testBetween75And90PercentWithNonGuaranteedThrowsPolicyViolation() {
    OutboundQueue queue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);
    when(queue.isSnapshotFull()).thenReturn(false);
    when(queue.getPercentageUsed()).thenReturn(80.0f);
    when(packet.getGuarantee()).thenReturn(ResponseGuarantee.NON_GUARANTEED);

    assertThrows(OutboundQueuePolicyViolationException.class,
        () -> policy.applyPolicy(queue, packet));
  }

  @Test
  void testBetween75And90PercentWithNormalGuaranteeDoesNotThrow() {
    OutboundQueue queue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);
    when(queue.isSnapshotFull()).thenReturn(false);
    when(queue.getPercentageUsed()).thenReturn(80.0f);
    when(packet.getGuarantee()).thenReturn(ResponseGuarantee.NORMAL);

    assertDoesNotThrow(() -> policy.applyPolicy(queue, packet));
  }

  @Test
  void testAt90PercentOrAboveWithNormalGuaranteeThrowsPolicyViolation() {
    OutboundQueue queue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);
    when(queue.isSnapshotFull()).thenReturn(false);
    when(queue.getPercentageUsed()).thenReturn(92.0f);
    when(packet.getGuarantee()).thenReturn(ResponseGuarantee.NORMAL);

    assertThrows(OutboundQueuePolicyViolationException.class,
        () -> policy.applyPolicy(queue, packet));
  }

  @Test
  void testAt90PercentOrAboveWithGuaranteedDoesNotThrow() {
    OutboundQueue queue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);
    when(queue.isSnapshotFull()).thenReturn(false);
    when(queue.getPercentageUsed()).thenReturn(92.0f);
    when(packet.getGuarantee()).thenReturn(ResponseGuarantee.GUARANTEED);

    assertDoesNotThrow(() -> policy.applyPolicy(queue, packet));
  }

  @Test
  void testBelow75PercentWithNonGuaranteedDoesNotThrow() {
    OutboundQueue queue = mock(OutboundQueue.class);
    Packet packet = mock(Packet.class);
    when(queue.isSnapshotFull()).thenReturn(false);
    when(queue.getPercentageUsed()).thenReturn(50.0f);
    when(packet.getGuarantee()).thenReturn(ResponseGuarantee.NON_GUARANTEED);

    assertDoesNotThrow(() -> policy.applyPolicy(queue, packet));
  }
}
