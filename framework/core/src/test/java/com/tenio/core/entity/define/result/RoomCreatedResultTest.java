package com.tenio.core.entity.define.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RoomCreatedResultTest {
  @Test
  void testValueOf() {
    assertEquals("SUCCESS", RoomCreatedResult.valueOf("SUCCESS").toString());
  }
}

