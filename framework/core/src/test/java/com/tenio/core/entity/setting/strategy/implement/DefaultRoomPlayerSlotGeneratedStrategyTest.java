package com.tenio.core.entity.setting.strategy.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DefaultRoomPlayerSlotGeneratedStrategyTest {
  @Test
  void testConstructor() {
    DefaultRoomPlayerSlotGeneratedStrategy actualDefaultRoomPlayerSlotGeneratedStrategy =
        new DefaultRoomPlayerSlotGeneratedStrategy();
    actualDefaultRoomPlayerSlotGeneratedStrategy.setRoom(null);
    actualDefaultRoomPlayerSlotGeneratedStrategy.freeSlotWhenPlayerLeft(1);
    actualDefaultRoomPlayerSlotGeneratedStrategy.initialize();
    actualDefaultRoomPlayerSlotGeneratedStrategy.tryTakeSlot(1);
    assertEquals(0, actualDefaultRoomPlayerSlotGeneratedStrategy.getFreePlayerSlotInRoom());
  }
}

