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

import com.tenio.core.entity.Player;
import com.tenio.core.entity.implement.DefaultPlayer;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.RemovedNonExistentPlayerException;
import java.util.ArrayList;
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
  @DisplayName("Test getPlayerCount is zero for fresh manager")
  void testGetPlayerCountInitiallyZero() {
    assertEquals(0, playerManager.getPlayerCount());
  }

  @Test
  @DisplayName("Test addPlayer increases player count")
  void testAddPlayerIncreasesCount() {
    Player player = DefaultPlayer.newInstance("alice");
    playerManager.addPlayer(player);
    assertEquals(1, playerManager.getPlayerCount());
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
    assertEquals(1, playerManager.getPlayerCount());
  }

  @Test
  @DisplayName("Test createPlayerWithSession with null session throws NullPointerException")
  void testCreatePlayerWithSessionNullThrows() {
    assertThrows(NullPointerException.class,
        () -> playerManager.createPlayerWithSession("dave", null));
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
  @DisplayName("Test getReadonlyPlayersList returns all added players")
  void testGetReadonlyPlayersList() {
    Player p1 = DefaultPlayer.newInstance("grace");
    Player p2 = DefaultPlayer.newInstance("henry");
    playerManager.addPlayer(p1);
    playerManager.addPlayer(p2);
    List<Player> list = playerManager.getReadonlyPlayersList();
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
  @DisplayName("Test removePlayerByIdentity decreases player count")
  void testRemovePlayerByIdentityDecreasesCount() {
    Player player = DefaultPlayer.newInstance("jack");
    playerManager.addPlayer(player);
    assertEquals(1, playerManager.getPlayerCount());
    playerManager.removePlayerByIdentity("jack");
    assertEquals(0, playerManager.getPlayerCount());
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
    assertEquals(2, playerManager.getPlayerCount());
    playerManager.clear();
    assertEquals(0, playerManager.getPlayerCount());
    assertTrue(playerManager.getReadonlyPlayersList().isEmpty());
  }

  @Test
  @DisplayName("Test configureMaxIdleTimeInSeconds does not throw")
  void testConfigureMaxIdleTimeInSeconds() {
    playerManager.configureMaxIdleTimeInSeconds(30);
    // no exception expected
  }

  @Test
  @DisplayName("Test configureMaxIdleTimeNeverDeportedInSeconds does not throw")
  void testConfigureMaxIdleTimeNeverDeportedInSeconds() {
    playerManager.configureMaxIdleTimeNeverDeportedInSeconds(60);
    // no exception expected
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
  @DisplayName("Test getReadonlyPlayersList is empty initially")
  void testGetReadonlyPlayersListInitiallyEmpty() {
    assertTrue(playerManager.getReadonlyPlayersList().isEmpty());
  }
}
