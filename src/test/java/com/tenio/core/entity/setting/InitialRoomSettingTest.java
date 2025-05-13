package com.tenio.core.entity.setting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.setting.strategy.RoomCredentialValidatedStrategy;
import com.tenio.core.entity.setting.strategy.RoomPlayerSlotGeneratedStrategy;
import com.tenio.core.entity.setting.strategy.implement.DefaultRoomCredentialValidatedStrategy;
import com.tenio.core.entity.setting.strategy.implement.DefaultRoomPlayerSlotGeneratedStrategy;
import org.junit.jupiter.api.Test;

class InitialRoomSettingTest {
  @Test
  void testBuilderBuild() {
    InitialRoomSetting actualBuildResult = InitialRoomSetting.Builder.newInstance().build();
    assertEquals(0, actualBuildResult.getMaxParticipants());
    assertFalse(actualBuildResult.isActivated());
    assertEquals(RoomRemoveMode.WHEN_EMPTY, actualBuildResult.getRoomRemoveMode());
    assertInstanceOf(DefaultRoomPlayerSlotGeneratedStrategy.class, actualBuildResult
        .getRoomPlayerSlotGeneratedStrategy());
    assertInstanceOf(DefaultRoomCredentialValidatedStrategy.class, actualBuildResult
        .getRoomCredentialValidatedStrategy());
    assertEquals(0, actualBuildResult.getMaxSpectators());
    assertNull(actualBuildResult.getPassword());
    assertNull(actualBuildResult.getName());
  }

  @Test
  void testBuilderNewInstance() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     Builder.activated
    //     Builder.credentialValidatedStrategy
    //     Builder.maxPlayers
    //     Builder.maxSpectators
    //     Builder.name
    //     Builder.password
    //     Builder.playerSlotGeneratedStrategy
    //     Builder.removeMode

    InitialRoomSetting.Builder actualNewInstanceResult = InitialRoomSetting.Builder.newInstance();
    actualNewInstanceResult.setActivated(true);
    actualNewInstanceResult.setMaxParticipants(3);
    actualNewInstanceResult.setMaxSpectators(3);
    actualNewInstanceResult.setName("Name");
    actualNewInstanceResult.setPassword("iloveyou");
    actualNewInstanceResult.setRoomRemoveMode(RoomRemoveMode.WHEN_EMPTY);
  }

  @Test
  void testBuilderNewInstance2() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     Builder.activated
    //     Builder.credentialValidatedStrategy
    //     Builder.maxPlayers
    //     Builder.maxSpectators
    //     Builder.name
    //     Builder.password
    //     Builder.playerSlotGeneratedStrategy
    //     Builder.removeMode

    InitialRoomSetting.Builder.newInstance();
  }

  @Test
  void testBuilderSetRoomCredentialValidatedStrategy() {
    InitialRoomSetting.Builder newInstanceResult = InitialRoomSetting.Builder.newInstance();
    assertSame(newInstanceResult,
        newInstanceResult.setRoomCredentialValidatedStrategy(
            RoomCredentialValidatedStrategy.class));
  }

  @Test
  void testBuilderSetRoomCredentialValidatedStrategy2() {
    InitialRoomSetting.Builder newInstanceResult = InitialRoomSetting.Builder.newInstance();
    assertSame(newInstanceResult,
        newInstanceResult.setRoomCredentialValidatedStrategy(
            DefaultRoomCredentialValidatedStrategy.class));
  }

  @Test
  void testBuilderSetRoomPlayerSlotGeneratedStrategy() {
    InitialRoomSetting.Builder newInstanceResult = InitialRoomSetting.Builder.newInstance();
    assertSame(newInstanceResult,
        newInstanceResult.setRoomPlayerSlotGeneratedStrategy(
            RoomPlayerSlotGeneratedStrategy.class));
  }
}

