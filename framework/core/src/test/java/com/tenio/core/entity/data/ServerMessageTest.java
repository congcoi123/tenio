package com.tenio.core.entity.data;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ServerMessageTest {
  @Test
  void testNewInstance() {
    ServerMessage actualNewInstanceResult = ServerMessage.newInstance();
    actualNewInstanceResult.setData(null);
    assertNull(actualNewInstanceResult.getData());
  }

  @Test
  void testNewInstance2() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by newInstance()
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    ServerMessage.newInstance();
  }
}

