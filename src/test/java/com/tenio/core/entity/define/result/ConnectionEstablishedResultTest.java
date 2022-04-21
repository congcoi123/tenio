package com.tenio.core.entity.define.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConnectionEstablishedResultTest {
  @Test
  void testValueOf() {
    assertEquals("SUCCESS", ConnectionEstablishedResult.valueOf("SUCCESS").toString());
  }
}

