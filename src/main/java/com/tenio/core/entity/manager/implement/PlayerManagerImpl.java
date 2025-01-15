/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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
import java.util.Objects;

/**
 * An implemented class is for player management.
 */
public final class PlayerManagerImpl extends AbstractManager implements PlayerManager {

  private final Map<String, Player> players;
  private volatile List<Player> readOnlyPlayersList;
  private volatile int playerCount;
  private int maxIdleTimeInSecond;
  private int maxIdleTimeNeverDeportedInSecond;

  private PlayerManagerImpl(EventManager eventManager) {
    super(eventManager);
    players = new HashMap<>();
    readOnlyPlayersList = new ArrayList<>();
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
    if (containsPlayerIdentity(player.getIdentity())) {
      throw new AddedDuplicatedPlayerException(player);
    }

    synchronized (this) {
      players.put(player.getIdentity(), player);
      readOnlyPlayersList = players.values().stream().toList();
      playerCount = readOnlyPlayersList.size();
    }
  }

  @Override
  public Player createPlayer(String playerName) {
    Player player = DefaultPlayer.newInstance(playerName);
    configureInitialPlayer(player);
    addPlayer(player);
    return player;
  }

  @Override
  public Player createPlayerWithSession(String playerName, Session session) {
    if (Objects.isNull(session)) {
      throw new NullPointerException("Unable to assign a null session for the player");
    }

    Player player = DefaultPlayer.newInstance(playerName, session);
    configureInitialPlayer(player);
    addPlayer(player);
    return player;
  }

  @Override
  public void configureInitialPlayer(Player player) {
    if (Objects.isNull(player)) {
      throw new NullPointerException("Unable to process an unavailable player");
    }

    player.configureMaxIdleTimeInSeconds(maxIdleTimeInSecond);
    player.configureMaxIdleTimeNeverDeportedInSeconds(maxIdleTimeNeverDeportedInSecond);
    player.setActivated(true);
    player.setLoggedIn(true);
  }

  @Override
  public synchronized Player getPlayerByIdentity(String playerIdentity) {
    return players.get(playerIdentity);
  }

  @Override
  public synchronized Iterator<Player> getPlayerIterator() {
    return players.values().iterator();
  }

  @Override
  public List<Player> getReadonlyPlayersList() {
    return readOnlyPlayersList;
  }

  @Override
  public void removePlayerByIdentity(String playerIdentity) {
    if (!containsPlayerIdentity(playerIdentity)) {
      throw new RemovedNonExistentPlayerException(playerIdentity);
    }

    synchronized (this) {
      players.remove(playerIdentity);
      readOnlyPlayersList = players.values().stream().toList();
      playerCount = readOnlyPlayersList.size();
    }
  }

  @Override
  public synchronized boolean containsPlayerIdentity(String playerIdentity) {
    return players.containsKey(playerIdentity);
  }

  @Override
  public int getPlayerCount() {
    return playerCount;
  }

  @Override
  public void configureMaxIdleTimeInSeconds(int seconds) {
    maxIdleTimeInSecond = seconds;
  }

  @Override
  public void configureMaxIdleTimeNeverDeportedInSeconds(int seconds) {
    maxIdleTimeNeverDeportedInSecond = seconds;
  }

  @Override
  public synchronized void clear() {
    players.clear();
    readOnlyPlayersList = new ArrayList<>();
    playerCount = 0;
  }
}
