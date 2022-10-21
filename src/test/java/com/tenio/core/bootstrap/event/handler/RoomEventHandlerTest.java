package com.tenio.core.bootstrap.event.handler;

import com.tenio.core.event.implement.EventManager;
import org.junit.jupiter.api.Test;

class RoomEventHandlerTest {
  @Test
  void testInitialize() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     RoomEventHandler.eventPlayerAfterLeftRoom
    //     RoomEventHandler.eventPlayerBeforeLeaveRoom
    //     RoomEventHandler.eventPlayerJoinedRoomResult
    //     RoomEventHandler.eventRoomCreatedResult
    //     RoomEventHandler.eventRoomWillBeRemoved
    //     RoomEventHandler.eventSwitchPlayerToSpectatorResult
    //     RoomEventHandler.eventSwitchSpectatorToPlayerResult
    //     EventManager.eventProducer
    //     EventManager.eventSubscribers

    RoomEventHandler roomEventHandler = new RoomEventHandler();
    roomEventHandler.initialize(EventManager.newInstance());
  }
}

