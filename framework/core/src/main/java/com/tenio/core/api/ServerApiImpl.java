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

package com.tenio.core.api;

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerLeaveRoomMode;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.define.result.PlayerJoinedRoomResult;
import com.tenio.core.entity.define.result.PlayerLeftRoomResult;
import com.tenio.core.entity.define.result.PlayerLoggedInResult;
import com.tenio.core.entity.define.result.RoomCreatedResult;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.CreatedRoomException;
import com.tenio.core.exception.PlayerJoinedRoomException;
import com.tenio.core.exception.RemovedNonExistentPlayerFromRoomException;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.server.Server;
import java.io.IOException;
import java.util.Collection;

/**
 * All supported APIs could be found in this class.
 *
 * @see ServerApi
 */
public final class ServerApiImpl extends SystemLogger implements ServerApi {

  private final Server server;

  private ServerApiImpl(Server server) {
    this.server = server;
  }

  public static ServerApi newInstance(Server server) {
    return new ServerApiImpl(server);
  }

  @Override
  public void login(String playerName) {
    try {
      var player = getPlayerManager().createPlayer(playerName);

      getEventManager().emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, player,
          PlayerLoggedInResult.SUCCESS);
    } catch (AddedDuplicatedPlayerException e) {
      error(e, "Logged in with same player name: ", playerName);
      getEventManager().emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, null,
          PlayerLoggedInResult.DUPLICATED_PLAYER);
    }
  }

  @Override
  public void login(String playerName, Session session) {
    try {
      final var player = getPlayerManager().createPlayerWithSession(playerName, session);
      session.setName(playerName);
      session.setConnected(true);
      session.activate();

      getEventManager().emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, player,
          PlayerLoggedInResult.SUCCESS);
    } catch (NullPointerException e) {
      error(e, "Unable to find session when logged in with the player name: ", playerName);
      getEventManager().emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, null,
          PlayerLoggedInResult.SESSION_NOT_FOUND);
    } catch (AddedDuplicatedPlayerException e) {
      error(e, "Logged in with same player name: ", playerName);
      getEventManager().emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, null,
          PlayerLoggedInResult.DUPLICATED_PLAYER);
    }
  }

  @Override
  public void logout(Player player) {
    if (player == null) {
      // maybe we needn't do anything
      return;
    }

    try {
      if (player.isInRoom()) {
        leaveRoom(player, PlayerLeaveRoomMode.LOG_OUT);
      }

      disconnectPlayer(player);

      player = null;
    } catch (RemovedNonExistentPlayerFromRoomException e) {
      error(e, "Removed player: ", player.getName(), " with issue");
    } catch (IOException e) {
      error(e, "Removed player: ", player.getName(), " with issue");
    }
  }

  private void disconnectPlayer(Player player) throws IOException {
    if (player.containsSession()) {
      player.getSession().close(ConnectionDisconnectMode.DEFAULT, PlayerDisconnectMode.DEFAULT);
    } else {
      getEventManager().emit(ServerEvent.DISCONNECT_PLAYER, player, PlayerDisconnectMode.DEFAULT);
      getPlayerManager().removePlayerByName(player.getName());
      player.clean();
    }
  }

  @Override
  public Room createRoom(InitialRoomSetting setting, Player owner) {
    Room room = null;
    try {
      if (owner == null) {
        room = getRoomManager().createRoom(setting);
      } else {
        room = getRoomManager().createRoomWithOwner(setting, owner);
        getEventManager().emit(ServerEvent.ROOM_CREATED_RESULT, room, setting,
            RoomCreatedResult.SUCCESS);
      }
    } catch (IllegalArgumentException e) {
      getEventManager().emit(ServerEvent.ROOM_CREATED_RESULT, null, setting,
          RoomCreatedResult.INVALID_NAME_OR_PASSWORD);
    } catch (CreatedRoomException e) {
      getEventManager().emit(ServerEvent.ROOM_CREATED_RESULT, null, setting, e.getResult());
    }

    return room;
  }

  @Override
  public Player getPlayerByName(String playerName) {
    return getPlayerManager().getPlayerByName(playerName);
  }

  @Override
  public Player getPlayerBySession(Session session) {
    return getPlayerManager().getPlayerBySession(session);
  }

  @Override
  public int getPlayerCount() {
    return getPlayerManager().getPlayerCount();
  }

  @Override
  public Collection<Player> getAllPlayers() {
    return getPlayerManager().getAllPlayers();
  }

  @Override
  public Room getRoomById(long roomId) {
    return getRoomManager().getRoomById(roomId);
  }

  @Override
  public void joinRoom(Player player, Room room, String roomPassword, int slotInRoom,
                       boolean asSpectator) {
    if (player == null || room == null) {
      getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
          PlayerJoinedRoomResult.PLAYER_OR_ROOM_UNAVAILABLE);
      return;
    }

    if (player.isInRoom()) {
      getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
          PlayerJoinedRoomResult.PLAYER_IS_IN_ANOTHER_ROOM);
      return;
    }

    try {
      room.addPlayer(player, asSpectator, slotInRoom);
      player.setCurrentRoom(room);
      getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
          PlayerJoinedRoomResult.SUCCESS);
    } catch (PlayerJoinedRoomException e) {
      getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room, e.getResult());
    } catch (AddedDuplicatedPlayerException e) {
      error(e, e.getMessage());
      getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
          PlayerJoinedRoomResult.DUPLICATED_PLAYER);
    }
  }

  @Override
  public void leaveRoom(Player player, PlayerLeaveRoomMode leaveRoomMode) {
    if (!player.isInRoom()) {
      getEventManager().emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, null,
          PlayerLeftRoomResult.PLAYER_ALREADY_LEFT_ROOM);
      return;
    }

    var room = player.getCurrentRoom();

    getEventManager().emit(ServerEvent.PLAYER_BEFORE_LEAVE_ROOM, player, room, leaveRoomMode);

    try {
      room.removePlayer(player);
      getEventManager().emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, room,
          PlayerLeftRoomResult.SUCCESS);
    } catch (RemovedNonExistentPlayerFromRoomException e) {
      getEventManager().emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, room,
          PlayerLeftRoomResult.PLAYER_ALREADY_LEFT_ROOM);
    }
  }

  @Override
  public void removeRoom(Room room, RoomRemoveMode removeRoomMode) {
    if (room == null) {
      // nothing needs to do
      return;
    }

    getEventManager().emit(ServerEvent.ROOM_WILL_BE_REMOVED, room, removeRoomMode);

    var players = room.getAllPlayersList();
    var iterator = players.iterator();

    while (iterator.hasNext()) {
      var player = iterator.next();
      leaveRoom(player, PlayerLeaveRoomMode.ROOM_REMOVED);
    }

    getRoomManager().removeRoomById(room.getId());
  }

  private EventManager getEventManager() {
    return server.getEventManager();
  }

  private PlayerManager getPlayerManager() {
    return server.getPlayerManager();
  }

  private RoomManager getRoomManager() {
    return server.getRoomManager();
  }
}
