/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.core.entity.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.RoomState;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.setting.strategy.RoomCredentialValidatedStrategy;
import com.tenio.core.entity.setting.strategy.RoomPlayerSlotGeneratedStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For DefaultRoom")
class DefaultRoomTest {

  @Test
  @DisplayName("Test creating a new instance")
  void testNewInstance() {
    Room actualNewInstanceResult = DefaultRoom.newInstance();
    assertFalse(actualNewInstanceResult.isActivated());
    assertEquals(0, actualNewInstanceResult.getSpectatorCount());
    assertEquals(RoomRemoveMode.WHEN_EMPTY, actualNewInstanceResult.getRoomRemoveMode());
    assertEquals(0, actualNewInstanceResult.getParticipantCount());
    assertTrue(actualNewInstanceResult.getOwner().isEmpty());
    assertEquals(0, actualNewInstanceResult.getMaxSpectators());
    assertEquals(0, actualNewInstanceResult.getMaxParticipants());
  }

  @Test
  @DisplayName("Test room getters/setters")
  void testSettersAndGetters() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    room.configurePlayerManager(mock(PlayerManager.class));
    room.configureRoomCredentialValidatedStrategy(mock(RoomCredentialValidatedStrategy.class));
    room.configurePlayerSlotGeneratedStrategy(mock(RoomPlayerSlotGeneratedStrategy.class));
    room.setName("Room1");
    room.setPassword("pass");
    room.setActivated(true);
    room.setMaxParticipants(5);
    room.setMaxSpectators(2);
    room.setCapacity(3, 4);
    room.setRoomRemoveMode(RoomRemoveMode.WHEN_EMPTY);
    room.setOwner(mock(Player.class));
    room.setState(null);
    assertEquals("Room1", room.getName());
    assertEquals("pass", room.getPassword());
    assertTrue(room.isActivated());
    assertEquals(3, room.getMaxParticipants());
    assertEquals(4, room.getMaxSpectators());
    assertEquals(7, room.getCapacity());
    assertEquals(RoomRemoveMode.WHEN_EMPTY, room.getRoomRemoveMode());
    assertTrue(room.getOwner().isPresent());
    assertNull(room.getState());
  }

  @Test
  @DisplayName("Test transitions of room states")
  void testStateTransitions() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    room.setState(null);
    assertTrue(room.isState(null));
    RoomState stateA = new RoomState() {
    };
    RoomState stateB = new RoomState() {
    };
    room.setState(stateA);
    assertTrue(room.isState(stateA));
    assertTrue(room.transitionState(stateA, stateB));
    assertTrue(room.isState(stateB));
    assertFalse(room.transitionState(stateA, stateB));
  }

  @Test
  @DisplayName("Test property getters/setters")
  void testPropertyMap() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    room.setProperty("key", "value");
    assertEquals("value", room.getProperty("key"));
    assertTrue(room.containsProperty("key"));
    room.removeProperty("key");
    assertFalse(room.containsProperty("key"));
    room.setProperty("a", 1);
    room.clearProperties();
    assertFalse(room.containsProperty("a"));
  }

  @Test
  @DisplayName("Test equals()/hashCode() methods")
  void testEqualsAndHashCode() {
    DefaultRoom room1 = (DefaultRoom) DefaultRoom.newInstance();
    DefaultRoom room2 = (DefaultRoom) DefaultRoom.newInstance();
    assertNotEquals(room1, room2);
    assertNotEquals(room1.hashCode(), room2.hashCode());
  }

  @Test
  @DisplayName("Test toString()")
  void testToString() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    assertTrue(room.toString().contains("DefaultRoom{"));
  }

  @Test
  @DisplayName("Test owner and activation getters/setters")
  void testOwnerAndActivation() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    assertTrue(room.getOwner().isEmpty());
    Player player = mock(Player.class);
    room.setOwner(player);
    assertTrue(room.getOwner().isPresent());
    room.setActivated(true);
    assertTrue(room.isActivated());
    room.setActivated(false);
    assertFalse(room.isActivated());
  }

  @Test
  @DisplayName("Test retrieving participants and spectators lists")
  void testParticipantAndSpectatorLists() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    room.configurePlayerManager(mock(PlayerManager.class));
    assertEquals(0, room.getReadonlyParticipantsList().size());
    assertEquals(0, room.getReadonlySpectatorsList().size());
    assertEquals(0, room.getReadonlyPlayersList().size());
  }

  @Test
  @DisplayName("Test public or private states")
  void testIsPublic() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    room.configureRoomCredentialValidatedStrategy(mock(RoomCredentialValidatedStrategy.class));
    room.setPassword(null);
    assertTrue(room.isPublic());
    room.setPassword("secret");
    assertFalse(room.isPublic());
  }

  @Test
  @DisplayName("Test room capacity and fullness")
  void testCapacityAndFullness() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    room.configurePlayerManager(pm);
    room.setMaxParticipants(2);
    room.setMaxSpectators(1);
    room.setCapacity(2, 1);
    org.mockito.Mockito.when(pm.getPlayerCount()).thenReturn(0);
    assertFalse(room.isFull());
    org.mockito.Mockito.when(pm.getPlayerCount()).thenReturn(3);
    assertTrue(room.isFull());
    org.mockito.Mockito.when(pm.getPlayerCount()).thenReturn(0);
    assertTrue(room.isEmpty());
  }

  @Test
  @DisplayName("Add a player with invalid password into a room should throw exception")
  void testExceptionOnInvalidAddPlayer() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    room.configurePlayerManager(mock(PlayerManager.class));
    room.configureRoomCredentialValidatedStrategy(mock(RoomCredentialValidatedStrategy.class));
    room.configurePlayerSlotGeneratedStrategy(mock(RoomPlayerSlotGeneratedStrategy.class));
    Player player = mock(Player.class);
    room.setPassword("pass");
    assertThrows(Exception.class, () -> room.addPlayer(player, "wrong", false, 0));
  }
}
