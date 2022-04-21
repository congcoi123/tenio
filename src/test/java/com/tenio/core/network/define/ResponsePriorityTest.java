package com.tenio.core.network.define;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResponsePriorityTest {
  @Test
  void testValueOf() {
    ResponsePriority actualValueOfResult = ResponsePriority.valueOf("NON_GUARANTEED");
    assertEquals(1, actualValueOfResult.getValue());
    assertEquals("NON_GUARANTEED", actualValueOfResult.toString());
  }
}

