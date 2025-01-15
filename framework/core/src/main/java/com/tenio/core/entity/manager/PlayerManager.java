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

package com.tenio.core.entity.manager;

import com.tenio.core.entity.Player;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.RemovedNonExistentPlayerException;
import com.tenio.core.manager.Manager;
import com.tenio.core.network.entity.session.Session;
import java.util.Iterator;
import java.util.List;

/**
 * All supported APIs for the player management. A management can belong to a room or live stand
 * alone.
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
   * @param playerName    a unique player's name ({@link String} value) on the server
   * @param session a {@link Session} associated with the player
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
   * Retrieves a player by using its name.
   *
   * @param playerIdentity a unique {@link String} name of player on the server
   * @return an instance of {@link Player} if present, otherwise {@code null}
   */
  Player getPlayerByIdentity(String playerIdentity);

  /**
   * Retrieves an iterator for a player management list. This method should be used to prevent
   * the "escape references" issue.
   *
   * @return an iterator of {@link Player} management list
   * @see Iterator
   */
  Iterator<Player> getPlayerIterator();

  /**
   * Retrieves a read-only player management list. This method should be used to prevent the
   * "escape references" issue.
   *
   * @return a list of all {@link Player}s in the management list
   * @see List
   */
  List<Player> getReadonlyPlayersList();

  /**
   * Removes a player from the management list.
   *
   * @param playerIdentity the player's name ({@link String} value)
   * @throws RemovedNonExistentPlayerException when the player is not present in the
   *                                                   management list
   */
  void removePlayerByIdentity(String playerIdentity) throws RemovedNonExistentPlayerException;

  /**
   * Determines whether the management list contains a player by checking its name.
   *
   * @param playerIdentity the player's name ({@link String} value)
   * @return {@code true} if the player is available, otherwise returns {@code false}
   */
  boolean containsPlayerIdentity(String playerIdentity);

  /**
   * Retrieves the current number of players in the management list.
   *
   * @return the current number ({@code integer} value) of players in the list
   */
  int getPlayerCount();

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
   */
  void configureMaxIdleTimeNeverDeportedInSeconds(int seconds);

  /**
   * Removes all players from the list.
   */
  void clear();
}
