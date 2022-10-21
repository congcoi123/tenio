/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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
import com.tenio.core.entity.PlayerRoleInRoom;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.RoomState;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.define.result.PlayerJoinedRoomResult;
import com.tenio.core.entity.define.result.SwitchedPlayerRoleInRoomResult;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.setting.strategy.RoomCredentialValidatedStrategy;
import com.tenio.core.entity.setting.strategy.RoomPlayerSlotGeneratedStrategy;
import com.tenio.core.exception.PlayerJoinedRoomException;
import com.tenio.core.exception.SwitchedPlayerRoleInRoomException;
import com.tenio.core.network.entity.session.Session;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * An implemented class is for a room using in the server.
 */
public final class RoomImpl implements Room {

  public static final int NIL_SLOT = -1;
  public static final int DEFAULT_SLOT = 0;

  private static final AtomicLong ID_COUNTER = new AtomicLong();

  private final long id;
  private final Lock switchRoleLock;
  private final Map<String, Object> properties;
  private String name;
  private String password;
  private int maxParticipants;
  private int maxSpectators;
  private volatile int participantCount;
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
    maxParticipants = 0;
    maxSpectators = 0;
    spectatorCount = 0;
    participantCount = 0;
    owner = null;
    playerManager = null;
    switchRoleLock = new ReentrantLock();
    properties = new ConcurrentHashMap<>();
    activated = false;
    setRoomRemoveMode(RoomRemoveMode.DEFAULT);
  }

  /**
   * Create a new instance.
   *
   * @return a new instance
   */
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
    return Objects.isNull(password);
  }

  @Override
  public int getMaxParticipants() {
    return maxParticipants;
  }

  @Override
  public void setMaxParticipants(int maxParticipants) {
    this.maxParticipants = maxParticipants;
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
  public Optional<Player> getOwner() {
    return Optional.ofNullable(owner);
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
  public void setProperty(String key, Object value) {
    properties.put(key, value);
  }

  @Override
  public boolean containsProperty(String key) {
    return properties.containsKey(key);
  }

  @Override
  public void removeProperty(String key) {
    properties.remove(key);
  }

  @Override
  public void clearProperties() {
    properties.clear();
  }

  @Override
  public int getCapacity() {
    return maxParticipants + maxSpectators;
  }

  @Override
  public void setCapacity(int maxParticipants, int maxSpectators) {
    this.maxParticipants = maxParticipants;
    this.maxSpectators = maxSpectators;
  }

  @Override
  public int getParticipantCount() {
    return participantCount;
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
  public Optional<Player> getPlayerByName(String playerName) {
    return Optional.ofNullable(playerManager.getPlayerByName(playerName));
  }

  @Override
  public Optional<Player> getPlayerBySession(Session session) {
    return Optional.ofNullable(playerManager.getPlayerBySession(session));
  }

  @Override
  public Iterator<Player> getPlayerIterator() {
    return playerManager.getPlayerIterator();
  }

  @Override
  public List<Player> getReadonlyPlayersList() {
    return playerManager.getReadonlyPlayersList();
  }

  @Override
  public List<Player> getReadonlyParticipantsList() {
    return getReadonlyPlayersList().stream()
        .filter(player -> player.getRoleInRoom() == PlayerRoleInRoom.PARTICIPANT)
        .collect(Collectors.toList());
  }

  @Override
  public List<Player> getReadonlySpectatorsList() {
    return getReadonlyPlayersList().stream()
        .filter(player -> player.getRoleInRoom() == PlayerRoleInRoom.SPECTATOR)
        .collect(Collectors.toList());
  }

  @Override
  public void addPlayer(Player player, boolean asSpectator, int targetSlot) {
    boolean validated;

    if (asSpectator) {
      validated = getSpectatorCount() < getMaxSpectators();
    } else {
      validated = getParticipantCount() < getMaxParticipants();
    }

    if (!validated) {
      throw new PlayerJoinedRoomException(
          String.format(
              "Unable to add player: %s to room, room is full with maximum participants: %d, "
                  + "spectators: %d",
              player.getName(), getMaxParticipants(), getMaxSpectators()),
          PlayerJoinedRoomResult.ROOM_IS_FULL);
    }

    playerManager.addPlayer(player);

    if (asSpectator) {
      player.setRoleInRoom(PlayerRoleInRoom.SPECTATOR);
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
              .format("Unable to set the target slot: %d for the participant: %s", targetSlot,
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
  public void switchParticipantToSpectator(Player player) {
    if (!containsPlayerName(player.getName())) {
      throw new SwitchedPlayerRoleInRoomException(
          String.format("Player %s was not in room", player.getName()),
          SwitchedPlayerRoleInRoomResult.PLAYER_WAS_NOT_IN_ROOM);
    }

    switchRoleLock.lock();
    try {
      if (getSpectatorCount() >= getMaxSpectators()) {
        throw new SwitchedPlayerRoleInRoomException("All spectator slots were already taken",
            SwitchedPlayerRoleInRoomResult.SWITCH_NO_SPECTATOR_SLOTS_AVAILABLE);
      }

      roomPlayerSlotGeneratedStrategy.freeSlotWhenPlayerLeft(player.getPlayerSlotInCurrentRoom());
      player.setPlayerSlotInCurrentRoom(DEFAULT_SLOT);
      player.setRoleInRoom(PlayerRoleInRoom.SPECTATOR);

      updateElementsCounter();
    } finally {
      switchRoleLock.unlock();
    }
  }

  @Override
  public void switchSpectatorToParticipant(Player player, int targetSlot) {
    if (!containsPlayerName(player.getName())) {
      throw new SwitchedPlayerRoleInRoomException(
          String.format("Player %s was not in room", player.getName()),
          SwitchedPlayerRoleInRoomResult.PLAYER_WAS_NOT_IN_ROOM);
    }

    switchRoleLock.lock();
    try {
      if (getParticipantCount() >= getMaxParticipants()) {
        throw new SwitchedPlayerRoleInRoomException("All participant slots were already taken",
            SwitchedPlayerRoleInRoomResult.SWITCH_NO_PARTICIPANT_SLOTS_AVAILABLE);
      }

      if (targetSlot == DEFAULT_SLOT) {
        player.setPlayerSlotInCurrentRoom(
            roomPlayerSlotGeneratedStrategy.getFreePlayerSlotInRoom());
        player.setRoleInRoom(PlayerRoleInRoom.PARTICIPANT);
      } else {
        try {
          roomPlayerSlotGeneratedStrategy.tryTakeSlot(targetSlot);
          player.setPlayerSlotInCurrentRoom(targetSlot);
          player.setRoleInRoom(PlayerRoleInRoom.PARTICIPANT);
        } catch (IllegalArgumentException e) {
          throw new SwitchedPlayerRoleInRoomException(String
              .format("Unable to set the target slot: %d for the participant: %s", targetSlot,
                  player.getName()),
              SwitchedPlayerRoleInRoomResult.SLOT_UNAVAILABLE_IN_ROOM);
        }
      }
    } finally {
      switchRoleLock.unlock();
    }
  }

  private void updateElementsCounter() {
    participantCount = getReadonlyParticipantsList().size();
    spectatorCount = getReadonlySpectatorsList().size();
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
    return String.format("{ id: %d, name: %s, password: %s, max participants: %d, max spectator: "
            + "%d }", id, Objects.nonNull(name) ? name : "null", Objects.nonNull(password) ? password : "null",
        maxParticipants, maxSpectators);
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
