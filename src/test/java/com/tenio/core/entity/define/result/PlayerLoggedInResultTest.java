package com.tenio.core.entity.define.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlayerLoggedInResultTest {
  @Test
  void testValueOf() {
    assertEquals("SUCCESS", PlayerLoggedInResult.valueOf("SUCCESS").toString());
  }
}

