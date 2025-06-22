/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.schedule.task.internal.AutoDisconnectPlayerTask;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents a player entity in the game server.
 * This interface defines the core functionality and properties of a player,
 * including session management, state tracking, and property storage.
 *
 * <p>Key features:
 * <ul>
 *   <li>Player session management and connection handling</li>
 *   <li>Activity tracking and idle state management</li>
 *   <li>Room and game state management</li>
 *   <li>Custom property storage and management</li>
 *   <li>Event listener support for player updates</li>
 *   <li>Player role and slot management</li>
 *   <li>Login state and authentication tracking</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * {@code
 * Player player = playerManager.getPlayerByIdentity("player1");
 * 
 * // Set player properties
 * player.setProperty("score", 100);
 * player.setProperty("level", 5);
 * 
 * // Track player activity
 * player.setLastActivityTime(System.currentTimeMillis());
 * if (player.isIdle()) {
 *     // Handle idle player
 * }
 * 
 * // Manage player state
 * player.setLoggedIn(true);
 * player.setCurrentRoom(gameRoom);
 * player.setPlayerSlotInCurrentRoom(1);
 * 
 * // Listen for player updates
 * player.onUpdateListener(field -> {
 *     // Handle player property changes
 * });
 * }
 * </pre>
 *
 * <p>Thread safety: Implementations of this interface should be thread-safe
 * as they may be accessed from multiple threads concurrently. The interface
 * provides atomic operations for state changes and property management.
 *
 * <p>Note: This interface is designed to work in conjunction with the
 * {@link PlayerManager} for player lifecycle management and the
 * {@link AutoDisconnectPlayerTask} for automatic player cleanup.
 *
 * @see PlayerManager
 * @see AutoDisconnectPlayerTask
 * @see Room
 * @since 0.5.0
 */
public interface Player {

  /**
   * Retrieves the player's identity which should be unique in the management list and on the server.
   *
   * @return the {@link String} player's identity
   */
  String getIdentity();

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
   * Updates state in thread-safe.
   *
   * @param expectedState the current expected state
   * @param newState      new state
   * @return {@code true} if the update is successful, otherwise returns {@code false}
   * @since 0.6.1
   */
  boolean transitionState(PlayerState expectedState, PlayerState newState);

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
   * This value is used for session management and activity tracking.
   *
   * @return the last login timestamp in milliseconds
   */
  long getLastLoggedInTime();

  /**
   * Retrieves the last activity time of the player.
   * This value is used to determine if the player is idle.
   *
   * @return the last activity timestamp in milliseconds
   */
  long getLastActivityTime();

  /**
   * Retrieves the duration from the last activity.
   * This value is used for idle state management.
   *
   * @return the duration from the last activity in seconds
   */
  long getInactiveTimeInSeconds();

  /**
   * Retrieves the last time when the player receives the last byte of data.
   * This value is used for connection monitoring.
   *
   * @return the last reading new data time in milliseconds
   */
  long getLastReadTime();

  /**
   * Sets the last time when the player receives the last byte of data.
   * This operation is atomic and thread-safe.
   *
   * @param timestamp the last reading new data time in milliseconds
   */
  void setLastReadTime(long timestamp);

  /**
   * Retrieves the last time when player sends the last byte of data.
   * This value is used for connection monitoring.
   *
   * @return the last writing data time in milliseconds
   */
  long getLastWriteTime();

  /**
   * Sets the last time when the player sends the last byte of data.
   * This operation is atomic and thread-safe.
   *
   * @param timestamp the last writing data time in milliseconds
   */
  void setLastWriteTime(long timestamp);

  /**
   * Determines whether the player is in an idle state.
   * A player is considered idle if they haven't performed any actions
   * within the configured idle time threshold.
   *
   * @return {@code true} if the player is idle, otherwise returns {@code false}
   */
  boolean isIdle();

  /**
   * Ensures that the player is never deported from the server even if they timeout.
   * This setting is useful for special players that should remain connected
   * regardless of activity status.
   *
   * @return {@code true} if the player should never be deported, otherwise returns {@code false}
   * @see AutoDisconnectPlayerTask
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
   * Updates role in thread-safe.
   *
   * @param expectedRole the current expected role
   * @param newRole      new role
   * @return {@code true} if the update is successful, otherwise returns {@code false}
   * @since 0.6.1
   */
  boolean transitionRole(PlayerRoleInRoom expectedRole, PlayerRoleInRoom newRole);

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
   * Retrieves a property belonging to the player.
   * This operation is thread-safe.
   *
   * @param key the property key
   * @return the property value if present, otherwise returns {@code null}
   */
  Object getProperty(String key);

  /**
   * Sets a property belonging to the player.
   * This operation is atomic and thread-safe.
   *
   * @param key   the property key
   * @param value the property value
   */
  void setProperty(String key, Object value);

  /**
   * Determines whether a property is available for the player.
   * This operation is thread-safe.
   *
   * @param key the property key to check
   * @return {@code true} if the property exists, otherwise returns {@code false}
   */
  boolean containsProperty(String key);

  /**
   * Removes a property belonging to the player.
   * This operation is atomic and thread-safe.
   *
   * @param key the property key to remove
   */
  void removeProperty(String key);

  /**
   * Removes all properties of the player.
   * This operation is atomic and thread-safe.
   */
  void clearProperties();

  /**
   * Observes all changes on the player.
   * This method allows registering a listener for player property changes.
   *
   * @param updateConsumer the action to perform when a player property changes
   * @since 0.5.0
   */
  void onUpdateListener(Consumer<Field> updateConsumer);

  /**
   * Wipes out all the player's information.
   * This operation is atomic and thread-safe.
   * It should be called when the player is being removed from the system.
   */
  void clean();

  /**
   * Sets the maximum time in seconds which allows the player to get in IDLE state (Do not
   * perform any action, such as reading or writing data).
   *
   * @param seconds the maximum time in seconds ({@code integer} value) which allows the
   *                player to get in IDLE state
   */
  void configureMaxIdleTimeInSeconds(int seconds);

  /**
   * Sets the maximum time in seconds which allows the player to get in IDLE state (Do not
   * perform any action, such as reading or writing data) in case of never deported selection.
   *
   * @param seconds the maximum time in seconds ({@code integer} value) which allows the
   *                player to get in IDLE state
   * @since 0.5.0
   */
  void configureMaxIdleTimeNeverDeportedInSeconds(int seconds);

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
