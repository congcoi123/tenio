package com.tenio.core.entity.define.mode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RoomRemoveModeTest {
  @Test
  void testValueOf() {
    assertEquals("DEFAULT", RoomRemoveMode.valueOf("DEFAULT").toString());
  }
}

