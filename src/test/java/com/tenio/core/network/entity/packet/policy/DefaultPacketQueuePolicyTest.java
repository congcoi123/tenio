package com.tenio.core.network.entity.packet.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.implement.PacketQueueImpl;
import org.junit.jupiter.api.Test;

class DefaultPacketQueuePolicyTest {
  @Test
  void testApplyPolicy() {
    DefaultPacketQueuePolicy defaultPacketQueuePolicy = new DefaultPacketQueuePolicy();
    PacketQueueImpl newInstanceResult = PacketQueueImpl.newInstance();
    defaultPacketQueuePolicy.applyPolicy(newInstanceResult, mock(Packet.class));
    assertEquals(0, newInstanceResult.getMaxSize());
    assertTrue(newInstanceResult.isEmpty());
  }

  @Test
  void testApplyPolicy2() {
    DefaultPacketQueuePolicy defaultPacketQueuePolicy = new DefaultPacketQueuePolicy();
    PacketQueueImpl newInstanceResult = PacketQueueImpl.newInstance();
    defaultPacketQueuePolicy.applyPolicy(newInstanceResult, null);
    assertEquals(0, newInstanceResult.getMaxSize());
    assertTrue(newInstanceResult.isEmpty());
  }
}

