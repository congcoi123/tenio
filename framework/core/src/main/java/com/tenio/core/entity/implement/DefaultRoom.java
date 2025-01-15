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

package com.tenio.core.entity.implement;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.RoomState;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.define.result.PlayerJoinedRoomResult;
import com.tenio.core.entity.define.result.SwitchedPlayerRoleInRoomResult;
import com.tenio.core.entity.define.room.PlayerRoleInRoom;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.setting.strategy.RoomCredentialValidatedStrategy;
import com.tenio.core.entity.setting.strategy.RoomPlayerSlotGeneratedStrategy;
import com.tenio.core.exception.PlayerJoinedRoomException;
import com.tenio.core.exception.SwitchedPlayerRoleInRoomException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An implemented class is for a room using in the server.
 */
public class DefaultRoom implements Room {

  private final long id;
  private final Map<String, Object> properties;
  private final AtomicReference<RoomState> state;

  private volatile String name;
  private volatile String password;
  private volatile Player owner;
  private volatile List<Player> participants;
  private volatile int maxParticipants;
  private volatile List<Player> spectators;
  private volatile int maxSpectators;
  private volatile RoomRemoveMode roomRemoveMode;
  private volatile boolean activated;

  private PlayerManager playerManager;
  private RoomCredentialValidatedStrategy roomCredentialValidatedStrategy;
  private RoomPlayerSlotGeneratedStrategy roomPlayerSlotGeneratedStrategy;

  /**
   * Constructor.
   */
  public DefaultRoom() {
    id = ID_COUNTER.getAndIncrement();
    properties = new ConcurrentHashMap<>();
    state = new AtomicReference<>();
    spectators = new ArrayList<>();
    participants = new ArrayList<>();
    setState(null);
    setRoomRemoveMode(RoomRemoveMode.WHEN_EMPTY);
  }

