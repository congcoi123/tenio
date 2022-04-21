package com.tenio.core.network.statistic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NetworkWriterStatisticTest {
  @Test
  void testNewInstance() {
    NetworkWriterStatistic actualNewInstanceResult = NetworkWriterStatistic.newInstance();
    actualNewInstanceResult.updateWrittenBytes(1L);
    actualNewInstanceResult.updateWrittenDroppedPacketsByFull(1L);
    actualNewInstanceResult.updateWrittenDroppedPacketsByPolicy(1L);
    actualNewInstanceResult.updateWrittenPackets(1L);
    assertEquals(1L, actualNewInstanceResult.getWrittenBytes());
    assertEquals(1L, actualNewInstanceResult.getWrittenDroppedPacketsByFull());
    assertEquals(1L, actualNewInstanceResult.getWrittenDroppedPacketsByPolicy());
    assertEquals(1L, actualNewInstanceResult.getWrittenPackets());
  }

  @Test
  void testNewInstance2() {
    NetworkWriterStatistic actualNewInstanceResult = NetworkWriterStatistic.newInstance();
    assertEquals(0L, actualNewInstanceResult.getWrittenBytes());
    assertEquals(0L, actualNewInstanceResult.getWrittenPackets());
    assertEquals(0L, actualNewInstanceResult.getWrittenDroppedPacketsByPolicy());
    assertEquals(0L, actualNewInstanceResult.getWrittenDroppedPacketsByFull());
  }

  @Test
  void testGetWrittenDroppedPackets() {
    assertEquals(0L, NetworkWriterStatistic.newInstance().getWrittenDroppedPackets());
  }
}

