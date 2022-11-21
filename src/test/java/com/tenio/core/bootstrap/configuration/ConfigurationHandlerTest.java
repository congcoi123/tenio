package com.tenio.core.bootstrap.configuration;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ConfigurationHandlerTest {
  @Test
  void testConstructor() {
    assertNull((new ConfigurationHandler()).getConfiguration());
  }
}
