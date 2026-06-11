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
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.result.RoomCreatedResult;
import com.tenio.core.entity.implement.DefaultRoom;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.AddedDuplicatedRoomException;
import com.tenio.core.exception.CreatedRoomException;
import com.tenio.core.manager.AbstractManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * An implemented class is for room management.
 */
public final class RoomManagerImpl extends AbstractManager implements RoomManager {

  private final Map<Long, Room> rooms;
  private volatile List<Room> snapshotRoomsList;
  private volatile int snapshotRoomCount;
  private int maxRooms;

  private RoomManagerImpl(EventManager eventManager) {
    super(eventManager);
    rooms = new HashMap<>();
    snapshotRoomsList = new ArrayList<>();
    maxRooms = DEFAULT_MAX_ROOMS;
  }

  /**
   * Retrieves a new room manager instance.
   *
   * @param eventManager the instance of {@link EventManager}
   * @return a new instance of {@link RoomManager}
   */
  public static RoomManager newInstance(EventManager eventManager) {
    return new RoomManagerImpl(eventManager);
  }

  @Override
  public void addRoom(Room room) {
    if (containsRoomId(room.getId())) {
      throw new AddedDuplicatedRoomException(room);
    }

    synchronized (this) {
      rooms.put(room.getId(), room);
      snapshotRoomsList = rooms.values().stream().toList();
      snapshotRoomCount = rooms.size();
    }
  }

  @Override
  public void addRoomWithOwner(Room room, InitialRoomSetting roomSetting, Player player)
      throws AddedDuplicatedRoomException {
    int roomCount = getRoomCount();
    if (roomCount >= maxRooms) {
      throw new CreatedRoomException(
          String.format("Unable to create new room, reached limited the maximum room number: %d",
              roomCount), RoomCreatedResult.REACHED_MAX_ROOMS);
    }

    room.configurePlayerManager(PlayerManagerImpl.newInstance(eventManager));
    room.configurePlayerSlotGeneratedStrategy(roomSetting.getRoomPlayerSlotGeneratedStrategy());
    room.configureRoomCredentialValidatedStrategy(roomSetting.getRoomCredentialValidatedStrategy());
    room.setRoomRemoveMode(roomSetting.getRoomRemoveMode());
    room.setName(roomSetting.getName());
    room.setPassword(roomSetting.getPassword());
    room.setActivated(roomSetting.isActivated());
    room.setCapacity(roomSetting.getMaxParticipants(), roomSetting.getMaxSpectators());
    room.setOwner(player);
    if (roomSetting.getProperties() != null) {
      roomSetting.getProperties().forEach(room::setProperty);
    }

    addRoom(room);
  }

  @Override
  public Room createRoomWithOwner(InitialRoomSetting roomSetting, Player player) {
    int roomCount = getRoomCount();
    if (roomCount >= maxRooms) {
      throw new CreatedRoomException(
          String.format("Unable to create new room, reached limited the maximum room number: %d",
              roomCount), RoomCreatedResult.REACHED_MAX_ROOMS);
    }

    Room room = DefaultRoom.newInstance();
    room.configurePlayerManager(PlayerManagerImpl.newInstance(eventManager));
    room.configurePlayerSlotGeneratedStrategy(roomSetting.getRoomPlayerSlotGeneratedStrategy());
    room.configureRoomCredentialValidatedStrategy(roomSetting.getRoomCredentialValidatedStrategy());
    room.setRoomRemoveMode(roomSetting.getRoomRemoveMode());
    room.setName(roomSetting.getName());
    room.setPassword(roomSetting.getPassword());
    room.setActivated(roomSetting.isActivated());
    room.setCapacity(roomSetting.getMaxParticipants(), roomSetting.getMaxSpectators());
    room.setOwner(player);
    if (roomSetting.getProperties() != null) {
      roomSetting.getProperties().forEach(room::setProperty);
    }

    addRoom(room);

    return room;
  }

  @Override
  public synchronized boolean containsRoomId(long roomId) {
    return rooms.containsKey(roomId);
  }

  @Override
  public boolean containsSnapshotRoomName(String roomName) {
    return snapshotRoomsList.stream().anyMatch(room -> room.getName().equals(roomName));
  }

  @Override
  public synchronized Room getRoomById(long roomId) {
    return rooms.get(roomId);
  }

  @Override
  public List<Room> getSnapshotRoomsListByName(String roomName) {
    return snapshotRoomsList.stream().filter(room -> room.getName().equals(roomName))
            .collect(Collectors.toList());
  }

  @Override
  public void computeRooms(Consumer<Iterator<Room>> onComputed) {
    synchronized (this) {
      onComputed.accept(rooms.values().iterator());
    }
  }

  @Override
  public List<Room> getSnapshotRoomsList() {
    return snapshotRoomsList;
  }

  @Override
  public List<Room> getRoomsList() {
    synchronized (this) {
      snapshotRoomsList = rooms.values().stream().toList();
      return getSnapshotRoomsList();
    }
  }

  @Override
  public void removeRoomById(long roomId) {
    synchronized (this) {
      rooms.remove(roomId);
      snapshotRoomsList = rooms.values().stream().toList();
      snapshotRoomCount = rooms.size();
    }
  }

  @Override
  public void changeRoomName(Room room, String roomName) {
    room.setName(roomName);
  }

  @Override
  public void changeRoomPassword(Room room, String roomPassword) {
    room.setPassword(roomPassword);
  }

  @Override
  public void changeRoomCapacity(Room room, int maxParticipants, int maxSpectators) {
    if (maxParticipants <= room.getSnapshotParticipantCount()) {
      throw new IllegalArgumentException(String.format(
          "Unable to assign the new max participants number: %d, "
              + "because it's less than the current number of participants: %d",
          maxParticipants, room.getSnapshotParticipantCount()));
    }
    if (maxSpectators <= room.getSnapshotSpectatorCount()) {
      throw new IllegalArgumentException(String.format(
          "Unable to assign the new max spectator number: %d, "
              + "because it's less than the current number of spectator: %d",
          maxSpectators, room.getSnapshotSpectatorCount()));
    }

    room.setCapacity(maxParticipants, maxSpectators);
  }

  @Override
  public int getSnapshotRoomCount() {
    return snapshotRoomCount;
  }

  @Override
  public int getRoomCount() {
    synchronized (this) {
      snapshotRoomCount = rooms.size();
      return getSnapshotRoomCount();
    }
  }

  @Override
  public void configureMaxRooms(int maxRooms) {
    this.maxRooms = maxRooms;
  }
}
