package com.tenio.core.network.entity.packet.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import org.junit.jupiter.api.Test;

class PacketQueueImplTest {
  @Test
  void testNewInstance() {
    PacketQueueImpl actualNewInstanceResult = PacketQueueImpl.newInstance();
    actualNewInstanceResult.setMaxSize(3);
    actualNewInstanceResult.setPacketQueuePolicy(mock(PacketQueuePolicy.class));
    assertEquals(3, actualNewInstanceResult.getMaxSize());
    assertEquals(0, actualNewInstanceResult.getSize());
    assertEquals("[]", actualNewInstanceResult.toString());
  }

  @Test
  void testGetPercentageUsed() {
    assertEquals(0.0f, PacketQueueImpl.newInstance().getPercentageUsed());
  }

  @Test
  void testGetPercentageUsed2() {
    PacketQueueImpl newInstanceResult = PacketQueueImpl.newInstance();
    newInstanceResult.setMaxSize(3);
    assertEquals(0.0f, newInstanceResult.getPercentageUsed());
  }

  @Test
  void testClear() {
    PacketQueueImpl newInstanceResult = PacketQueueImpl.newInstance();
    newInstanceResult.clear();
    assertEquals("[]", newInstanceResult.toString());
    assertTrue(newInstanceResult.isEmpty());
  }
}

