package com.tenio.core.network.define;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RequestPriorityTest {
  @Test
  void testValueOf() {
    RequestPriority actualValueOfResult = RequestPriority.valueOf("LOWEST");
    assertEquals(1, actualValueOfResult.getValue());
    assertEquals("LOWEST", actualValueOfResult.toString());
  }
}

