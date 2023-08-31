package com.tenio.core.entity.define.mode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RoomRemoveModeTest {
  @Test
  void testValueOf() {
    assertEquals("WHEN_EMPTY", RoomRemoveMode.valueOf("WHEN_EMPTY").toString());
  }
}

