package com.tenio.core.entity.define.mode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlayerDisconnectModeTest {
  @Test
  void testValueOf() {
    assertEquals("DEFAULT", PlayerDisconnectMode.valueOf("DEFAULT").toString());
  }
}

