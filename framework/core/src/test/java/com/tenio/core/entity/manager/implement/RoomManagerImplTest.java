package com.tenio.core.entity.manager.implement;

import com.tenio.core.event.implement.EventManager;
import org.junit.jupiter.api.Test;

class RoomManagerImplTest {
  @Test
  void testNewInstance() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     RoomManagerImpl.maxRooms
    //     RoomManagerImpl.roomByIds
    //     EventManager.eventProducer
    //     EventManager.eventSubscribers
    //     AbstractManager.eventManager

    RoomManagerImpl.newInstance(EventManager.newInstance());
  }
}

