package com.tenio.core.network.entity.session.implement;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.session.manager.SessionManagerImpl;
import org.junit.jupiter.api.Test;

class SessionManagerImplTest {
  @Test
  void testNewInstance() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     EventManager.eventProducer
    //     EventManager.eventSubscribers
    //     AbstractManager.eventManager
    //     SessionManagerImpl.packetQueuePolicy
    //     SessionManagerImpl.packetQueueSize
    //     SessionManagerImpl.sessionByDatagrams
    //     SessionManagerImpl.sessionByIds
    //     SessionManagerImpl.sessionBySockets
    //     SessionManagerImpl.sessionByWebSockets
    //     SessionManagerImpl.sessionCount

    SessionManagerImpl.newInstance(EventManager.newInstance());
  }
}

