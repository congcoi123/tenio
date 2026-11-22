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

package com.tenio.core.entity.manager.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.implement.DefaultPlayer;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.RemovedNonExistentPlayerException;
import com.tenio.core.network.entity.session.Session;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For PlayerManagerImpl")
class PlayerManagerImplTest {

  private PlayerManager playerManager;

  @BeforeEach
  void setUp() {
    playerManager = PlayerManagerImpl.newInstance(EventManager.newInstance());
  }

  @Test
  @DisplayName("Test newInstance creates non-null manager")
  void testNewInstance() {
    assertNotNull(PlayerManagerImpl.newInstance(EventManager.newInstance()));
  }

  @Test
  @DisplayName("Test getSnapshotPlayerCount is zero for fresh manager")
  void testGetSnapshotPlayerCountInitiallyZero() {
    assertEquals(0, playerManager.getSnapshotPlayerCount());
  }

  @Test
  @DisplayName("Test addPlayer increases player count")
  void testAddPlayerIncreasesCount() {
    Player player = DefaultPlayer.newInstance("alice");
    playerManager.addPlayer(player);
    assertEquals(1, playerManager.getSnapshotPlayerCount());
  }

  @Test
  @DisplayName("Test addPlayer with null throws NullPointerException")
  void testAddPlayerNullThrows() {
    assertThrows(NullPointerException.class, () -> playerManager.addPlayer(null));
  }

  @Test
  @DisplayName("Test addPlayer duplicate throws AddedDuplicatedPlayerException")
  void testAddPlayerDuplicateThrows() {
    Player player = DefaultPlayer.newInstance("bob");
    playerManager.addPlayer(player);
    assertThrows(AddedDuplicatedPlayerException.class, () -> playerManager.addPlayer(player));
  }

  @Test
  @DisplayName("Test createPlayer creates and adds player")
  void testCreatePlayer() {
    Player player = playerManager.createPlayer("charlie");
    assertNotNull(player);
    assertEquals("charlie", player.getIdentity());
    assertEquals(1, playerManager.getSnapshotPlayerCount());
  }

  @Test
  @DisplayName("Test createPlayerWithSession with null session throws NullPointerException")
  void testCreatePlayerWithSessionNullThrows() {
    assertThrows(NullPointerException.class,
        () -> playerManager.createPlayerWithSession("dave", null));
  }

  @Test
  @DisplayName("Test createPlayerWithSession binds an active fresh session after registering player")
  void testCreatePlayerWithSession() {
    Session session = mock(Session.class);
    when(session.associatePlayer(org.mockito.ArgumentMatchers.any(Player.class))).thenReturn(true);

    Player player = playerManager.createPlayerWithSession("dave", session);

    assertEquals(player, playerManager.getPlayerByIdentity("dave"));
    verify(session).associatePlayer(player);
  }

  @Test
  @DisplayName("Test getPlayerByIdentity returns existing player")
  void testGetPlayerByIdentityFound() {
    Player player = DefaultPlayer.newInstance("eve");
    playerManager.addPlayer(player);
    assertEquals(player, playerManager.getPlayerByIdentity("eve"));
  }

  @Test
  @DisplayName("Test getPlayerByIdentity returns null for non-existent player")
  void testGetPlayerByIdentityNotFound() {
    assertNull(playerManager.getPlayerByIdentity("nobody"));
  }

  @Test
  @DisplayName("Test containsPlayerIdentity returns true when player exists")
  void testContainsPlayerIdentityTrue() {
    Player player = DefaultPlayer.newInstance("frank");
    playerManager.addPlayer(player);
    assertTrue(playerManager.containsPlayerIdentity("frank"));
  }

  @Test
  @DisplayName("Test containsPlayerIdentity returns false when player does not exist")
  void testContainsPlayerIdentityFalse() {
    assertFalse(playerManager.containsPlayerIdentity("ghost"));
  }

