package com.tenio.core.entity.define.mode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlayerLeaveRoomModeTest {
  @Test
  void testValueOf() {
    assertEquals("DEFAULT", PlayerLeaveRoomMode.valueOf("DEFAULT").toString());
  }
}

