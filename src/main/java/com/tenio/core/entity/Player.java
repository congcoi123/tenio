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

package com.tenio.core.entity;

import com.tenio.core.entity.define.room.PlayerRoleInRoom;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.schedule.task.internal.AutoDisconnectPlayerTask;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An abstract player entity used on the server.
 */
public interface Player {

  /**
   * Retrieves the player's name which should be unique in the management list and on the server.
   *
   * @return the {@link String} player's name
   */
  String getName();

  /**
   * Determines whether the player contains a session.
   *
   * @return {@code true} if the player contains a session, otherwise returns {@code false}
   */
  boolean containsSession();

  /**
   * Determines whether the player is in a particular state.
   *
   * @param state a {@link PlayerState} is in judgment
   * @return {@code true} if the player in that state, otherwise returns {@code false}
   */
  boolean isState(PlayerState state);

  /**
   * Retrieves the player's current state.
   *
   * @return the current {@link PlayerState}
   */
  PlayerState getState();

  /**
   * Sets current state value for the player.
   *
   * @param state a new value of {@link PlayerState} set to the player
   */
  void setState(PlayerState state);

  /**
   * Determines whether the player is activated.
   *
   * @return {@code true} if the player is activated, otherwise returns {@code false}
   */
  boolean isActivated();

  /**
   * Sets the current active state for the player.
   *
   * @param activated the new <code>boolean</code> state of player
   */
  void setActivated(boolean activated);

  /**
   * Determines whether the player is logged in the server.
   *
   * @return {@code true} if the player is logged, otherwise returns {@code false}
   */
  boolean isLoggedIn();

  /**
   * Sets the current logged in state for the player.
   *
   * @param loggedIn the new <code>boolean</code> state of player
   */
  void setLoggedIn(boolean loggedIn);

  /**
   * Retrieves the last logged in time in milliseconds of the player on the server.
   *
   * @return a {@code long} milliseconds value
   */
  long getLastLoggedInTime();

  /**
   * Retrieves the last activity time of player.
   *
   * @return the last activity time in milliseconds ({@code long} value)
   */
  long getLastActivityTime();

  /**
   * Sets the last activity time for the player.
   *
   * @param timestamp the last activity time in milliseconds ({@code long} value)
   */
  void setLastActivityTime(long timestamp);

  /**
   * Retrieves the last time when the player receives the last byte of data.
   *
   * @return the last reading new data time in milliseconds ({@code long} value)
   */
  long getLastReadTime();

  /**
   * Sets the last time when the player receives the last byte of data.
   *
   * @param timestamp the last reading new data time in milliseconds ({@code long} value)
   */
  void setLastReadTime(long timestamp);

  /**
   * Retrieves the last time when player sends the last byte of data.
   *
   * @return the last writing data time in milliseconds ({@code long} value)
   */
  long getLastWriteTime();

  /**
   * Sets the last time when the player sends the last byte of data.
   *
   * @param timestamp the last writing data time in milliseconds ({@code long} value)
   */
  void setLastWriteTime(long timestamp);

  /**
   * Retrieves the maximum time in seconds which allows the player to get in IDLE state (Do not
   * perform any action, such as reading or writing data).
   *
   * @return the maximum time in seconds ({@code integer} value) which allows the player to
   * get in IDLE state
   */
  int getMaxIdleTimeInSeconds();

  /**
   * Sets the maximum time in seconds which allows the player to get in IDLE state (Do not
   * perform any action, such as reading or writing data).
   *
   * @param seconds the maximum time in seconds ({@code integer} value) which allows the
   *                player to get in IDLE state
   */
  void setMaxIdleTimeInSeconds(int seconds);

  /**
   * Determines whether the player got in IDLE state (Do not perform any action, such as reading
   * or writing data).
   *
   * @return {@code true} if the player got in IDLE state, otherwise returns {@code false}
   */
  boolean isIdle();

  /**
   * Ensures that the {@link Player} is never deported from the server even it gets timeout.
   *
   * @return {@code true} if the player is never considered to be deported, otherwise returns
   * {@code false}
   * @see AutoDisconnectPlayerTask
   * @since 0.5.0
   */
  boolean isNeverDeported();

  /**
   * Allows making a {@link Player} not to be deported from the server.
   *
   * @param flag sets it {@code true} to make the player not to be deported
   * @see AutoDisconnectPlayerTask
   * @since 0.5.0
   */
  void setNeverDeported(boolean flag);

