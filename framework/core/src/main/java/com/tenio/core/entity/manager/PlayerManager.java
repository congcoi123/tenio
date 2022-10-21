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
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.RemovedNonExistentPlayerFromRoomException;
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
   * @param name a unique player's name ({@link String} value) on the server
   * @return a new instance of {@link Player}
   * @throws AddedDuplicatedPlayerException when a same player is already available in the
   *                                        management list, but it is mentioned again
   */
  Player createPlayer(String name) throws AddedDuplicatedPlayerException;

  /**
   * Creates a new player with session and adds it in to the management list.
   *
   * @param name    a unique player's name ({@link String} value) on the server
   * @param session a {@link Session} associated with the player
   * @return a new instance of {@link Player}
   * @throws AddedDuplicatedPlayerException when a same player is already available in the
   *                                        management list, but it is mentioned again
   * @throws NullPointerException           when the attaching session could not be found
   */
  Player createPlayerWithSession(String name, Session session)
      throws AddedDuplicatedPlayerException, NullPointerException;

  /**
   * Retrieves a player by using its name.
   *
   * @param playerName a unique {@link String} name of player on the server
   * @return an instance of {@link Player} if present, otherwise {@code null}
   */
  Player getPlayerByName(String playerName);

  /**
   * Retrieves a player by using its session.
   *
   * @param session the {@link Session} associated with a player on the server
   * @return an instance of {@link Player} if present, otherwise {@code null}
   */
  Player getPlayerBySession(Session session);

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
   * Retrieves an iterator for sessions in a player management list. This method should be
   * used to prevent the "escape references" issue.
   *
   * @return an iterator of {@link Session} in the player management list
   * @see Iterator
   */
  Iterator<Session> getSessionIterator();

  /**
   * Retrieves a read-only list of sessions in a player management list. This method should be
   * used to prevent the "escape references" issue.
   *
   * @return a list of {@link Session}s in the player management list
   * @see List
   */
  List<Session> getReadonlySessionsList();

  /**
   * Removes a player from the management list.
   *
   * @param playerName the player's name ({@link String} value)
   * @throws RemovedNonExistentPlayerFromRoomException when the player is not present in the
   *                                                   management list
   */
  void removePlayerByName(String playerName) throws RemovedNonExistentPlayerFromRoomException;

  /**
   * Removes a player from the management list.
   *
   * @param session the {@link Session} associated to a player
   * @throws RemovedNonExistentPlayerFromRoomException when the player is not present in the
   *                                                   management list
   */
  void removePlayerBySession(Session session) throws RemovedNonExistentPlayerFromRoomException;

  /**
   * Determines whether the management list contains a player by checking its name.
   *
   * @param playerName the player's name ({@link String} value)
   * @return {@code true} if the player is available, otherwise returns {@code false}
   */
  boolean containsPlayerName(String playerName);

  /**
   * Determines whether the management list contains a player by checking its session.
   *
   * @param session the {@link Session} associated to a player
   * @return {@code true} if the player is available, otherwise returns {@code false}
   */
  boolean containsPlayerSession(Session session);

  /**
   * Retrieves a room of the management list.
   *
   * @return an instance of {@link Room} if present, otherwise {@code null}
   */
  Room getOwnerRoom();

  /**
   * Adds the management list to a room.
   *
   * @param room an instance of {@link Room}, this value can be {@code null}. In that case,
   *             the management list does not belong to any room
   */
  void setOwnerRoom(Room room);

  /**
   * Retrieves the current number of players in the management list.
   *
   * @return the current number ({@code integer} value) of players in the list
   */
  int getPlayerCount();

  /**
   * Removes all players from the list.
   */
  void clear();
}
