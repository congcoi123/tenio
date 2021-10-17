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

package com.tenio.core.entity;

import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.implement.RoomImpl;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.setting.strategy.RoomCredentialValidatedStrategy;
import com.tenio.core.entity.setting.strategy.RoomPlayerSlotGeneratedStrategy;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.PlayerJoinedRoomException;
import com.tenio.core.exception.RemovedNonExistentPlayerFromRoomException;
import com.tenio.core.exception.SwitchedPlayerSpectatorException;
import com.tenio.core.network.entity.session.Session;
import java.util.Collection;
import java.util.List;

/**
 * The abstract room object used in the server.
 */
public interface Room {

  long getId();

  String getName();

  void setName(String name) throws IllegalArgumentException;

  String getPassword();

  void setPassword(String password) throws IllegalArgumentException;

  RoomState getState();

  void setState(RoomState state);

  boolean isPublic();

  int getMaxPlayers();

  void setMaxPlayers(int maxPlayers) throws IllegalArgumentException;

  int getMaxSpectators();

  void setMaxSpectators(int maxSpectators) throws IllegalArgumentException;

  Player getOwner();

  void setOwner(Player owner);

  PlayerManager getPlayerManager();

  void setPlayerManager(PlayerManager playerManager);

  boolean isActivated();

  void setActivated(boolean activated);

  RoomRemoveMode getRoomRemoveMode();

  void setRoomRemoveMode(RoomRemoveMode roomRemoveMode);

  Object getProperty(String key);

  boolean containsProperty(String key);

  void addProperty(String key, Object value);

  void removeProperty(String key);

  int getCapacity();

  void setCapacity(int maxPlayers, int maxSpectators) throws IllegalArgumentException;

  List<Player> getPlayersList();

  List<Player> getSpectatorsList();

  int getPlayerCount();

  int getSpectatorCount();

  boolean containsPlayerName(String playerName);

  Player getPlayerByName(String playerName);

  Player getPlayerBySession(Session session);

  Collection<Player> getAllPlayersList();

  Collection<Session> getAllSessionList();

  void addPlayer(Player player, boolean asSpectator, int targetSlot)
      throws PlayerJoinedRoomException, AddedDuplicatedPlayerException;

  default void addPlayer(Player player, boolean asSpectator)
      throws PlayerJoinedRoomException, AddedDuplicatedPlayerException {
    addPlayer(player, asSpectator, RoomImpl.DEFAULT_SLOT);
  }

  default void addPlayer(Player player)
      throws PlayerJoinedRoomException, AddedDuplicatedPlayerException {
    addPlayer(player, false);
  }

  void removePlayer(Player player) throws RemovedNonExistentPlayerFromRoomException;

  void switchPlayerToSpectator(Player player) throws SwitchedPlayerSpectatorException;

  void switchSpectatorToPlayer(Player player, int targetSlot)
      throws SwitchedPlayerSpectatorException;

  default void switchSpectatorToPlayer(Player player) throws SwitchedPlayerSpectatorException {
    switchSpectatorToPlayer(player, RoomImpl.DEFAULT_SLOT);
  }

  boolean isEmpty();

  boolean isFull();

  RoomPlayerSlotGeneratedStrategy getPlayerSlotGeneratedStrategy();

  void setPlayerSlotGeneratedStrategy(
      RoomPlayerSlotGeneratedStrategy roomPlayerSlotGeneratedStrategy);

  RoomCredentialValidatedStrategy getRoomCredentialValidatedStrategy();

  void setRoomCredentialValidatedStrategy(
      RoomCredentialValidatedStrategy roomCredentialValidatedStrategy);

  default void clear() {
    throw new UnsupportedOperationException();
  }
}
