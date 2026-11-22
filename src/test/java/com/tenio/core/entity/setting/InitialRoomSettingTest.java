/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.tenio.core.entity.setting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.setting.strategy.RoomCredentialValidatedStrategy;
import com.tenio.core.entity.setting.strategy.RoomPlayerSlotGeneratedStrategy;
import com.tenio.core.entity.setting.strategy.implement.DefaultRoomCredentialValidatedStrategy;
import com.tenio.core.entity.setting.strategy.implement.DefaultRoomPlayerSlotGeneratedStrategy;
import java.util.Map;
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

  @Test
  void testGettersReturnExpectedValues() {
    Map<String, Object> props = Map.of("key", "value");
    InitialRoomSetting setting = InitialRoomSetting.Builder.newInstance()
        .setName("room1")
        .setPassword("pass")
        .setMaxParticipants(10)
        .setMaxSpectators(5)
        .setActivated(true)
        .setRoomRemoveMode(RoomRemoveMode.NEVER_REMOVE)
        .setRoomCredentialValidatedStrategy(DefaultRoomCredentialValidatedStrategy.class)
        .setRoomPlayerSlotGeneratedStrategy(DefaultRoomPlayerSlotGeneratedStrategy.class)
        .setProperties(props)
        .build();

    assertEquals("room1", setting.getName());
    assertEquals("pass", setting.getPassword());
    assertEquals(10, setting.getMaxParticipants());
    assertEquals(5, setting.getMaxSpectators());
    assertTrue(setting.isActivated());
    assertEquals(RoomRemoveMode.NEVER_REMOVE, setting.getRoomRemoveMode());
    assertNotNull(setting.getRoomCredentialValidatedStrategy());
    assertNotNull(setting.getRoomPlayerSlotGeneratedStrategy());
    assertEquals(props, setting.getProperties());
    assertNotNull(setting.toString());
    assertTrue(setting.toString().contains("room1"));
  }

  @Test
  void testBuildWhenStrategiesArePreSet() {
    InitialRoomSetting setting = InitialRoomSetting.Builder.newInstance()
        .setRoomCredentialValidatedStrategy(DefaultRoomCredentialValidatedStrategy.class)
        .setRoomPlayerSlotGeneratedStrategy(DefaultRoomPlayerSlotGeneratedStrategy.class)
        .build();

    assertInstanceOf(DefaultRoomCredentialValidatedStrategy.class,
        setting.getRoomCredentialValidatedStrategy());
    assertInstanceOf(DefaultRoomPlayerSlotGeneratedStrategy.class,
        setting.getRoomPlayerSlotGeneratedStrategy());
  }
}
