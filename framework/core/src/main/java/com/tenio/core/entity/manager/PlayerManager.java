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

package com.tenio.core.entity.manager;

import com.tenio.core.entity.Player;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.RemovedNonExistentPlayerException;
import com.tenio.core.manager.Manager;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.schedule.task.internal.AutoDisconnectPlayerTask;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages the lifecycle and operations of players in the game server.
 * This interface provides comprehensive player management capabilities including
 * player creation, removal, and state tracking.
 *
 * <p>Key features:
 * <ul>
 *   <li>Player lifecycle management (creation, removal, cleanup)</li>
 *   <li>Player state tracking (idle time, activity monitoring)</li>
 *   <li>Thread-safe player operations</li>
 *   <li>Player identity management</li>
 *   <li>Player property management</li>
 *   <li>Player event handling</li>
 * </ul>
 *
 * <p>Thread safety: Implementations of this interface should be thread-safe
 * as they handle concurrent player operations. The interface provides atomic
 * operations for player management and state changes.
 *
 * <p>Note: This interface is designed to work in conjunction with the
 * {@link AutoDisconnectPlayerTask} for automatic player cleanup based on
 * idle time and activity status.
 *
 * @see Player
 * @see AutoDisconnectPlayerTask
 * @see RemovedNonExistentPlayerException
 * @since 0.5.0
 */
public interface PlayerManager extends Manager {

  /**
   * Adds a new player in to the management list.
   *
   * @param player a created {@link Player}
   * @throws AddedDuplicatedPlayerException when a same player is already available in the
   *                                        management list, but it is mentioned again
   */
  void addPlayer(Player player) throws AddedDuplicatedPlayerException;

  /**
   * Creates a new player without session and adds it in to the management list.
   *
   * @param playerName a unique player's name ({@link String} value) on the server
   * @return a new instance of {@link Player}
   * @throws AddedDuplicatedPlayerException when a same player is already available in the
   *                                        management list, but it is mentioned again
   */
  Player createPlayer(String playerName) throws AddedDuplicatedPlayerException;

  /**
   * Creates a new player with session and adds it in to the management list.
   *
   * @param playerName a unique player's name ({@link String} value) on the server
   * @param session    a {@link Session} associated with the player
   * @return a new instance of {@link Player}
   * @throws AddedDuplicatedPlayerException when a same player is already available in the
   *                                        management list, but it is mentioned again
   * @throws NullPointerException           when the attaching session could not be found
   */
  Player createPlayerWithSession(String playerName, Session session)
      throws AddedDuplicatedPlayerException, NullPointerException;

  /**
   * Configures basic info when a player is initially created, or before it is added into
   * the management list.
   *
   * @param player the target player
   */
  void configureInitialPlayer(Player player);

  /**
   * Retrieves a player by their unique identity.
   * This method provides thread-safe access to player instances.
   *
   * @param playerIdentity the unique identifier of the player
   * @return the {@link Player} instance if found, otherwise returns {@code null}
   */
  Player getPlayerByIdentity(String playerIdentity);

  /**
   * Ensures the calculation on the player list is thread-safe.
   *
   * @param onComputed a {@link Consumer} to handle the logic
   * @since 0.6.6
   */
  void computePlayers(Consumer<Iterator<Player>> onComputed);

  /**
   * Retrieves a read-only player management list.
   * This method should be used to prevent the "escape references" issue
   * and provides thread-safe access to the player collection.
   *
   * @return a list of all {@link Player}s in the management list
   * @see List
   */
  List<Player> getReadonlyPlayersList();

  /**
   * Removes a player from the management list.
   * This operation is atomic and thread-safe.
   *
   * @param playerIdentity the player's unique identifier
   * @throws RemovedNonExistentPlayerException when the player is not present in the
   *                                           management list
   */
  void removePlayerByIdentity(String playerIdentity) throws RemovedNonExistentPlayerException;

  /**
   * Determines whether the management list contains a player by checking their identity.
   * This operation is thread-safe.
   *
   * @param playerIdentity the player's unique identifier
   * @return {@code true} if the player is available, otherwise returns {@code false}
   */
  boolean containsPlayerIdentity(String playerIdentity);

  /**
   * Retrieves the current number of players in the management list.
   * This operation is thread-safe and provides an atomic count.
   *
   * @return the current number of players in the list
   */
  int getPlayerCount();

  /**
   * Sets the maximum time in seconds which allows the player to get in IDLE state.
   * This configuration affects all players managed by this manager.
   * Players exceeding this idle time may be automatically disconnected.
   *
   * @param seconds the maximum time in seconds which allows the player to get in IDLE state
   * @see AutoDisconnectPlayerTask
   */
  void configureMaxIdleTimeInSeconds(int seconds);

  /**
   * Sets the maximum time in seconds which allows the player to get in IDLE state
   * in case of never deported selection. This configuration is used for special
   * players that should not be automatically disconnected.
   *
   * @param seconds the maximum time in seconds which allows the player to get in IDLE state
   * @see Player#isNeverDeported()
   */
  void configureMaxIdleTimeNeverDeportedInSeconds(int seconds);

  /**
   * Removes all players from the list.
   */
  void clear();
}
