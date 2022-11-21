package com.tenio.core.bootstrap.event.handler;

import com.tenio.core.event.handler.implement.MixinsEventHandler;
import com.tenio.core.event.implement.EventManager;
import org.junit.jupiter.api.Test;

class MixinsEventHandlerTest {
  @Test
  void testInitialize() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     MixinsEventHandler.eventFetchedBandwidthInfo
    //     MixinsEventHandler.eventFetchedCcuInfo
    //     MixinsEventHandler.eventServerException
    //     MixinsEventHandler.eventServerInitialization
    //     MixinsEventHandler.eventServerTeardown
    //     MixinsEventHandler.eventSystemMonitoring
    //     EventManager.eventProducer
    //     EventManager.eventSubscribers

    MixinsEventHandler mixinsEventHandler = new MixinsEventHandler();
    mixinsEventHandler.initialize(EventManager.newInstance());
  }
}
