package com.tenio.core.entity.define.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlayerLeftRoomResultTest {
  @Test
  void testValueOf() {
    assertEquals("SUCCESS", PlayerLeftRoomResult.valueOf("SUCCESS").toString());
  }
}

