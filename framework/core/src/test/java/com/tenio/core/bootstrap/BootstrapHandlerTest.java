package com.tenio.core.bootstrap;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class BootstrapHandlerTest {
  @Test
  void testConstructor() {
    BootstrapHandler actualBootstrapHandler = new BootstrapHandler();
    assertNull(actualBootstrapHandler.getConfigurationHandler());
    assertNull(actualBootstrapHandler.getEventHandler());
  }
}

