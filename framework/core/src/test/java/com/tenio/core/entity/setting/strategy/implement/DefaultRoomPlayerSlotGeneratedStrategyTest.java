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

package com.tenio.core.entity.setting.strategy.implement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.tenio.core.entity.Room;
import com.tenio.core.entity.setting.strategy.RoomPlayerSlotGeneratedStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For DefaultRoomPlayerSlotGeneratedStrategy")
class DefaultRoomPlayerSlotGeneratedStrategyTest {

  private DefaultRoomPlayerSlotGeneratedStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new DefaultRoomPlayerSlotGeneratedStrategy();
  }

  @Test
  @DisplayName("implements RoomPlayerSlotGeneratedStrategy")
  void testImplementsInterface() {
    assertInstanceOf(RoomPlayerSlotGeneratedStrategy.class, strategy);
  }

  @Test
  @DisplayName("initialize does not throw")
  void testInitializeDoesNotThrow() {
    assertDoesNotThrow(() -> strategy.initialize());
  }

  @Test
  @DisplayName("getFreePlayerSlotInRoom always returns 0")
  void testGetFreePlayerSlotInRoomReturnsZero() {
    assertEquals(0, strategy.getFreePlayerSlotInRoom());
  }

  @Test
  @DisplayName("freeSlotWhenPlayerLeft does not throw")
  void testFreeSlotWhenPlayerLeftDoesNotThrow() {
    assertDoesNotThrow(() -> strategy.freeSlotWhenPlayerLeft(1));
    assertDoesNotThrow(() -> strategy.freeSlotWhenPlayerLeft(0));
    assertDoesNotThrow(() -> strategy.freeSlotWhenPlayerLeft(-1));
  }

  @Test
  @DisplayName("tryTakeSlot does not throw")
  void testTryTakeSlotDoesNotThrow() {
    assertDoesNotThrow(() -> strategy.tryTakeSlot(0));
    assertDoesNotThrow(() -> strategy.tryTakeSlot(5));
  }

  @Test
  @DisplayName("getRoom returns null before a room is set")
  void testGetRoomInitiallyNull() {
    assertNull(strategy.getRoom());
  }

  @Test
  @DisplayName("setRoom and getRoom work as a getter/setter pair")
  void testSetAndGetRoom() {
    Room room = mock(Room.class);
    strategy.setRoom(room);
    assertEquals(room, strategy.getRoom());
  }

  @Test
  @DisplayName("setRoom with null clears the room reference")
  void testSetRoomNullClearsRoom() {
    strategy.setRoom(mock(Room.class));
    strategy.setRoom(null);
    assertNull(strategy.getRoom());
  }
}
