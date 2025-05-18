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

package com.tenio.core.api.implement;

import com.tenio.common.data.DataCollection;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.api.ServerApi;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Channel;
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
import com.tenio.core.entity.manager.ChannelManager;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.CreatedRoomException;
import com.tenio.core.exception.PlayerJoinedRoomException;
import com.tenio.core.exception.RemovedNonExistentPlayerException;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.server.Server;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An implementation for Server APIs.
 */
public final class ServerApiImpl extends SystemLogger implements ServerApi {

  private final Server server;

  private ServerApiImpl(Server server) {
    this.server = server;
  }

  /**
   * Initialization.
   *
   * @param server an instance of {@link Server}
   * @return an instance of {@link ServerApi}
   */
  public static ServerApi newInstance(Server server) {
    return new ServerApiImpl(server);
  }

  @Override
  public void login(String playerName) {
    try {
      final var player = getPlayerManager().createPlayer(playerName);

      getEventManager().emit(ServerEvent.PLAYER_LOGIN_RESULT, player,
          PlayerLoggedInResult.SUCCESS);
    } catch (AddedDuplicatedPlayerException exception) {
      error(exception, "Logged in with same player name: ", playerName);
      getEventManager().emit(ServerEvent.PLAYER_LOGIN_RESULT, exception.getPlayer(),
          PlayerLoggedInResult.DUPLICATED_PLAYER);
    }
  }

  @Override
  public void login(String playerName, Session session) {
    try {
      Player player = getPlayerManager().createPlayerWithSession(playerName, session);

      getEventManager().emit(ServerEvent.PLAYER_LOGIN_RESULT, player,
          PlayerLoggedInResult.SUCCESS);
    } catch (Exception exception) {
      if (exception instanceof AddedDuplicatedPlayerException duplicatedPlayerException) {
        error(exception, "Logged in with same player name: ", playerName);
        getEventManager().emit(ServerEvent.PLAYER_LOGIN_RESULT,
            duplicatedPlayerException.getPlayer(), PlayerLoggedInResult.DUPLICATED_PLAYER);
        return;
      }
      error(exception, "On the player: ", playerName);
      getEventManager().emit(ServerEvent.PLAYER_LOGIN_RESULT,
          getPlayerByIdentity(playerName).orElse(null), PlayerLoggedInResult.EXCEPTION);
    }
  }

  @Override
  public void login(Player player) {
    try {
      getPlayerManager().configureInitialPlayer(player);
      getPlayerManager().addPlayer(player);

      getEventManager().emit(ServerEvent.PLAYER_LOGIN_RESULT, player,
          PlayerLoggedInResult.SUCCESS);
    } catch (AddedDuplicatedPlayerException exception) {
      error(exception, "Logged in with same player name: ", player.getIdentity());
      getEventManager().emit(ServerEvent.PLAYER_LOGIN_RESULT, exception.getPlayer(),
          PlayerLoggedInResult.DUPLICATED_PLAYER);
    }
  }

  @Override
  public void logout(Player player, ConnectionDisconnectMode connectionDisconnectMode,
                     PlayerDisconnectMode playerDisconnectMode) {
    if (Objects.isNull(player)) {
      // maybe we needn't do anything
      return;
    }

    try {
      if (player.containsSession() && player.getSession().isPresent()) {
        // check process on method InternalProcessorServiceImpl#processSessionWillBeClosed
        player.getSession().get().close(connectionDisconnectMode, playerDisconnectMode);
      } else {
        // unsubscribe it from all channels
        unsubscribeFromAllChannels(player);
        // player should leave room (if applicable) first
        if (player.isInRoom()) {
          leaveRoom(player, PlayerLeaveRoomMode.LOG_OUT);
        }
        getEventManager().emit(ServerEvent.DISCONNECT_PLAYER, player, playerDisconnectMode);
        String removedPlayer = player.getIdentity();
        getPlayerManager().removePlayerByIdentity(removedPlayer);
        debug("DISCONNECTED PLAYER", "Player ", removedPlayer, " was removed");
        player.clean();
      }
    } catch (RemovedNonExistentPlayerException | IOException exception) {
      error(exception, "Remove player: ", player.getIdentity(), " with issue");
    }
  }

  @Override
  public Room createRoom(InitialRoomSetting roomSetting, Player roomOwner) {
    Room room = null;
    try {
      if (Objects.isNull(roomOwner)) {
        room = getRoomManager().createRoom(roomSetting);
      } else {
        room = getRoomManager().createRoomWithOwner(roomSetting, roomOwner);
      }
      getEventManager().emit(ServerEvent.ROOM_CREATED_RESULT, room,
          roomSetting, RoomCreatedResult.SUCCESS);
    } catch (IllegalArgumentException exception) {
      getEventManager().emit(ServerEvent.ROOM_CREATED_RESULT, null, roomSetting,
          RoomCreatedResult.INVALID_NAME_OR_PASSWORD);
    } catch (CreatedRoomException exception) {
      getEventManager().emit(ServerEvent.ROOM_CREATED_RESULT, null, roomSetting,
          exception.getResult());
    }

    return room;
  }

  @Override
  public Room addRoom(Room room, InitialRoomSetting roomSetting, Player roomOwner) {
    try {
      getRoomManager().addRoomWithOwner(room, roomSetting, roomOwner);
      getEventManager().emit(ServerEvent.ROOM_CREATED_RESULT, room,
          roomSetting, RoomCreatedResult.SUCCESS);
      return room;
    } catch (IllegalArgumentException exception) {
      getEventManager().emit(ServerEvent.ROOM_CREATED_RESULT, null, roomSetting,
          RoomCreatedResult.INVALID_NAME_OR_PASSWORD);
    } catch (CreatedRoomException exception) {
      getEventManager().emit(ServerEvent.ROOM_CREATED_RESULT, null, roomSetting,
          exception.getResult());
    }
    return null;
  }

