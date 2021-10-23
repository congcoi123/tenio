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

package com.tenio.core.entity.manager;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.RemovedNonExistentPlayerFromRoomException;
import com.tenio.core.manager.Manager;
import com.tenio.core.network.entity.session.Session;
import java.util.Collection;

/**
 * All supported APIs for player management.
 */
public interface PlayerManager extends Manager {

  void addPlayer(Player player) throws AddedDuplicatedPlayerException;

  Player createPlayer(String name) throws AddedDuplicatedPlayerException;

  Player createPlayerWithSession(String name, Session session)
      throws AddedDuplicatedPlayerException, NullPointerException;

  Player getPlayerByName(String playerName);

  Player getPlayerBySession(Session session);

  Collection<Player> getAllPlayers();

  Collection<Session> getAllSessions();

  void removePlayerByName(String playerName) throws RemovedNonExistentPlayerFromRoomException;

  void removePlayerBySession(Session session) throws RemovedNonExistentPlayerFromRoomException;

  boolean containsPlayerName(String playerName);

  boolean containsPlayerSession(Session session);

  Room getOwnerRoom();

  void setOwnerRoom(Room room);

  int getPlayerCount();

  void clear();
}