  @Test
  @DisplayName("Test getSnapshotPlayersList returns all added players")
  void testGetSnapshotPlayersList() {
    Player p1 = DefaultPlayer.newInstance("grace");
    Player p2 = DefaultPlayer.newInstance("henry");
    playerManager.addPlayer(p1);
    playerManager.addPlayer(p2);
    List<Player> list = playerManager.getSnapshotPlayersList();
    assertEquals(2, list.size());
  }

  @Test
  @DisplayName("Test computePlayers iterates all players")
  void testComputePlayers() {
    Player player = DefaultPlayer.newInstance("iris");
    playerManager.addPlayer(player);
    AtomicInteger count = new AtomicInteger(0);
    playerManager.computePlayers(iterator -> {
      while (iterator.hasNext()) {
        iterator.next();
        count.incrementAndGet();
      }
    });
    assertEquals(1, count.get());
  }

  @Test
  @DisplayName("computePlayers allows the callback to remove a player")
  void testComputePlayersAllowsCallbackMutation() {
    Player player = DefaultPlayer.newInstance("snapshot-player");
    playerManager.addPlayer(player);

    playerManager.computePlayers(iterator -> {
      while (iterator.hasNext()) {
        playerManager.removePlayerByIdentity(iterator.next().getIdentity());
      }
    });

    assertEquals(0, playerManager.getSnapshotPlayerCount());
  }

  @Test
  @DisplayName("Test removePlayerByIdentity decreases player count")
  void testRemovePlayerByIdentityDecreasesCount() {
    Player player = DefaultPlayer.newInstance("jack");
    playerManager.addPlayer(player);
    assertEquals(1, playerManager.getSnapshotPlayerCount());
    playerManager.removePlayerByIdentity("jack");
    assertEquals(0, playerManager.getSnapshotPlayerCount());
  }

  @Test
  @DisplayName("Test removePlayerByIdentity for non-existent player throws")
  void testRemovePlayerByIdentityNonExistentThrows() {
    assertThrows(RemovedNonExistentPlayerException.class,
        () -> playerManager.removePlayerByIdentity("unknown"));
  }

  @Test
  @DisplayName("Test clear removes all players")
  void testClearRemovesAllPlayers() {
    playerManager.addPlayer(DefaultPlayer.newInstance("kate"));
    playerManager.addPlayer(DefaultPlayer.newInstance("leo"));
    assertEquals(2, playerManager.getSnapshotPlayerCount());
    playerManager.clear();
    assertEquals(0, playerManager.getSnapshotPlayerCount());
    assertTrue(playerManager.getSnapshotPlayersList().isEmpty());
  }

  @Test
  @DisplayName("Test configureMaxIdleTimeInSeconds does not throw")
  void testConfigureMaxIdleTimeInSeconds() {
    playerManager.configureMaxIdleTimeInSeconds(30);
    // no exception expected
  }

  @Test
  @DisplayName("A room membership manager does not clear the configured reconnect grace period")
  void testRoomMembershipManagerKeepsReconnectGracePeriod() throws Exception {
    playerManager.configureMaxIdleTimeInSeconds(1);
    Player player = DefaultPlayer.newInstance("reconnecting", mock(Session.class));
    playerManager.addPlayer(player);

    PlayerManager roomPlayerManager = PlayerManagerImpl.newInstance(EventManager.newInstance());
    roomPlayerManager.addPlayer(player);
    player.setSession(null);
    java.lang.reflect.Field field = DefaultPlayer.class.getDeclaredField("disconnectedTime");
    field.setAccessible(true);
    field.set(player, System.currentTimeMillis() - 2000L);

    assertTrue(player.isReconnectGracePeriodExpired());
  }

  @Test
  @DisplayName("Test addPlayer activates and logs in the player")
  void testAddPlayerSetsActivatedAndLoggedIn() {
    Player player = DefaultPlayer.newInstance("mia");
    playerManager.addPlayer(player);
    assertTrue(player.isActivated());
    assertTrue(player.isLoggedIn());
  }

  @Test
  @DisplayName("Test getSnapshotPlayersList is empty initially")
  void testGetSnapshotPlayersListInitiallyEmpty() {
    assertTrue(playerManager.getSnapshotPlayersList().isEmpty());
  }
}
