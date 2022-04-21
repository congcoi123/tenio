package com.tenio.core.entity.define.mode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlayerBanModeTest {
  @Test
  void testValueOf() {
    assertEquals("BY_ADDRESS", PlayerBanMode.valueOf("BY_ADDRESS").toString());
  }
}