  /**
   * Retrieves the maximum time in seconds which allows the player to get in IDLE state (Do not
   * perform any action, such as reading or writing data) in case of never deported selection.
   *
   * @return the maximum time in seconds ({@code integer} value) which allows the player to
   * get in IDLE state
   * @since 0.5.0
   */
  int getMaxIdleTimeNeverDeportedInSeconds();

  /**
   * Sets the maximum time in seconds which allows the player to get in IDLE state (Do not
   * perform any action, such as reading or writing data) in case of never deported selection.
   *
   * @param seconds the maximum time in seconds ({@code integer} value) which allows the
   *                player to get in IDLE state
   * @since 0.5.0
   */
  void setMaxIdleTimeNeverDeportedInSeconds(int seconds);

  /**
   * Determines whether the player got in IDLE state (Do not perform any action, such as reading
   * or writing data) in case of never deported selection.
   *
   * @return {@code true} if the player got in IDLE state, otherwise returns {@code false}
   * @see AutoDisconnectPlayerTask
   * @since 0.5.0
   */
  boolean isIdleNeverDeported();

  /**
   * Retrieves the player's session.
   *
   * @return an instance of optional {@link Session}
   * @see Optional
   */
  Optional<Session> getSession();

  /**
   * Associates a session with the player.
   *
   * @param session a associating {@link Session}
   */
  void setSession(Session session);

  /**
   * Determines whether the player is in a room.
   *
   * @return {@code true} if the player is in a room, otherwise returns {@code false}
   */
  boolean isInRoom();

  /**
   * Retrieves a player role when the player joins room.
   *
   * @return the {@link PlayerRoleInRoom}
   */
  PlayerRoleInRoom getRoleInRoom();

  /**
   * Sets a player role when the player joins room.
   *
   * @param roleInRoom the {@link PlayerRoleInRoom}
   */
  void setRoleInRoom(PlayerRoleInRoom roleInRoom);

  /**
   * Retrieves the current room which the player is in.
   *
   * @return an optional {@link Room} instance
   * @see Optional
   */
  Optional<Room> getCurrentRoom();

  /**
   * Sets associated room to the player.
   *
   * @param room an instance of {@link Room}
   */
  void setCurrentRoom(Room room);

  /**
   * Retrieves the last time the player left its room.
   *
   * @return the milliseconds in {@code long} value
   */
  long getLastJoinedRoomTime();

  /**
   * Retrieves the player's slot in its room.
   *
   * @return the {@code integer} value of the player slot position
   */
  int getPlayerSlotInCurrentRoom();

  /**
   * Set a slot value for the player in its room.
   *
   * @param slot the {@code integer} value of the player slot position
   */
  void setPlayerSlotInCurrentRoom(int slot);

  /**
   * Retrieves a property belongs to the player.
   *
   * @param key the {@link String} key to fetch data
   * @return an {@link Object} value if present, otherwise {@code null}
   */
  Object getProperty(String key);

  /**
   * Sets a property belongs to the player.
   *
   * @param key   the {@link String} key
   * @param value an instance {@link Object} of property's value
   */
  void setProperty(String key, Object value);

  /**
   * Determines whether a property is available for the player.
   *
   * @param key a {@link String} key used for searching the corresponding property
   * @return {@code true} if the property is available, otherwise returns {@code false}
   */
  boolean containsProperty(String key);

  /**
   * Removes a property which belongs to the player.
   *
   * @param key the {@link String} key
   */
  void removeProperty(String key);

  /**
   * Removes all properties of the player.
   */
  void clearProperties();

  /**
   * Observes all changes on the player.
   *
   * @param updateConsumer action when there is any change on player in {@link Field}
   * @since 0.5.0
   */
  void onUpdateListener(Consumer<Field> updateConsumer);

  /**
   * Wipes out all the player's information.
   */
  void clean();

  /**
   * All the support fields that can be triggered as events.
   *
   * @see Player#onUpdateListener(Consumer)
   */
  enum Field {
    /**
     * The player state.
     */
    STATE,
    /**
     * The player activation status.
     */
    ACTIVATION,
    /**
     * The player deportation status.
     */
    DEPORTATION,
    /**
     * The player role in his room.
     *
     * @see PlayerRoleInRoom
     */
    ROLE_IN_ROOM,
    /**
     * The player slot position in his room.
     */
    SLOT_IN_ROOM,
    /**
     * The player map of properties.
     */
    PROPERTY
  }
}
