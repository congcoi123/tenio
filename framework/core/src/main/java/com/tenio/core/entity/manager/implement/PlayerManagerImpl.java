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

import com.tenio.core.entity.Player;
import com.tenio.core.entity.implement.DefaultPlayer;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.RemovedNonExistentPlayerException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.entity.session.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * An implemented class is for player management.
 */
public final class PlayerManagerImpl extends AbstractManager implements PlayerManager {

  private final Map<String, Player> players;
  private final Object playersLock;
  private volatile List<Player> snapshotPlayersList;
  private volatile int snapshotPlayerCount;
  private volatile int maxIdleTimeInSecond;

  private PlayerManagerImpl(EventManager eventManager) {
    super(eventManager);
    players = new HashMap<>();
    playersLock = new Object();
    snapshotPlayersList = new ArrayList<>();
  }

  /**
   * Creates a new instance of the player manager.
   *
   * @param eventManager the instance of {@link EventManager}
   * @return a new instance of {@link PlayerManager}
   */
  public static PlayerManager newInstance(EventManager eventManager) {
    return new PlayerManagerImpl(eventManager);
  }

  @Override
  public void addPlayer(Player player) {
    if (player == null) {
      throw new NullPointerException("Unable to process an unavailable player");
    }

    synchronized (playersLock) {
      if (players.containsKey(player.getIdentity())) {
        throw new AddedDuplicatedPlayerException(player);
      }
      configureInitialPlayer(player);
      players.put(player.getIdentity(), player);
      snapshotPlayersList = players.values().stream().toList();
      snapshotPlayerCount = players.size();
    }
  }

  @Override
  public Player createPlayer(String playerName) {
    Player player = DefaultPlayer.newInstance(playerName);
    addPlayer(player);
    return player;
  }

  @Override
  public Player createPlayerWithSession(String playerName, Session session) {
    if (session == null) {
      throw new NullPointerException("Unable to assign a null session to the player");
    }

    // Register the player before publishing the session association. The close handler can then
    // always find the player after it observes DONE.
    Player player = DefaultPlayer.newInstance(playerName);
    addPlayer(player);
    if (!associateSession(player, session)) {
      removePlayerByIdentity(playerName);
      throw new IllegalStateException("Unable to associate a closing session with player: "
          + playerName);
    }
    return player;
  }

  @Override
  public Player getPlayerByIdentity(String playerIdentity) {
    synchronized (playersLock) {
      return players.get(playerIdentity);
    }
  }

  @Override
  public void computePlayers(Consumer<Iterator<Player>> onComputed) {
    List<Player> playersSnapshot;
    synchronized (playersLock) {
      playersSnapshot = new ArrayList<>(players.values());
    }
    onComputed.accept(playersSnapshot.iterator());
  }

  @Override
  public List<Player> getSnapshotPlayersList() {
    return snapshotPlayersList;
  }

  @Override
  public List<Player> getPlayersList() {
    synchronized (playersLock) {
      snapshotPlayersList = players.values().stream().toList();
      return getSnapshotPlayersList();
    }
  }

  @Override
  public void removePlayerByIdentity(String playerIdentity) {
    synchronized (playersLock) {
      if (!players.containsKey(playerIdentity)) {
        throw new RemovedNonExistentPlayerException(playerIdentity);
      }
      players.remove(playerIdentity);
      snapshotPlayersList = players.values().stream().toList();
      snapshotPlayerCount = players.size();
    }
  }

  @Override
  public boolean containsPlayerIdentity(String playerIdentity) {
    synchronized (playersLock) {
      return players.containsKey(playerIdentity);
    }
  }

  @Override
  public int getSnapshotPlayerCount() {
    return snapshotPlayerCount;
  }

  @Override
  public int getPlayerCount() {
    synchronized (playersLock) {
      snapshotPlayerCount = players.size();
      return getSnapshotPlayerCount();
    }
  }

  @Override
  public void configureMaxIdleTimeInSeconds(int seconds) {
    maxIdleTimeInSecond = seconds;
  }

  @Override
  public void clear() {
    synchronized (playersLock) {
      players.clear();
      snapshotPlayersList = new ArrayList<>();
      snapshotPlayerCount = 0;
    }
  }

  /**
   * Configures basic info when a player is initially created, or before it is added into
   * the management list.
   *
   * @param player the target player
   */
  private void configureInitialPlayer(Player player) {
    // Room-owned player managers only track membership and intentionally have no reconnect
    // timeout. Do not let their default zero value overwrite the server player's configured
    // grace period when a player joins a room.
    if (maxIdleTimeInSecond > 0) {
      player.configureMaxIdleTimeInSeconds(maxIdleTimeInSecond);
    }
    player.setActivated(true);
    player.setLoggedIn(true);
  }

  /**
   * The session owns the synchronization for association publication, so close cannot change the
   * association to CLOSING between the state transition and the player-side reference update.
   */
  private boolean associateSession(Player player, Session session) {
    return session.associatePlayer(player);
  }
}
