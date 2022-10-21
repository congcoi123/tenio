package com.tenio.core.bootstrap.event.handler;

import com.tenio.core.event.implement.EventManager;
import org.junit.jupiter.api.Test;

class ConnectionEventHandlerTest {
  @Test
  void testInitialize() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     ConnectionEventHandler.eventAttachConnectionRequestValidation
    //     ConnectionEventHandler.eventAttachedConnectionResult
    //     ConnectionEventHandler.eventConnectionEstablishedResult
    //     ConnectionEventHandler.eventDisconnectConnection
    //     EventManager.eventProducer
    //     EventManager.eventSubscribers

    ConnectionEventHandler connectionEventHandler = new ConnectionEventHandler();
    connectionEventHandler.initialize(EventManager.newInstance());
  }
}

