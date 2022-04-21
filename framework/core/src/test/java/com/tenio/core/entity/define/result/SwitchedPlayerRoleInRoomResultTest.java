package com.tenio.core.entity.define.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SwitchedPlayerRoleInRoomResultTest {
  @Test
  void testValueOf() {
    assertEquals("SUCCESS", SwitchedPlayerRoleInRoomResult.valueOf("SUCCESS").toString());
  }
}

