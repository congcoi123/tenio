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
import com.tenio.core.entity.Room;
import com.tenio.core.entity.implement.PlayerImpl;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.RemovedNonExistentPlayerFromRoomException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.entity.session.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.concurrent.GuardedBy;

/**
 * An implemented class is for player management.
 */
public final class PlayerManagerImpl extends AbstractManager implements PlayerManager {

  @GuardedBy("this")
  private final Map<String, Player> players;

  private List<Player> readonlyPlayersList;
  private volatile Room ownerRoom;
  private volatile int playerCount;
  private int maxIdleTimeInSecond;
  private int maxIdleTimeNeverDeportedInSecond;

  private PlayerManagerImpl(EventManager eventManager) {
    super(eventManager);
    players = new HashMap<>();
    readonlyPlayersList = new ArrayList<>();
    ownerRoom = null;
    playerCount = 0;
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
    if (containsPlayerName(player.getName())) {
      throw new AddedDuplicatedPlayerException(player, ownerRoom);
    }

    synchronized (this) {
      players.put(player.getName(), player);
      playerCount = players.size();
      readonlyPlayersList = List.copyOf(players.values());
    }
  }

  @Override
  public Player createPlayer(String playerName) {
    var player = PlayerImpl.newInstance(playerName);
    player.setActivated(true);
    player.setLoggedIn(true);
    player.setMaxIdleTimeInSeconds(maxIdleTimeInSecond);
    player.setMaxIdleTimeNeverDeportedInSeconds(maxIdleTimeNeverDeportedInSecond);
    addPlayer(player);

    return player;
  }

  @Override
  public Player createPlayerWithSession(String playerName, Session session) {
    if (Objects.isNull(session)) {
      throw new NullPointerException("Unable to assign a null session for the player");
    }

    var player = PlayerImpl.newInstance(playerName, session);
    player.setActivated(true);
    player.setLoggedIn(true);
    player.setMaxIdleTimeInSeconds(maxIdleTimeInSecond);
    player.setMaxIdleTimeNeverDeportedInSeconds(maxIdleTimeNeverDeportedInSecond);
    addPlayer(player);

    return player;
  }

  @Override
  public Player getPlayerByName(String playerName) {
    synchronized (players) {
      return players.get(playerName);
    }
  }

  @Override
  public Iterator<Player> getPlayerIterator() {
    synchronized (this) {
      return players.values().iterator();
    }
  }

  @Override
  public List<Player> getReadonlyPlayersList() {
    return readonlyPlayersList;
  }

  @Override
  public void removePlayerByName(String playerName) {
    if (!containsPlayerName(playerName)) {
      throw new RemovedNonExistentPlayerFromRoomException(playerName, ownerRoom);
    }

    removePlayer(playerName);
  }

  private void removePlayer(Player player) {
    synchronized (this) {
      players.remove(player.getName());
      playerCount = players.size();
      readonlyPlayersList = List.copyOf(players.values());
    }
  }

  private void removePlayer(String playerName) {
    synchronized (this) {
      players.remove(playerName);
      playerCount = players.size();
      readonlyPlayersList = List.copyOf(players.values());
    }
  }

  @Override
  public boolean containsPlayerName(String playerName) {
    synchronized (this) {
      return players.containsKey(playerName);
    }
  }

  @Override
  public Room getOwnerRoom() {
    return ownerRoom;
  }

  @Override
  public void setOwnerRoom(Room room) {
    ownerRoom = room;
  }

  @Override
  public int getPlayerCount() {
    return playerCount;
  }

  @Override
  public int getMaxIdleTimeInSeconds() {
    return maxIdleTimeInSecond;
  }

  @Override
  public void setMaxIdleTimeInSeconds(int seconds) {
    maxIdleTimeInSecond = seconds;
  }

  @Override
  public int getMaxIdleTimeNeverDeportedInSeconds() {
    return maxIdleTimeNeverDeportedInSecond;
  }

  @Override
  public void setMaxIdleTimeNeverDeportedInSeconds(int seconds) {
    maxIdleTimeNeverDeportedInSecond = seconds;
  }

  @Override
  public void clear() {
    synchronized (this) {
      var iterator = new ArrayList<>(players.values()).iterator();
      while (iterator.hasNext()) {
        var player = iterator.next();
        removePlayer(player);
      }
    }
  }
}