  @Override
  public Optional<Player> getPlayerByIdentity(String identity) {
    return Optional.ofNullable(getPlayerManager().getPlayerByIdentity(identity));
  }

  @Override
  public int getPlayerCount() {
    return getPlayerManager().getPlayerCount();
  }

  @Override
  public Iterator<Player> getPlayerIterator() {
    return getPlayerManager().getPlayerIterator();
  }

  @Override
  public List<Player> getReadonlyPlayersList() {
    return getPlayerManager().getReadonlyPlayersList();
  }

  @Override
  public Optional<Room> getRoomById(long roomId) {
    return Optional.ofNullable(getRoomManager().getRoomById(roomId));
  }

  @Override
  public Iterator<Room> getRoomIterator() {
    return getRoomManager().getRoomIterator();
  }

  @Override
  public List<Room> getReadonlyRoomsList() {
    return getRoomManager().getReadonlyRoomsList();
  }

  @Override
  public int getRoomCount() {
    return getRoomManager().getRoomCount();
  }

  @Override
  public void joinRoom(Player player, Room room, String roomPassword, int slotInRoom,
                       boolean asSpectator) {
    if (Objects.isNull(player) || Objects.isNull(room)) {
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
      room.addPlayer(player, roomPassword, asSpectator, slotInRoom);
      player.setCurrentRoom(room);
      getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
          PlayerJoinedRoomResult.SUCCESS);
    } catch (PlayerJoinedRoomException exception) {
      getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
          exception.getResult());
    } catch (AddedDuplicatedPlayerException exception) {
      getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
          PlayerJoinedRoomResult.DUPLICATED_PLAYER);
    }
  }

  @Override
  public void changeRoom(Player player, Room room, String roomPassword, int slotInRoom,
                         boolean asSpectator) {
    if (Objects.isNull(player) || Objects.isNull(room)) {
      getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
          PlayerJoinedRoomResult.PLAYER_OR_ROOM_UNAVAILABLE);
      return;
    }

    if (player.isInRoom()) {
      leaveRoom(player, PlayerLeaveRoomMode.CHANGE_ROOM);
    }

    joinRoom(player, room, roomPassword, slotInRoom, asSpectator);
  }

  @Override
  public void leaveRoom(Player player, PlayerLeaveRoomMode leaveRoomMode) {
    if (!player.isInRoom()) {
      getEventManager().emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, null,
          leaveRoomMode, PlayerLeftRoomResult.PLAYER_ALREADY_LEFT_ROOM);
      return;
    }

    var room = player.getCurrentRoom().orElse(null);
    getEventManager().emit(ServerEvent.PLAYER_BEFORE_LEAVE_ROOM, player, room,
        leaveRoomMode);
    try {
      if (Objects.nonNull(room)) {
        room.removePlayer(player);
      }
      getEventManager().emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, room,
          leaveRoomMode, PlayerLeftRoomResult.SUCCESS);
    } catch (RemovedNonExistentPlayerException exception) {
      getEventManager().emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, room,
          leaveRoomMode, PlayerLeftRoomResult.PLAYER_ALREADY_LEFT_ROOM);
    }
  }

  @Override
  public void removeRoom(Room room, RoomRemoveMode removeRoomMode) {
    if (Objects.isNull(room)) {
      // nothing needs to do
      return;
    }

    getEventManager().emit(ServerEvent.ROOM_WILL_BE_REMOVED, room, removeRoomMode);

    var playerIterator = room.getPlayerIterator();
    while (playerIterator.hasNext()) {
      var player = playerIterator.next();
      leaveRoom(player, PlayerLeaveRoomMode.ROOM_REMOVED);
    }

    long roomId = room.getId();
    getRoomManager().removeRoomById(roomId);
    debug("REMOVED ROOM", "Room ", roomId, " was removed");
  }

  @Override
  public void createChannel(String id, String description) {
    getChannelManager().createChannel(id, description);
  }

  @Override
  public void removeChannel(String id) {
    getChannelManager().removeChannel(id);
  }

  @Override
  public void subscribeToChannel(Channel channel, Player player) {
    getChannelManager().subscribe(channel, player);
  }

  @Override
  public void unsubscribeFromChannel(Channel channel, Player player) {
    getChannelManager().unsubscribe(channel, player);
  }

  @Override
  public void unsubscribeFromAllChannels(Player player) {
    getChannelManager().unsubscribe(player);
  }

  @Override
  public void broadcastToChannel(Channel channel, DataCollection message) {
    getChannelManager().broadcast(channel, message);
  }

  @Override
  public Map<String, Channel> getSubscribedChannelsForPlayer(Player player) {
    return getChannelManager().getSubscribedChannelsForPlayer(player);
  }

  @Override
  public int getUdpPort() {
    return server.getDatagramChannelManager().getUdpPort();
  }

  @Override
  public int getKcpPort() {
    return server.getDatagramChannelManager().getKcpPort();
  }

  @Override
  public int getCurrentKcpConveyId() {
    return server.getDatagramChannelManager().getCurrentKcpConveyId();
  }

  @Override
  public long getStartedTime() {
    return server.getStartedTime();
  }

  @Override
  public long getUptime() {
    return server.getUptime();
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

  private ChannelManager getChannelManager() {
    return server.getChannelManager();
  }
}
