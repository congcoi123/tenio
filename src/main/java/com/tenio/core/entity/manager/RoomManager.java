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

package com.tenio.core.entity.manager;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.entity.setting.strategy.RoomCredentialValidatedStrategy;
import com.tenio.core.exception.AddedDuplicatedRoomException;
import com.tenio.core.exception.CreatedRoomException;
import com.tenio.core.manager.Manager;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * All supported APIs for the room management.
 */
public interface RoomManager extends Manager {

  /**
   * Retrieves the maximum number of rooms on the server.
   *
   * @return the maximum number of room on the server ({@code integer} value)
   */
  int getMaxRooms();

  /**
   * Sets the maximum number of room on the server.
   *
   * @param maxRooms the maximum number of rooms ({@code integer} value)
   */
  void setMaxRooms(int maxRooms);

  /**
   * Adds a new room to the server.
   *
   * @param room an instance of {@link Room}
   * @throws AddedDuplicatedRoomException when a room is already available on the server, but it
   *                                      is mentioned again
   */
  void addRoom(Room room) throws AddedDuplicatedRoomException;

  /**
   * Creates a new room without an owner and adds it to the server.
   *
   * @param roomSetting all settings created by a {@link InitialRoomSetting} builder
   * @return an instance of {@link Room}
   * @throws IllegalArgumentException when an invalid setting builder is set
   */
  default Room createRoom(InitialRoomSetting roomSetting)
      throws IllegalArgumentException, CreatedRoomException {
    return createRoomWithOwner(roomSetting, null);
  }

  /**
   * Creates a new room with an owner and adds it to the server.
   *
   * @param roomSetting all settings created by a {@link InitialRoomSetting} builder
   * @param player      a {@link Player} as the room's owner
   * @return an instance of {@link Room}
   * @throws IllegalArgumentException when an invalid setting builder is set
   * @throws CreatedRoomException     when it fails to create a new room
   */
  Room createRoomWithOwner(InitialRoomSetting roomSetting, Player player)
      throws IllegalArgumentException, CreatedRoomException;

  /**
   * Determines whether a room is in the management list by looking for its unique ID.
   *
   * @param roomId the {@code long} value of room's ID
   * @return {@code true} if the searching room is available, otherwise returns {@code false}
   */
  boolean containsRoomId(long roomId);

  /**
   * Determines whether a room is in the management list by looking for its name.
   *
   * @param roomName the {@link String} value room's name
   * @return {@code true} if the searching room is available, otherwise returns {@code false}
   */
  boolean containsRoomName(String roomName);

  /**
   * Retrieves a room instance by looking for its unique ID.
   *
   * @param roomId the {@code long} value of room's ID
   * @return an instance of {@link Room} if present, otherwise {@code null}
   */
  Room getRoomById(long roomId);

  /**
   * Retrieves a read-only room management list by searching with the room's name. This method
   * should be used to prevent the "escape references" issue.
   *
   * @param roomName the room's name that is in inquiring
   * @return a list of all {@link Room}s sharing the same name in the management list
   * @see List
   */
  List<Room> getReadonlyRoomsListByName(String roomName);

  /**
   * Retrieves an iterator for a room management list. This method should be used to prevent the
   * "escape references" issue.
   *
   * @return a list of all {@link Room}s in the management list
   * @see Iterator
   */
  Iterator<Room> getRoomIterator();

  /**
   * Retrieves a read-only room management list. This method should be used to prevent the
   * "escape references" issue.
   *
   * @return a list of all {@link Room}s in the management list
   * @see List
   */
  List<Room> getReadonlyRoomsList();

  /**
   * Removes a room from the management list.
   *
   * @param roomId the unique {@code long} room's ID
   */
  void removeRoomById(long roomId);

  /**
   * Updates a room's name.
   *
   * @param room     the updating {@link Room}
   * @param roomName new {@link String} value of room's name
   * @throws IllegalArgumentException when invalid name is set
   * @see RoomCredentialValidatedStrategy
   */
  void changeRoomName(Room room, String roomName) throws IllegalArgumentException;

  /**
   * Updates a room's password.
   *
   * @param room         the updating {@link Room}
   * @param roomPassword new {@link String} value of room's password
   * @throws IllegalArgumentException when invalid password is set
   * @see RoomCredentialValidatedStrategy
   */
  void changeRoomPassword(Room room, String roomPassword) throws IllegalArgumentException;

  /**
   * Updates a room's capacity.
   *
   * @param room          the updating {@link Room}
   * @param maxParticipants    the maximum number of participants allows in the room
   *                           ({@code integer} value)
   * @param maxSpectators the maximum number of spectators allows in the room
   *                      ({@code integer} value)
   * @throws IllegalArgumentException when invalid value is set
   */
  void changeRoomCapacity(Room room, int maxParticipants, int maxSpectators)
      throws IllegalArgumentException;

  /**
   * Fetches the current number of rooms in the management list.
   *
   * @return the current number of rooms ({@code integer} value)
   */
  int getRoomCount();

  /**
   * Removes all rooms from the management list.
   *
   * @throws UnsupportedOperationException the operation is not supported at the moment
   */
  default void clear() {
    throw new UnsupportedOperationException();
  }
}
