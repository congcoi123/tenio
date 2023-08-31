package com.tenio.core.bootstrap.event.handler;

import com.tenio.core.event.handler.implement.ConnectionEventHandler;
import com.tenio.core.event.implement.EventManager;
import org.junit.jupiter.api.Test;

class ConnectionEventHandlerTest {

  @Test
  void testInitialize() {
    ConnectionEventHandler connectionEventHandler = new ConnectionEventHandler();
    connectionEventHandler.initialize(EventManager.newInstance());
  }
}