  /**
   * Create a new instance.
   *
   * @return a new instance
   */
  public static Room newInstance() {
    return new DefaultRoom();
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
  public boolean isState(RoomState state) {
    return getState() == state;
  }

  @Override
  public RoomState getState() {
    return state.get();
  }

  @Override
  public void setState(RoomState state) {
    this.state.set(state);
  }

  @Override
  public boolean transitionState(RoomState expectedState, RoomState newState) {
    return state.compareAndSet(expectedState, newState);
  }

  @Override
  public boolean isPublic() {
    return Objects.isNull(password);
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
  public int getPlayerCount() {
    return playerManager.getPlayerCount();
  }

  @Override
  public int getParticipantCount() {
    return getReadonlyParticipantsList().size();
  }

  @Override
  public int getSpectatorCount() {
    return getReadonlySpectatorsList().size();
  }

  @Override
  public boolean containsPlayerIdentity(String playerIdentity) {
    return playerManager.containsPlayerIdentity(playerIdentity);
  }

  @Override
  public Optional<Player> getPlayerByIdentity(String playerIdentity) {
    return Optional.ofNullable(playerManager.getPlayerByIdentity(playerIdentity));
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
    return participants;
  }

  @Override
  public List<Player> getReadonlySpectatorsList() {
    return spectators;
  }

  @Override
  public void addPlayer(Player player, String password, boolean asSpectator, int targetSlot) {
    if (Objects.nonNull(this.password) && !this.password.equals(password)) {
      throw new PlayerJoinedRoomException(
          String.format("Unable to add player: %s to room due to invalid password provided", player.getIdentity()),
          PlayerJoinedRoomResult.INVALID_CREDENTIALS);
    }

    boolean validated;
    if (asSpectator) {
      validated = getSpectatorCount() < getMaxSpectators();
    } else {
      validated = getParticipantCount() < getMaxParticipants();
    }

    if (!validated) {
      throw new PlayerJoinedRoomException(
          String.format("Unable to add player: %s to room, room is full with maximum participants: %d, spectators: %d",
              player.getIdentity(), getMaxParticipants(), getMaxSpectators()),
          PlayerJoinedRoomResult.ROOM_IS_FULL);
    }

    playerManager.addPlayer(player);

    if (asSpectator) {
      player.setRoleInRoom(PlayerRoleInRoom.SPECTATOR);
    } else {
      player.setRoleInRoom(PlayerRoleInRoom.PARTICIPANT);
    }

    classifyPlayersByRoles();

    if (asSpectator) {
      player.setPlayerSlotInCurrentRoom(NIL_SLOT);
    } else {
      allocateSlotToPlayer(player, targetSlot);
    }
  }

  @Override
  public void removePlayer(Player player) {
    roomPlayerSlotGeneratedStrategy.freeSlotWhenPlayerLeft(player.getPlayerSlotInCurrentRoom());
    playerManager.removePlayerByIdentity(player.getIdentity());
    player.setCurrentRoom(null);
    getOwner().ifPresent(owner -> {
      if (owner.getIdentity().equals(player.getIdentity())) {
        setOwner(null);
      }
    });

    classifyPlayersByRoles();
  }

  @Override
  public void switchParticipantToSpectator(Player player) {
    if (!containsPlayerIdentity(player.getIdentity())) {
      throw new SwitchedPlayerRoleInRoomException(
          String.format("Player %s was not in room", player.getIdentity()),
          SwitchedPlayerRoleInRoomResult.PLAYER_WAS_NOT_IN_ROOM);
    }

    if (getSpectatorCount() >= getMaxSpectators()) {
      throw new SwitchedPlayerRoleInRoomException("All spectator slots were already taken",
          SwitchedPlayerRoleInRoomResult.SWITCH_NO_SPECTATOR_SLOTS_AVAILABLE);
    }

    roomPlayerSlotGeneratedStrategy.freeSlotWhenPlayerLeft(player.getPlayerSlotInCurrentRoom());
    player.setPlayerSlotInCurrentRoom(DEFAULT_SLOT);
    player.setRoleInRoom(PlayerRoleInRoom.SPECTATOR);

    classifyPlayersByRoles();
  }

  @Override
  public void switchSpectatorToParticipant(Player player, int targetSlot) {
    if (!containsPlayerIdentity(player.getIdentity())) {
      throw new SwitchedPlayerRoleInRoomException(
          String.format("Player %s was not in room", player.getIdentity()),
          SwitchedPlayerRoleInRoomResult.PLAYER_WAS_NOT_IN_ROOM);
    }

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
                player.getIdentity()),
            SwitchedPlayerRoleInRoomResult.SLOT_UNAVAILABLE_IN_ROOM);
      }
    }

    classifyPlayersByRoles();
  }

  private synchronized void classifyPlayersByRoles() {
    participants = getReadonlyPlayersList().stream()
        .filter(player -> player.getRoleInRoom() == PlayerRoleInRoom.PARTICIPANT)
        .toList();
    spectators = getReadonlyPlayersList().stream()
        .filter(player -> player.getRoleInRoom() == PlayerRoleInRoom.SPECTATOR)
        .toList();
  }

  private void allocateSlotToPlayer(Player player, int targetSlot) {
    if (targetSlot == DEFAULT_SLOT) {
      player.setPlayerSlotInCurrentRoom(
          roomPlayerSlotGeneratedStrategy.getFreePlayerSlotInRoom());
    } else {
      try {
        roomPlayerSlotGeneratedStrategy.tryTakeSlot(targetSlot);
        player.setPlayerSlotInCurrentRoom(targetSlot);
      } catch (IllegalArgumentException exception) {
        player.setPlayerSlotInCurrentRoom(DEFAULT_SLOT);
        throw new PlayerJoinedRoomException(String
            .format("Unable to set the target slot: %d for the participant: %s", targetSlot,
                player.getIdentity()), PlayerJoinedRoomResult.SLOT_UNAVAILABLE_IN_ROOM);
      }
    }
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
  public int getCapacity() {
    return maxParticipants + maxSpectators;
  }

  @Override
  public void setCapacity(int maxParticipants, int maxSpectators) {
    this.maxParticipants = maxParticipants;
    this.maxSpectators = maxSpectators;
  }

  @Override
  public void configurePlayerManager(PlayerManager playerManager) {
    this.playerManager = playerManager;
  }

  @Override
  public void configurePlayerSlotGeneratedStrategy(
      RoomPlayerSlotGeneratedStrategy roomPlayerSlotGeneratedStrategy) {
    this.roomPlayerSlotGeneratedStrategy = roomPlayerSlotGeneratedStrategy;
  }

  @Override
  public void configureRoomCredentialValidatedStrategy(
      RoomCredentialValidatedStrategy roomCredentialValidatedStrategy) {
    this.roomCredentialValidatedStrategy = roomCredentialValidatedStrategy;
  }
  
  @Override
  public boolean equals(Object object) {
    return (object instanceof Room room) && (room.getId() == id);
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public String toString() {
    return "DefaultRoom{" +
        "id=" + id +
        ", properties=" + properties +
        ", owner=" + owner +
        ", state=" + state.get() +
        ", name='" + name + '\'' +
        ", password='" + password + '\'' +
        ", participants=" + participants +
        ", maxParticipants=" + maxParticipants +
        ", spectators=" + spectators +
        ", maxSpectators=" + maxSpectators +
        ", roomRemoveMode=" + roomRemoveMode +
        ", activated=" + activated +
        '}';
  }
}
