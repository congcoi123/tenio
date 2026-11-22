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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.core.entity.Player.Field;
import java.util.concurrent.atomic.AtomicReference;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.PlayerState;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.room.PlayerRoleInRoom;
import com.tenio.core.network.entity.session.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For DefaultPlayer")
class DefaultPlayerTest {

  @Test
  @DisplayName("Test creating a new instance without session")
  void testNewInstance() {
    Player actualNewInstanceResult = DefaultPlayer.newInstance("Name");
    assertTrue(actualNewInstanceResult.getCurrentRoom().isEmpty());
    assertEquals(PlayerRoleInRoom.SPECTATOR, actualNewInstanceResult.getRoleInRoom());
    assertFalse(actualNewInstanceResult.isLoggedIn());
    assertFalse(actualNewInstanceResult.isInRoom());
    assertFalse(actualNewInstanceResult.isActivated());
    assertFalse(actualNewInstanceResult.getSession().isPresent());
    assertEquals("Name", actualNewInstanceResult.getIdentity());
    assertEquals(0L, actualNewInstanceResult.getLastLoggedInTime());
  }

  @Test
  @DisplayName("Test creating a new instance with session")
  void testNewInstance2() {
    Player actualNewInstanceResult = DefaultPlayer.newInstance("Name", mock(Session.class));
    assertTrue(actualNewInstanceResult.getCurrentRoom().isEmpty());
    assertEquals(PlayerRoleInRoom.SPECTATOR, actualNewInstanceResult.getRoleInRoom());
    assertFalse(actualNewInstanceResult.isLoggedIn());
    assertFalse(actualNewInstanceResult.isInRoom());
    assertFalse(actualNewInstanceResult.isActivated());
    assertEquals("Name", actualNewInstanceResult.getIdentity());
    assertEquals(0L, actualNewInstanceResult.getLastLoggedInTime());
  }

  @Test
  @DisplayName("Test transitions of player states")
  void testStateTransitions() {
    Player player = DefaultPlayer.newInstance("Test");
    player.setState(null);
    assertTrue(player.isState(null));
    PlayerState stateA = new PlayerState() {
    };
    PlayerState stateB = new PlayerState() {
    };
    assertTrue(player.transitionState(null, stateA));
    assertTrue(player.isState(stateA));
    assertFalse(player.transitionState(stateB, stateA));
  }

  @Test
  @DisplayName("Test activation and login setter/getter")
  void testActivationAndLogin() {
    Player player = DefaultPlayer.newInstance("Test");
    player.setActivated(true);
    assertTrue(player.isActivated());
    player.setLoggedIn(true);
    assertTrue(player.isLoggedIn());
    assertTrue(player.getLastLoggedInTime() > 0);
    player.setLoggedIn(false);
    assertFalse(player.isLoggedIn());
  }

  @Test
  @DisplayName("Test session setter/getter")
  void testSessionAssignment() {
    Player player = DefaultPlayer.newInstance("Test");
    Session session = mock(Session.class);
    player.setSession(session);
    assertTrue(player.getSession().isPresent());
    player.setSession(null);
    assertFalse(player.getSession().isPresent());
  }

  @Test
  @DisplayName("Reconnect grace period starts only after a session is detached")
  void testReconnectGracePeriod() throws Exception {
    Player player = DefaultPlayer.newInstance("Test", mock(Session.class));
    player.configureMaxIdleTimeInSeconds(1);
    assertFalse(player.isReconnectGracePeriodExpired());

    player.setSession(null);
    java.lang.reflect.Field field = DefaultPlayer.class.getDeclaredField("disconnectedTime");
    field.setAccessible(true);
    field.set(player, System.currentTimeMillis() - 2000L);

    assertTrue(player.isReconnectGracePeriodExpired());
    player.setSession(mock(Session.class));
    assertFalse(player.isReconnectGracePeriodExpired());
  }

  @Test
  @DisplayName("Test room setter/getter")
  void testRoomAssignment() {
    Player player = DefaultPlayer.newInstance("Test");
    assertFalse(player.isInRoom());
    Room room = mock(Room.class);
    player.setCurrentRoom(room);
    assertTrue(player.isInRoom());
    assertTrue(player.getCurrentRoom().isPresent());
    player.setCurrentRoom(null);
    assertFalse(player.isInRoom());
  }

  @Test
  @DisplayName("Test transitions of roles")
  void testRoleTransitions() {
    Player player = DefaultPlayer.newInstance("Test");
    assertTrue(player.transitionRole(PlayerRoleInRoom.SPECTATOR, PlayerRoleInRoom.PARTICIPANT));
    assertEquals(PlayerRoleInRoom.PARTICIPANT, player.getRoleInRoom());
    assertFalse(player.transitionRole(PlayerRoleInRoom.SPECTATOR, PlayerRoleInRoom.PARTICIPANT));
  }

  @Test
  @DisplayName("Test player slot getter/setter")
  void testPlayerSlot() {
    Player player = DefaultPlayer.newInstance("Test");
    player.setPlayerSlotInCurrentRoom(5);
    assertEquals(5, player.getPlayerSlotInCurrentRoom());
  }

  @Test
  @DisplayName("Test player property getter/setter")
  void testPropertyMap() {
    Player player = DefaultPlayer.newInstance("Test");
    player.setProperty("key", "value");
    assertEquals("value", player.getProperty("key"));
    assertTrue(player.containsProperty("key"));
    player.removeProperty("key");
    assertFalse(player.containsProperty("key"));
    player.setProperty("a", 1);
    player.clearProperties();
    assertFalse(player.containsProperty("a"));
  }

