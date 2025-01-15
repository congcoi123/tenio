package com.tenio.core.entity.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.room.PlayerRoleInRoom;
import com.tenio.core.network.entity.session.Session;
import org.junit.jupiter.api.Test;

class DefaultPlayerTest {
  @Test
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
  void testNewInstance3() {
    Player actualNewInstanceResult = DefaultPlayer.newInstance("Name", null);
    assertTrue(actualNewInstanceResult.getCurrentRoom().isEmpty());
    assertEquals(PlayerRoleInRoom.SPECTATOR, actualNewInstanceResult.getRoleInRoom());
    assertFalse(actualNewInstanceResult.isLoggedIn());
    assertFalse(actualNewInstanceResult.isInRoom());
    assertFalse(actualNewInstanceResult.isActivated());
    assertFalse(actualNewInstanceResult.getSession().isPresent());
    assertEquals("Name", actualNewInstanceResult.getIdentity());
    assertEquals(0L, actualNewInstanceResult.getLastLoggedInTime());
  }
}

