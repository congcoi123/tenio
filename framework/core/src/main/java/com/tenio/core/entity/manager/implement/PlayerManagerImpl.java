/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.GuardedBy;

/**
 * An implemented class is for player management.
 */
public final class PlayerManagerImpl extends AbstractManager implements PlayerManager {

  @GuardedBy("this")
  private final Map<String, Player> playerByNames;
  @GuardedBy("this")
  private final Map<Session, Player> playerBySessions;

  private Room ownerRoom;

  private volatile int playerCount;

  private PlayerManagerImpl(EventManager eventManager) {
    super(eventManager);

    playerByNames = new HashMap<String, Player>();
    playerBySessions = new HashMap<Session, Player>();

    ownerRoom = null;

    playerCount = 0;
  }

  public static PlayerManager newInstance(EventManager eventManager) {
    return new PlayerManagerImpl(eventManager);
  }

  @Override
  public void addPlayer(Player player) {
    if (containsPlayerName(player.getName())) {
      throw new AddedDuplicatedPlayerException(player, ownerRoom);
    }

    synchronized (this) {
      playerByNames.put(player.getName(), player);
      if (player.containsSession()) {
        playerBySessions.put(player.getSession(), player);
      }
      playerCount = playerByNames.size();
    }
  }

  @Override
  public Player createPlayer(String name) {
    var newPlayer = PlayerImpl.newInstance(name);
    newPlayer.setActivated(true);
    newPlayer.setLoggedIn(true);

    addPlayer(newPlayer);

    return newPlayer;
  }

  @Override
  public Player createPlayerWithSession(String name, Session session) {
    if (session == null) {
      throw new NullPointerException("Unable to assign a null session for the player");
    }

    var newPlayer = PlayerImpl.newInstance(name, session);
    newPlayer.setActivated(true);
    newPlayer.setLoggedIn(true);

    addPlayer(newPlayer);

    return newPlayer;
  }

  @Override
  public Player getPlayerByName(String playerName) {
    synchronized (playerByNames) {
      return playerByNames.get(playerName);
    }
  }

  @Override
  public Player getPlayerBySession(Session session) {
    synchronized (playerBySessions) {
      return playerBySessions.get(session);
    }
  }

  @Override
  public Collection<Player> getAllPlayers() {
    synchronized (playerByNames) {
      return playerByNames.values();
    }
  }

  @Override
  public Collection<Session> getAllSessions() {
    synchronized (playerBySessions) {
      return playerBySessions.keySet();
    }
  }

  @Override
  public void removePlayerByName(String playerName) {
    var player = getPlayerByName(playerName);
    if (player == null) {
      throw new RemovedNonExistentPlayerFromRoomException(playerName, ownerRoom);
    }

    removePlayer(player);
  }

  @Override
  public void removePlayerBySession(Session session) {
    var player = getPlayerBySession(session);
    if (player == null) {
      throw new RemovedNonExistentPlayerFromRoomException(session.toString(), ownerRoom);
    }

    removePlayer(player);
  }

  private void removePlayer(Player player) {
    synchronized (this) {
      playerByNames.remove(player.getName());
      if (player.containsSession()) {
        playerBySessions.remove(player.getSession());
      }
      playerCount = playerByNames.size();
    }
  }

  @Override
  public boolean containsPlayerName(String playerName) {
    synchronized (playerByNames) {
      return playerByNames.containsKey(playerName);
    }
  }

  @Override
  public boolean containsPlayerSession(Session session) {
    synchronized (playerBySessions) {
      return playerBySessions.containsKey(session);
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
  public void clear() {
    synchronized (this) {
      var iterator = new ArrayList<Player>(playerByNames.values()).iterator();
      while (iterator.hasNext()) {
        var player = iterator.next();
        removePlayer(player);
      }
    }
  }
}
