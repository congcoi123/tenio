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

package com.tenio.core.entity.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.RoomState;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.define.result.PlayerJoinedRoomResult;
import com.tenio.core.entity.define.result.SwitchedPlayerRoleInRoomResult;
import com.tenio.core.entity.define.room.PlayerRoleInRoom;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.setting.strategy.RoomCredentialValidatedStrategy;
import com.tenio.core.entity.setting.strategy.RoomPlayerSlotGeneratedStrategy;
import com.tenio.core.exception.PlayerJoinedRoomException;
import com.tenio.core.exception.SwitchedPlayerRoleInRoomException;
import java.util.List;
import java.util.function.Consumer;
import java.util.Iterator;
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

  @Test
  @DisplayName("Test getPlayerCount delegates to playerManager")
  void testGetPlayerCount() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    room.configurePlayerManager(pm);
    when(pm.getPlayerCount()).thenReturn(5);
    assertEquals(5, room.getPlayerCount());
  }

  @Test
  @DisplayName("Test containsPlayerIdentity delegates to playerManager")
  void testContainsPlayerIdentity() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    room.configurePlayerManager(pm);
    when(pm.containsPlayerIdentity("alice")).thenReturn(true);
    assertTrue(room.containsPlayerIdentity("alice"));
    assertFalse(room.containsPlayerIdentity("unknown"));
  }

  @Test
  @DisplayName("Test getPlayerByIdentity delegates to playerManager")
  void testGetPlayerByIdentity() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    room.configurePlayerManager(pm);
    Player player = mock(Player.class);
    when(pm.getPlayerByIdentity("alice")).thenReturn(player);
    assertTrue(room.getPlayerByIdentity("alice").isPresent());
    assertFalse(room.getPlayerByIdentity("nobody").isPresent());
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test computePlayers delegates to playerManager")
  void testComputePlayers() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    room.configurePlayerManager(pm);
    Consumer<Iterator<Player>> consumer = mock(Consumer.class);
    room.computePlayers(consumer);
    verify(pm).computePlayers(consumer);
  }

  @Test
  @DisplayName("Test addPlayer as participant with empty room succeeds")
  void testAddPlayerAsParticipantSuccess() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(4);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    Player player = mock(Player.class);
    room.addPlayer(player, null, false, 0);
    verify(player).setRoleInRoom(PlayerRoleInRoom.PARTICIPANT);
    verify(pm).addPlayer(player);
  }

  @Test
  @DisplayName("Test addPlayer as spectator with empty room succeeds")
  void testAddPlayerAsSpectatorSuccess() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxSpectators(2);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    Player player = mock(Player.class);
    room.addPlayer(player, null, true, 0);
    verify(player).setRoleInRoom(PlayerRoleInRoom.SPECTATOR);
    verify(player).setPlayerSlotInCurrentRoom(-1);
  }

  @Test
  @DisplayName("Test addPlayer throws when room is full for participants")
  void testAddPlayerThrowsWhenRoomFullForParticipants() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(0);
    Player player = mock(Player.class);
    PlayerJoinedRoomException ex = assertThrows(PlayerJoinedRoomException.class,
        () -> room.addPlayer(player, null, false, 0));
    assertEquals(PlayerJoinedRoomResult.ROOM_IS_FULL, ex.getResult());
  }

  @Test
  @DisplayName("Test removePlayer removes player and clears slot")
  void testRemovePlayer() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    Player player = mock(Player.class);
    when(player.getIdentity()).thenReturn("alice");
    when(player.getPlayerSlotInCurrentRoom()).thenReturn(1);
    room.removePlayer(player);
    verify(slotStrategy).freeSlotWhenPlayerLeft(1);
    verify(pm).removePlayerByIdentity("alice");
    verify(player).setCurrentRoom(null);
  }

  @Test
  @DisplayName("Test switchParticipantToSpectator throws when player not in room")
  void testSwitchParticipantToSpectatorPlayerNotInRoom() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    Player player = mock(Player.class);
    when(player.getIdentity()).thenReturn("alice");
    when(pm.containsPlayerIdentity("alice")).thenReturn(false);
    SwitchedPlayerRoleInRoomException ex = assertThrows(SwitchedPlayerRoleInRoomException.class,
        () -> room.switchParticipantToSpectator(player));
    assertEquals(SwitchedPlayerRoleInRoomResult.PLAYER_WAS_NOT_IN_ROOM, ex.getResult());
  }

  @Test
  @DisplayName("Test switchParticipantToSpectator succeeds when slots available")
  void testSwitchParticipantToSpectatorSuccess() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxSpectators(2);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    Player player = mock(Player.class);
    when(player.getIdentity()).thenReturn("alice");
    when(pm.containsPlayerIdentity("alice")).thenReturn(true);
    when(player.getPlayerSlotInCurrentRoom()).thenReturn(1);
    room.switchParticipantToSpectator(player);
    verify(player).setRoleInRoom(PlayerRoleInRoom.SPECTATOR);
    verify(slotStrategy).freeSlotWhenPlayerLeft(1);
  }

  @Test
  @DisplayName("Test switchSpectatorToParticipant throws when player not in room")
  void testSwitchSpectatorToParticipantPlayerNotInRoom() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    Player player = mock(Player.class);
    when(player.getIdentity()).thenReturn("bob");
    when(pm.containsPlayerIdentity("bob")).thenReturn(false);
    SwitchedPlayerRoleInRoomException ex = assertThrows(SwitchedPlayerRoleInRoomException.class,
        () -> room.switchSpectatorToParticipant(player, 0));
    assertEquals(SwitchedPlayerRoleInRoomResult.PLAYER_WAS_NOT_IN_ROOM, ex.getResult());
  }

  @Test
  @DisplayName("Test switchSpectatorToParticipant succeeds with auto slot")
  void testSwitchSpectatorToParticipantSuccessWithAutoSlot() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(4);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    when(slotStrategy.getFreePlayerSlotInRoom()).thenReturn(2);
    Player player = mock(Player.class);
    when(player.getIdentity()).thenReturn("bob");
    when(pm.containsPlayerIdentity("bob")).thenReturn(true);
    room.switchSpectatorToParticipant(player, 0);
    verify(player).setRoleInRoom(PlayerRoleInRoom.PARTICIPANT);
    verify(player).setPlayerSlotInCurrentRoom(2);
  }

  @Test
  @DisplayName("Test addPlayer(player) default method delegates to addPlayer(player, false)")
  void testAddPlayerDefaultMethodDelegatesToTwoArg() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(4);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    Player player = mock(Player.class);
    room.addPlayer(player); // calls addPlayer(player, false) → addPlayer(player, false, 0) → addPlayer(player, null, false, 0)
    verify(player).setRoleInRoom(PlayerRoleInRoom.PARTICIPANT);
  }

  @Test
  @DisplayName("Test addPlayer(player, asSpectator) default method delegates to addPlayer with slot")
  void testAddPlayerTwoArgDefaultMethod() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(4);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    Player player = mock(Player.class);
    room.addPlayer(player, false); // calls addPlayer(player, false, DEFAULT_SLOT)
    verify(player).setRoleInRoom(PlayerRoleInRoom.PARTICIPANT);
  }

  @Test
  @DisplayName("Test addPlayer(player, asSpectator, targetSlot) default method")
  void testAddPlayerThreeArgDefaultMethod() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(4);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    Player player = mock(Player.class);
    room.addPlayer(player, false, 0); // calls addPlayer(player, null, false, 0)
    verify(player).setRoleInRoom(PlayerRoleInRoom.PARTICIPANT);
  }

  @Test
  @DisplayName("Test switchSpectatorToParticipant(player) default method")
  void testSwitchSpectatorToParticipantDefaultMethod() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(4);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    when(slotStrategy.getFreePlayerSlotInRoom()).thenReturn(1);
    Player player = mock(Player.class);
    when(player.getIdentity()).thenReturn("carol");
    when(pm.containsPlayerIdentity("carol")).thenReturn(true);
    room.switchSpectatorToParticipant(player); // calls switchSpectatorToParticipant(player, DEFAULT_SLOT=0)
    verify(player).setRoleInRoom(PlayerRoleInRoom.PARTICIPANT);
  }

  @Test
  @DisplayName("Test clear() default method throws UnsupportedOperationException")
  void testClearDefaultMethodThrows() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    assertThrows(UnsupportedOperationException.class, room::clear);
  }

  @Test
  @DisplayName("Test removePlayer clears owner when removed player is the room owner")
  void testRemovePlayerClearsOwnerWhenOwner() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    Player owner = mock(Player.class);
    when(owner.getIdentity()).thenReturn("owner");
    when(owner.getPlayerSlotInCurrentRoom()).thenReturn(0);
    room.setOwner(owner);
    room.removePlayer(owner);
    assertTrue(room.getOwner().isEmpty());
  }

  @Test
  @DisplayName("Test switchParticipantToSpectator throws when spectators full")
  void testSwitchParticipantToSpectatorThrowsWhenSpectatorsFull() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxSpectators(0);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    Player player = mock(Player.class);
    when(player.getIdentity()).thenReturn("alice");
    when(pm.containsPlayerIdentity("alice")).thenReturn(true);
    SwitchedPlayerRoleInRoomException ex = assertThrows(SwitchedPlayerRoleInRoomException.class,
        () -> room.switchParticipantToSpectator(player));
    assertEquals(SwitchedPlayerRoleInRoomResult.SWITCH_NO_SPECTATOR_SLOTS_AVAILABLE, ex.getResult());
  }

  @Test
  @DisplayName("Test switchSpectatorToParticipant throws when participants full")
  void testSwitchSpectatorToParticipantThrowsWhenParticipantsFull() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(0);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    Player player = mock(Player.class);
    when(player.getIdentity()).thenReturn("bob");
    when(pm.containsPlayerIdentity("bob")).thenReturn(true);
    SwitchedPlayerRoleInRoomException ex = assertThrows(SwitchedPlayerRoleInRoomException.class,
        () -> room.switchSpectatorToParticipant(player, 0));
    assertEquals(SwitchedPlayerRoleInRoomResult.SWITCH_NO_PARTICIPANT_SLOTS_AVAILABLE,
        ex.getResult());
  }

  @Test
  @DisplayName("Test addPlayer with specific valid slot succeeds")
  void testAddPlayerWithSpecificSlotSuccess() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(4);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    Player player = mock(Player.class);
    room.addPlayer(player, null, false, 2);
    verify(slotStrategy).tryTakeSlot(2);
    verify(player).setPlayerSlotInCurrentRoom(2);
  }

  @Test
  @DisplayName("Test addPlayer with specific slot that is unavailable throws")
  void testAddPlayerWithUnavailableSlotThrows() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(4);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    org.mockito.Mockito.doThrow(new IllegalArgumentException("slot taken"))
        .when(slotStrategy).tryTakeSlot(3);
    Player player = mock(Player.class);
    PlayerJoinedRoomException ex = assertThrows(PlayerJoinedRoomException.class,
        () -> room.addPlayer(player, null, false, 3));
    assertEquals(PlayerJoinedRoomResult.SLOT_UNAVAILABLE_IN_ROOM, ex.getResult());
  }

  @Test
  @DisplayName("Test switchSpectatorToParticipant with specific valid slot succeeds")
  void testSwitchSpectatorToParticipantWithSpecificSlotSuccess() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(4);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    Player player = mock(Player.class);
    when(player.getIdentity()).thenReturn("carol");
    when(pm.containsPlayerIdentity("carol")).thenReturn(true);
    room.switchSpectatorToParticipant(player, 2);
    verify(slotStrategy).tryTakeSlot(2);
    verify(player).setPlayerSlotInCurrentRoom(2);
    verify(player).setRoleInRoom(PlayerRoleInRoom.PARTICIPANT);
  }

  @Test
  @DisplayName("Test switchSpectatorToParticipant with unavailable specific slot throws")
  void testSwitchSpectatorToParticipantWithUnavailableSlotThrows() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(4);
    when(pm.getReadonlyPlayersList()).thenReturn(List.of());
    org.mockito.Mockito.doThrow(new IllegalArgumentException("slot taken"))
        .when(slotStrategy).tryTakeSlot(5);
    Player player = mock(Player.class);
    when(player.getIdentity()).thenReturn("dave");
    when(pm.containsPlayerIdentity("dave")).thenReturn(true);
    SwitchedPlayerRoleInRoomException ex = assertThrows(SwitchedPlayerRoleInRoomException.class,
        () -> room.switchSpectatorToParticipant(player, 5));
    assertEquals(SwitchedPlayerRoleInRoomResult.SLOT_UNAVAILABLE_IN_ROOM, ex.getResult());
  }

  @Test
  @DisplayName("classifyPlayersByRoles filters participants and spectators from non-empty list")
  void testClassifyPlayersByRolesWithNonEmptyList() {
    DefaultRoom room = (DefaultRoom) DefaultRoom.newInstance();
    PlayerManager pm = mock(PlayerManager.class);
    RoomPlayerSlotGeneratedStrategy slotStrategy = mock(RoomPlayerSlotGeneratedStrategy.class);
    RoomCredentialValidatedStrategy credStrategy = mock(RoomCredentialValidatedStrategy.class);
    room.configurePlayerManager(pm);
    room.configurePlayerSlotGeneratedStrategy(slotStrategy);
    room.configureRoomCredentialValidatedStrategy(credStrategy);
    room.setMaxParticipants(4);
    room.setMaxSpectators(2);

    Player participant = mock(Player.class);
    when(participant.getRoleInRoom()).thenReturn(PlayerRoleInRoom.PARTICIPANT);
    Player spectator = mock(Player.class);
    when(spectator.getRoleInRoom()).thenReturn(PlayerRoleInRoom.SPECTATOR);

    // Return both players so the filter lambdas run over a non-empty stream
    when(pm.getReadonlyPlayersList()).thenReturn(List.of(participant, spectator));

    // Triggering addPlayer calls classifyPlayersByRoles internally
    Player newPlayer = mock(Player.class);
    room.addPlayer(newPlayer, null, false, 0);
  }
}
