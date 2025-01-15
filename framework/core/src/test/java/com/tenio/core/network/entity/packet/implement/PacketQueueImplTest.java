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
    actualNewInstanceResult.configureMaxSize(3);
    actualNewInstanceResult.configurePacketQueuePolicy(mock(PacketQueuePolicy.class));
    assertEquals(0, actualNewInstanceResult.getSize());
  }

  @Test
  void testGetPercentageUsed() {
    assertEquals(0.0f, PacketQueueImpl.newInstance().getPercentageUsed());
  }

  @Test
  void testGetPercentageUsed2() {
    PacketQueueImpl newInstanceResult = PacketQueueImpl.newInstance();
    newInstanceResult.configureMaxSize(3);
    assertEquals(0.0f, newInstanceResult.getPercentageUsed());
  }

  @Test
  void testClear() {
    PacketQueueImpl newInstanceResult = PacketQueueImpl.newInstance();
    newInstanceResult.clear();
    assertEquals("PacketQueue{queue=[], packetQueuePolicy=null, maxSize=0, size=0}", newInstanceResult.toString());
    assertTrue(newInstanceResult.isEmpty());
  }
}
