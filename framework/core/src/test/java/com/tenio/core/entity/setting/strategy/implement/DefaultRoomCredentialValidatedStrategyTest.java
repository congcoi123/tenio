package com.tenio.core.entity.setting.strategy.implement;

import org.junit.jupiter.api.Test;

class DefaultRoomCredentialValidatedStrategyTest {
  @Test
  void testConstructor() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   There are no fields that could be asserted on.

    DefaultRoomCredentialValidatedStrategy actualDefaultRoomCredentialValidatedStrategy =
        new DefaultRoomCredentialValidatedStrategy();
    actualDefaultRoomCredentialValidatedStrategy.validateName("Name");
    actualDefaultRoomCredentialValidatedStrategy.validatePassword("iloveyou");
  }
}