  @Test
  @DisplayName("Test equals() and hashCode() methods")
  void testEqualsAndHashCode() {
    Player player1 = DefaultPlayer.newInstance("Test");
    Player player2 = DefaultPlayer.newInstance("Test");
    Player player3 = DefaultPlayer.newInstance("Other");
    assertEquals(player1, player2);
    assertNotEquals(player1, player3);
    assertEquals(player1.hashCode(), player2.hashCode());
  }

  @Test
  @DisplayName("Test toString()")
  void testToString() {
    Player player = DefaultPlayer.newInstance("Test");
    assertTrue(player.toString().contains("identity='Test'"));
  }

  @Test
  @DisplayName("Test setLastReadTime and getLastReadTime")
  void testSetAndGetLastReadTime() {
    Player player = DefaultPlayer.newInstance("Test");
    long timestamp = System.currentTimeMillis();
    player.setLastReadTime(timestamp);
    assertEquals(timestamp, player.getLastReadTime());
    assertEquals(timestamp, player.getLastActivityTime());
  }

  @Test
  @DisplayName("Test setLastWriteTime and getLastWriteTime")
  void testSetAndGetLastWriteTime() {
    Player player = DefaultPlayer.newInstance("Test");
    long timestamp = System.currentTimeMillis() + 1000L;
    player.setLastWriteTime(timestamp);
    assertEquals(timestamp, player.getLastWriteTime());
    assertEquals(timestamp, player.getLastActivityTime());
  }

  @Test
  @DisplayName("Test getLastActivityTime is updated by setLastReadTime")
  void testGetLastActivityTime() {
    Player player = DefaultPlayer.newInstance("Test");
    long timestamp = System.currentTimeMillis() + 5000L;
    player.setLastReadTime(timestamp);
    assertEquals(timestamp, player.getLastActivityTime());
  }

  @Test
  @DisplayName("Test getLastJoinedRoomTime returns 0 initially")
  void testGetLastJoinedRoomTimeInitiallyZero() {
    Player player = DefaultPlayer.newInstance("Test");
    assertEquals(0L, player.getLastJoinedRoomTime());
  }

  @Test
  @DisplayName("Test setCurrentRoom updates lastJoinedRoomTime")
  void testSetCurrentRoomUpdatesLastJoinedRoomTime() {
    Player player = DefaultPlayer.newInstance("Test");
    Room room = mock(Room.class);
    player.setCurrentRoom(room);
    assertTrue(player.getLastJoinedRoomTime() > 0);
  }

  @Test
  @DisplayName("Test onUpdateListener is called on state changes")
  void testOnUpdateListenerTriggeredOnSetActivated() {
    Player player = DefaultPlayer.newInstance("Test");
    AtomicReference<Field> captured = new AtomicReference<>();
    player.onUpdateListener(captured::set);
    player.setActivated(true);
    assertNotNull(captured.get());
  }

  @Test
  @DisplayName("Test onUpdateListener is called on setProperty")
  void testOnUpdateListenerTriggeredOnSetProperty() {
    Player player = DefaultPlayer.newInstance("Test");
    AtomicReference<Field> captured = new AtomicReference<>();
    player.onUpdateListener(captured::set);
    player.setProperty("x", 1);
    assertEquals(Field.PROPERTY, captured.get());
  }

  @Test
  @DisplayName("Test onUpdateListener is called on setRoleInRoom")
  void testOnUpdateListenerTriggeredOnSetRole() {
    Player player = DefaultPlayer.newInstance("Test");
    AtomicReference<Field> captured = new AtomicReference<>();
    player.onUpdateListener(captured::set);
    player.setRoleInRoom(com.tenio.core.entity.define.room.PlayerRoleInRoom.PARTICIPANT);
    assertEquals(Field.ROLE_IN_ROOM, captured.get());
  }

  @Test
  @DisplayName("Test cleaning property")
  void testClean() {
    Player player = DefaultPlayer.newInstance("Test");
    player.setActivated(true);
    player.setProperty("a", 1);
    player.setCurrentRoom(mock(Room.class));
    player.setSession(mock(Session.class));
    player.clean();
    assertFalse(player.isActivated());
    assertFalse(player.isInRoom());
    assertFalse(player.getSession().isPresent());
    assertFalse(player.containsProperty("a"));
  }

  @Test
  @DisplayName("containsSession returns false when no session is set")
  void testContainsSessionReturnsFalseWhenNoSession() {
    Player player = DefaultPlayer.newInstance("Test");
    assertFalse(player.containsSession());
  }

  @Test
  @DisplayName("containsSession returns true after session is set")
  void testContainsSessionReturnsTrueAfterSessionSet() {
    Player player = DefaultPlayer.newInstance("Test");
    player.setSession(mock(com.tenio.core.network.entity.session.Session.class));
    assertTrue(player.containsSession());
  }

  @Test
  @DisplayName("hashCode returns 0 when identity is null")
  void testHashCodeWithNullIdentity() throws Exception {
    DefaultPlayer player = new DefaultPlayer(null);
    assertEquals(0, player.hashCode());
  }

  @Test
  void describesRoomAndOwnerWithCyclicProperties() {
    var player = DefaultPlayer.newInstance("TEST");
    var room = DefaultRoom.newInstance();
    room.setOwner(player);
    player.setCurrentRoom(room);
    player.setProperty("self", player);
    room.setProperty("self", room);

    assertTrue(player.toString().contains("currentRoomId=" + room.getId()));
    assertTrue(room.toString().contains("ownerIdentity=TEST"));
  }
}
