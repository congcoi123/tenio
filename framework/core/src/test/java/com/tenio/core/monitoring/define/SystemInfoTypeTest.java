package com.tenio.core.monitoring.define;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SystemInfoTypeTest {
  @Test
  void testValueOf() {
    SystemInfoType actualValueOfResult = SystemInfoType.valueOf("OS_NAME");
    assertEquals("os.name", actualValueOfResult.getValue());
    assertEquals("OS_NAME", actualValueOfResult.toString());
  }
}

