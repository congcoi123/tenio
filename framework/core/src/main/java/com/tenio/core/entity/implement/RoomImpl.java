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

package com.tenio.core.entity.implement;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.RoomState;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.define.result.PlayerJoinedRoomResult;
import com.tenio.core.entity.define.result.SwitchedPlayerSpectatorResult;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.setting.strategy.RoomCredentialValidatedStrategy;
import com.tenio.core.entity.setting.strategy.RoomPlayerSlotGeneratedStrategy;
import com.tenio.core.exception.PlayerJoinedRoomException;
import com.tenio.core.exception.SwitchedPlayerSpectatorException;
import com.tenio.core.network.entity.session.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * An implemented room class is for using in the server.
 */
public final class RoomImpl implements Room {

  public static final int NIL_SLOT = -1;
  public static final int DEFAULT_SLOT = 0;

  private static final AtomicLong ID_COUNTER = new AtomicLong();

  private final long id;
  private final Lock switchPlayerLock;
  private final Map<String, Object> properties;
  private String name;
  private String password;
  private int maxPlayers;
  private int maxSpectators;
  private volatile int playerCount;
  private volatile int spectatorCount;
  private Player owner;
  private PlayerManager playerManager;
  private RoomRemoveMode roomRemoveMode;
  private RoomCredentialValidatedStrategy roomCredentialValidatedStrategy;
  private RoomPlayerSlotGeneratedStrategy roomPlayerSlotGeneratedStrategy;
  private RoomState state;

  private volatile boolean activated;

  private RoomImpl() {
    id = ID_COUNTER.getAndIncrement();

    maxPlayers = 0;
    maxSpectators = 0;
    spectatorCount = 0;
    playerCount = 0;

    owner = null;
    playerManager = null;

    switchPlayerLock = new ReentrantLock();

    properties = new ConcurrentHashMap<String, Object>();
    activated = false;

    setRoomRemoveMode(RoomRemoveMode.DEFAULT);
  }

