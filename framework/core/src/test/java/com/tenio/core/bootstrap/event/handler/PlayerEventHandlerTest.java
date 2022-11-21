package com.tenio.core.bootstrap.event.handler;

import com.tenio.core.event.handler.implement.PlayerEventHandler;
import com.tenio.core.event.implement.EventManager;
import org.junit.jupiter.api.Test;

class PlayerEventHandlerTest {
  @Test
  void testInitialize() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     PlayerEventHandler.eventDisconnectPlayer
    //     PlayerEventHandler.eventPlayerLoggedInResult
    //     PlayerEventHandler.eventPlayerReconnectRequestHandle
    //     PlayerEventHandler.eventPlayerReconnectedResult
    //     PlayerEventHandler.eventReceivedMessageFromPlayer
    //     PlayerEventHandler.eventSendMessageToPlayer
    //     EventManager.eventProducer
    //     EventManager.eventSubscribers

    PlayerEventHandler playerEventHandler = new PlayerEventHandler();
    playerEventHandler.initialize(EventManager.newInstance());
  }
}
