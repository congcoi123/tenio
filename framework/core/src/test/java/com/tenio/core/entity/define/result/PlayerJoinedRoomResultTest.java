package com.tenio.core.entity.define.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlayerJoinedRoomResultTest {
  @Test
  void testValueOf() {
    assertEquals("SUCCESS", PlayerJoinedRoomResult.valueOf("SUCCESS").toString());
  }
}

