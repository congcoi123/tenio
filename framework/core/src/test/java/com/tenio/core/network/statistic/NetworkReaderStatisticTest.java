package com.tenio.core.network.statistic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NetworkReaderStatisticTest {
  @Test
  void testNewInstance() {
    NetworkReaderStatistic actualNewInstanceResult = NetworkReaderStatistic.newInstance();
    actualNewInstanceResult.updateReadBytes(1L);
    actualNewInstanceResult.updateReadDroppedPackets(1L);
    actualNewInstanceResult.updateReadPackets(1L);
    assertEquals(1L, actualNewInstanceResult.getReadBytes());
    assertEquals(1L, actualNewInstanceResult.getReadDroppedPackets());
    assertEquals(1L, actualNewInstanceResult.getReadPackets());
  }

  @Test
  void testNewInstance2() {
    NetworkReaderStatistic actualNewInstanceResult = NetworkReaderStatistic.newInstance();
    assertEquals(0L, actualNewInstanceResult.getReadBytes());
    assertEquals(0L, actualNewInstanceResult.getReadPackets());
    assertEquals(0L, actualNewInstanceResult.getReadDroppedPackets());
  }
}