  public static Room newInstance() {
    return new RoomImpl();
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    roomCredentialValidatedStrategy.validateName(name);
    this.name = name;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public void setPassword(String password) {
    roomCredentialValidatedStrategy.validatePassword(password);
    this.password = password;
  }

  @Override
  public RoomState getState() {
    return state;
  }

  @Override
  public void setState(RoomState state) {
    this.state = state;
  }

  @Override
  public boolean isPublic() {
    return password == null;
  }

  @Override
  public int getMaxPlayers() {
    return maxPlayers;
  }

  @Override
  public void setMaxPlayers(int maxPlayers) {
    this.maxPlayers = maxPlayers;
  }

  @Override
  public int getMaxSpectators() {
    return maxSpectators;
  }

  @Override
  public void setMaxSpectators(int maxSpectators) {
    this.maxSpectators = maxSpectators;
  }

  @Override
  public Player getOwner() {
    return owner;
  }

  @Override
  public void setOwner(Player owner) {
    this.owner = owner;
  }

  @Override
  public PlayerManager getPlayerManager() {
    return playerManager;
  }

  @Override
  public void setPlayerManager(PlayerManager playerManager) {
    this.playerManager = playerManager;
  }

  @Override
  public boolean isActivated() {
    return activated;
  }

  @Override
  public void setActivated(boolean activated) {
    this.activated = activated;
  }

  @Override
  public RoomRemoveMode getRoomRemoveMode() {
    return roomRemoveMode;
  }

  @Override
  public void setRoomRemoveMode(RoomRemoveMode roomRemoveMode) {
    this.roomRemoveMode = roomRemoveMode;
  }

  @Override
  public Object getProperty(String key) {
    return properties.get(key);
  }

  @Override
  public boolean containsProperty(String key) {
    return properties.containsKey(key);
  }

  @Override
  public void addProperty(String key, Object value) {
    properties.put(key, value);
  }

  @Override
  public void removeProperty(String key) {
    properties.remove(key);
  }

  @Override
  public int getCapacity() {
    return maxPlayers + maxSpectators;
  }

  @Override
  public void setCapacity(int maxPlayers, int maxSpectators) {
    this.maxPlayers = maxPlayers;
    this.maxSpectators = maxSpectators;
  }

  @Override
  public List<Player> getPlayersList() {
    var players = playerManager.getAllPlayers().stream().filter(player -> !player.isSpectator())
        .collect(Collectors.toList());
    return new ArrayList<Player>(players);
  }

  @Override
  public List<Player> getSpectatorsList() {
    var spectators = playerManager.getAllPlayers().stream().filter(player -> player.isSpectator())
        .collect(Collectors.toList());
    return new ArrayList<Player>(spectators);
  }

  @Override
  public int getPlayerCount() {
    return playerCount;
  }

  @Override
  public int getSpectatorCount() {
    return spectatorCount;
  }

  @Override
  public boolean containsPlayerName(String playerName) {
    return playerManager.containsPlayerName(playerName);
  }

  @Override
  public Player getPlayerByName(String playerName) {
    return playerManager.getPlayerByName(playerName);
  }

  @Override
  public Player getPlayerBySession(Session session) {
    return playerManager.getPlayerBySession(session);
  }

  @Override
  public Collection<Player> getAllPlayersList() {
    return playerManager.getAllPlayers();
  }

  @Override
  public Collection<Session> getAllSessionList() {
    return playerManager.getAllSessions();
  }

  @Override
  public void addPlayer(Player player, boolean asSpectator, int targetSlot) {
    boolean validated = false;

    if (asSpectator) {
      validated = getSpectatorCount() < getMaxSpectators();
    } else {
      validated = getPlayerCount() < getMaxPlayers();
    }

    if (!validated) {
      throw new PlayerJoinedRoomException(
          String.format(
              "Unable to add player: %s to room, room is full with maximum player: %d, "
                  + "spectator: %d",
              player.getName(), getMaxPlayers(), getMaxSpectators()),
          PlayerJoinedRoomResult.ROOM_IS_FULL);
    }

    playerManager.addPlayer(player);

    if (asSpectator) {
      player.setSpectator(true);
    }

    updateElementsCounter();

    if (asSpectator) {
      player.setPlayerSlotInCurrentRoom(DEFAULT_SLOT);
    } else {
      if (targetSlot == DEFAULT_SLOT) {
        player.setPlayerSlotInCurrentRoom(
            roomPlayerSlotGeneratedStrategy.getFreePlayerSlotInRoom());
      } else {
        try {
          roomPlayerSlotGeneratedStrategy.tryTakeSlot(targetSlot);
          player.setPlayerSlotInCurrentRoom(targetSlot);
        } catch (IllegalArgumentException e) {
          player.setPlayerSlotInCurrentRoom(DEFAULT_SLOT);
          throw new PlayerJoinedRoomException(String
              .format("Unable to set the target slot: %d for player: %s", targetSlot,
                  player.getName()),
              PlayerJoinedRoomResult.SLOT_UNAVAILABLE_IN_ROOM);
        }
      }
    }
  }

  @Override
  public void removePlayer(Player player) {
    roomPlayerSlotGeneratedStrategy.freeSlotWhenPlayerLeft(player.getPlayerSlotInCurrentRoom());
    playerManager.removePlayerByName(player.getName());
    player.setCurrentRoom(null);
    updateElementsCounter();
  }

  @Override
  public void switchPlayerToSpectator(Player player) {
    if (!containsPlayerName(player.getName())) {
      throw new SwitchedPlayerSpectatorException(
          String.format("Player %s was not in room", player.getName()),
          SwitchedPlayerSpectatorResult.PLAYER_WAS_NOT_IN_ROOM);
    }

    switchPlayerLock.lock();
    try {
      if (getSpectatorCount() >= getMaxSpectators()) {
        throw new SwitchedPlayerSpectatorException("All spectator slots were already taken",
            SwitchedPlayerSpectatorResult.SWITCH_NO_SPECTATOR_SLOTS_AVAILABLE);
      }

      roomPlayerSlotGeneratedStrategy.freeSlotWhenPlayerLeft(player.getPlayerSlotInCurrentRoom());
      player.setPlayerSlotInCurrentRoom(DEFAULT_SLOT);
      player.setSpectator(true);

      updateElementsCounter();
    } finally {
      switchPlayerLock.unlock();
    }
  }

  @Override
  public void switchSpectatorToPlayer(Player player, int targetSlot) {
    if (!containsPlayerName(player.getName())) {
      throw new SwitchedPlayerSpectatorException(
          String.format("Player %s was not in room", player.getName()),
          SwitchedPlayerSpectatorResult.PLAYER_WAS_NOT_IN_ROOM);
    }

    switchPlayerLock.lock();
    try {
      if (getPlayerCount() >= getMaxPlayers()) {
        throw new SwitchedPlayerSpectatorException("All player slots were already taken",
            SwitchedPlayerSpectatorResult.SWITCH_NO_PLAYER_SLOTS_AVAILABLE);
      }

      if (targetSlot == DEFAULT_SLOT) {
        player.setPlayerSlotInCurrentRoom(
            roomPlayerSlotGeneratedStrategy.getFreePlayerSlotInRoom());
        player.setSpectator(false);
      } else {
        try {
          roomPlayerSlotGeneratedStrategy.tryTakeSlot(targetSlot);
          player.setPlayerSlotInCurrentRoom(targetSlot);
          player.setSpectator(false);
        } catch (IllegalArgumentException e) {
          throw new SwitchedPlayerSpectatorException(String
              .format("Unable to set the target slot: %d for player: %s", targetSlot,
                  player.getName()),
              SwitchedPlayerSpectatorResult.SLOT_UNAVAILABLE_IN_ROOM);
        }
      }
    } finally {
      switchPlayerLock.unlock();
    }
  }

  private void updateElementsCounter() {
    playerCount = getPlayersList().size();
    spectatorCount = getSpectatorsList().size();
  }

  @Override
  public boolean isEmpty() {
    return playerManager.getPlayerCount() == 0;
  }

  @Override
  public boolean isFull() {
    return playerManager.getPlayerCount() == getCapacity();
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Room)) {
      return false;
    } else {
      var room = (Room) object;
      return getId() == room.getId();
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return String.format("{ id: %d, name: %s, password: %s, max player: %d, max spectator: %d }",
        id, name != null ? name : "null", password != null ? password : "null", maxPlayers, maxSpectators);
  }

  @Override
  public RoomPlayerSlotGeneratedStrategy getPlayerSlotGeneratedStrategy() {
    return roomPlayerSlotGeneratedStrategy;
  }

  @Override
  public void setPlayerSlotGeneratedStrategy(
      RoomPlayerSlotGeneratedStrategy roomPlayerSlotGeneratedStrategy) {
    this.roomPlayerSlotGeneratedStrategy = roomPlayerSlotGeneratedStrategy;
  }

  @Override
  public RoomCredentialValidatedStrategy getRoomCredentialValidatedStrategy() {
    return roomCredentialValidatedStrategy;
  }

  @Override
  public void setRoomCredentialValidatedStrategy(
      RoomCredentialValidatedStrategy roomCredentialValidatedStrategy) {
    this.roomCredentialValidatedStrategy = roomCredentialValidatedStrategy;
  }
}
