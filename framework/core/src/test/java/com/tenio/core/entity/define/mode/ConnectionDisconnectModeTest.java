package com.tenio.core.entity.define.mode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConnectionDisconnectModeTest {

  @Test
  void testValueOf() {
    assertEquals("DEFAULT", ConnectionDisconnectMode.valueOf("DEFAULT").toString());
  }
}

