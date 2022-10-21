package com.tenio.core.api;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.core.api.implement.ServerApiImpl;
import com.tenio.core.server.Server;
import org.junit.jupiter.api.Test;

class ServerApiImplTest {
  @Test
  void testNewInstance() {
    assertTrue(ServerApiImpl.newInstance(mock(Server.class)) instanceof ServerApiImpl);
    assertTrue(ServerApiImpl.newInstance(mock(Server.class)) instanceof ServerApiImpl);
  }

  @Test
  void testNewInstance2() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     ServerApiImpl.server

    ServerApiImpl.newInstance(null);
  }

  @Test
  void testNewInstance3() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     ServerApiImpl.server

    ServerApiImpl.newInstance(null);
  }
}

