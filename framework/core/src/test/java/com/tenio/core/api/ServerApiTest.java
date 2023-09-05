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

package com.tenio.core.api;

import com.tenio.common.data.DataCollection;
import com.tenio.core.api.implement.ServerApiImpl;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerBanMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerLeaveRoomMode;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.define.result.PlayerJoinedRoomResult;
import com.tenio.core.entity.define.result.PlayerLeftRoomResult;
import com.tenio.core.entity.define.result.PlayerLoggedInResult;
import com.tenio.core.entity.define.result.RoomCreatedResult;
import com.tenio.core.entity.implement.PlayerImpl;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.CreatedRoomException;
import com.tenio.core.exception.PlayerJoinedRoomException;
import com.tenio.core.exception.RemovedNonExistentPlayerFromRoomException;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.implement.SessionImpl;
import com.tenio.core.network.zero.engine.manager.UdpChannelManager;
import com.tenio.core.server.Server;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("All unit test cases for the Server Api")
@ExtendWith(MockitoExtension.class)
class ServerApiTest {

  @Mock
  Server server;

  @Mock
  PlayerManager playerManager;

  @Mock
  RoomManager roomManager;

  @Mock
  UdpChannelManager udpChannelManager;

  @Mock
  EventManager eventManager;

  @Mock
  Optional<Room> optionalRoom;

  @Mock
  Optional<Session> optionalSession;

  ServerApi serverApi;

  @BeforeEach
  void initialization() {
    serverApi = ServerApiImpl.newInstance(server);
  }

  @Test
  @DisplayName("It should create a new instance")
  void itShouldCreateNewInstance() {
    ServerApiImpl.newInstance(Mockito.mock(Server.class));
  }

  @Test
  @DisplayName("When a player login without session, and it has no exception")
  void playerLoginWithoutSessionShouldHaveNoException() {
    Mockito.when(server.getPlayerManager()).thenReturn(playerManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var loginName = "test";
    var loginPlayer = PlayerImpl.newInstance(loginName);
    Mockito.when(playerManager.createPlayer(loginName)).thenReturn(loginPlayer);
    serverApi.login(loginName);
    Mockito.verify(eventManager, Mockito.times(1))
        .emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, loginPlayer,
            PlayerLoggedInResult.SUCCESS);
  }

  @Test
  @DisplayName("When a player login without session, and it has duplicated exception")
  void playerLoginWithoutSessionShouldHaveDuplicatedException() {
    Mockito.when(server.getPlayerManager()).thenReturn(playerManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var loginName = "test";
    var loginPlayer = PlayerImpl.newInstance(loginName);
    Mockito.doThrow(new AddedDuplicatedPlayerException(loginPlayer, Mockito.mock(Room.class)))
        .when(playerManager).createPlayer(loginName);
    serverApi.login(loginName);
    Mockito.verify(eventManager, Mockito.times(1))
        .emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, loginPlayer,
            PlayerLoggedInResult.DUPLICATED_PLAYER);
  }

  @Test
  @DisplayName("When a player login with a session, it should have no exception")
  void playerLoginWithSessionShouldHaveNoException() {
    Mockito.when(server.getPlayerManager()).thenReturn(playerManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var loginName = "test";
    var loginPlayer = PlayerImpl.newInstance(loginName);
    var loginSession = SessionImpl.newInstance();
    Mockito.when(playerManager.createPlayerWithSession(loginName, loginSession))
        .thenReturn(loginPlayer);
    serverApi.login(loginName, loginSession);
    Mockito.verify(eventManager, Mockito.times(1))
        .emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, loginPlayer,
            PlayerLoggedInResult.SUCCESS);
  }

  @Test
  @DisplayName("When a player login with an unavailable session, it should have null pointer " +
      "exception")
  void playerLoginWithSessionShouldHaveNullPointerException() {
    Mockito.when(server.getPlayerManager()).thenReturn(playerManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var loginName = "test";
    var loginSession = SessionImpl.newInstance();
    Mockito.doThrow(NullPointerException.class)
        .when(playerManager).createPlayerWithSession(loginName, loginSession);
    serverApi.login(loginName, loginSession);
    Mockito.verify(eventManager, Mockito.times(1))
        .emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, null,
            PlayerLoggedInResult.EXCEPTION);
  }

  @Test
  @DisplayName("When a player login with a session, and it has duplicated exception")
  void playerLoginWithSessionShouldHaveDuplicatedException() {
    Mockito.when(server.getPlayerManager()).thenReturn(playerManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var loginName = "test";
    var loginPlayer = PlayerImpl.newInstance(loginName);
    var loginSession = SessionImpl.newInstance();
    Mockito.doThrow(new AddedDuplicatedPlayerException(loginPlayer, Mockito.mock(Room.class)))
        .when(playerManager).createPlayerWithSession(loginName, loginSession);
    serverApi.login(loginName, loginSession);
    Mockito.verify(eventManager, Mockito.times(1))
        .emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, loginPlayer,
            PlayerLoggedInResult.DUPLICATED_PLAYER);
  }

  @Test
  @DisplayName("When it tries to logout a null player, it should not do any further action")
  void itLogoutNullPlayerShouldDoNothingFurther() {
    serverApi.logout(null, ConnectionDisconnectMode.DEFAULT, PlayerDisconnectMode.DEFAULT);
    Mockito.verifyNoMoreInteractions(eventManager, playerManager);
  }

  @Test
  @DisplayName("When it tries to logout a player in a room, the player should leave the room first")
  void itLogoutPlayerInRoomShouldLeaveRoomFirst() {
    Mockito.when(server.getPlayerManager()).thenReturn(playerManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var player = Mockito.mock(Player.class);
    var room = Mockito.mock(Room.class);
    Mockito.when(optionalRoom.get()).thenReturn(room);
    optionalRoom.get().addPlayer(player);
    serverApi.logout(player, ConnectionDisconnectMode.DEFAULT, PlayerDisconnectMode.DEFAULT);
    Mockito.verify(eventManager, Mockito.times(1))
        .emit(ServerEvent.DISCONNECT_PLAYER, player, PlayerDisconnectMode.DEFAULT);
  }

  @Test
  @DisplayName("When it tries to logout a player, and remove him from a nonexistent room, an " +
      "exception RemovedNonExistentPlayer should be thrown")
  void itLogoutPlayerNotInRoomShouldHaveRemovedNonExistentPlayerException() {
    Mockito.when(server.getPlayerManager()).thenReturn(playerManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var player = Mockito.mock(Player.class);
    var room = Mockito.mock(Room.class);
    Mockito.when(optionalRoom.get()).thenReturn(room);
    optionalRoom.get().addPlayer(player);
    serverApi.logout(player, ConnectionDisconnectMode.DEFAULT, PlayerDisconnectMode.DEFAULT);
    Mockito.verify(eventManager, Mockito.times(1))
        .emit(ServerEvent.DISCONNECT_PLAYER, player, PlayerDisconnectMode.DEFAULT);
  }

  @Test
  @DisplayName("When it tries to logout a player which has session, the session should be closed")
  void itLogoutPlayerHasSessionShouldCloseSession() {
    Mockito.when(server.getPlayerManager()).thenReturn(playerManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var player = Mockito.mock(Player.class);
    Mockito.when(player.getSession()).thenReturn(optionalSession);
    Mockito.when(player.containsSession()).thenReturn(true);
    serverApi.logout(player, ConnectionDisconnectMode.DEFAULT, PlayerDisconnectMode.DEFAULT);
    Mockito.verify(eventManager, Mockito.times(1))
        .emit(ServerEvent.DISCONNECT_PLAYER, player, PlayerDisconnectMode.DEFAULT);
  }

  @Test
  @DisplayName("When it tries to logout a player which has session, and the closed session has IO" +
      " exception")
  void itLogoutPlayerHasSessionShouldHaveClosedSessionIoException() {
    Mockito.when(server.getPlayerManager()).thenReturn(playerManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var player = Mockito.mock(Player.class);
    Mockito.when(player.getSession()).thenReturn(optionalSession);
    Mockito.when(player.containsSession()).thenReturn(true);
    serverApi.logout(player, ConnectionDisconnectMode.DEFAULT, PlayerDisconnectMode.DEFAULT);
  }

  @Test
  @DisplayName("When it created successfully a new room without owner")
  void itCreateRoomWithoutOwnerShouldBeSuccessful() {
    Mockito.when(server.getRoomManager()).thenReturn(roomManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var setting = Mockito.mock(InitialRoomSetting.class);
    var createdRoom = Mockito.mock(Room.class);
    Mockito.when(roomManager.createRoom(setting)).thenReturn(createdRoom);
    serverApi.createRoom(setting);
    Mockito.verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, Optional.ofNullable(createdRoom), setting,
        RoomCreatedResult.SUCCESS);
  }

  @Test
  @DisplayName("When it failed to create a new room without owner, and throw invalid credentials " +
      "exception")
  void itCreateRoomWithoutOwnerShouldThrowInvalidCredentialsException() {
    Mockito.when(server.getRoomManager()).thenReturn(roomManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var setting = Mockito.mock(InitialRoomSetting.class);
    Mockito.doThrow(IllegalArgumentException.class).when(roomManager).createRoom(setting);
    serverApi.createRoom(setting);
    Mockito.verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, Optional.empty(), setting,
        RoomCreatedResult.INVALID_NAME_OR_PASSWORD);
  }

  @Test
  @DisplayName("When it failed to create a new room without owner, and throw exceptions")
  void itCreateRoomWithoutOwnerShouldThrowExceptions() {
    Mockito.when(server.getRoomManager()).thenReturn(roomManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var setting = Mockito.mock(InitialRoomSetting.class);
    Mockito.doThrow(new CreatedRoomException("failed", RoomCreatedResult.REACHED_MAX_ROOMS))
        .when(roomManager).createRoom(setting);
    serverApi.createRoom(setting);
    Mockito.verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, Optional.empty(), setting,
        RoomCreatedResult.REACHED_MAX_ROOMS);
  }

  @Test
  @DisplayName("When it created successfully a new room with an owner")
  void itCreateRoomWithOwnerShouldBeSuccessful() {
    Mockito.when(server.getRoomManager()).thenReturn(roomManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var setting = Mockito.mock(InitialRoomSetting.class);
    var createdRoom = Mockito.mock(Room.class);
    var roomOwner = Mockito.mock(Player.class);
    Mockito.when(roomManager.createRoomWithOwner(setting, roomOwner)).thenReturn(createdRoom);
    serverApi.createRoom(setting, roomOwner);
    Mockito.verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, Optional.ofNullable(createdRoom), setting,
        RoomCreatedResult.SUCCESS);
  }

  @Test
  @DisplayName("When it failed to create a new room with an owner, and throw invalid credentials " +
      "exception")
  void itCreateRoomWithOwnerShouldThrowInvalidCredentialsException() {
    Mockito.when(server.getRoomManager()).thenReturn(roomManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var setting = Mockito.mock(InitialRoomSetting.class);
    var roomOwner = Mockito.mock(Player.class);
    Mockito.doThrow(IllegalArgumentException.class).when(roomManager).createRoomWithOwner(setting
        , roomOwner);
    serverApi.createRoom(setting, roomOwner);
    Mockito.verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, Optional.empty(), setting,
        RoomCreatedResult.INVALID_NAME_OR_PASSWORD);
  }

  @Test
  @DisplayName("When it failed to create a new room with an owner, and throw exceptions")
  void itCreateRoomWithOwnerShouldThrowExceptions() {
    Mockito.when(server.getRoomManager()).thenReturn(roomManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var setting = Mockito.mock(InitialRoomSetting.class);
    var roomOwner = Mockito.mock(Player.class);
    Mockito.doThrow(new CreatedRoomException("failed", RoomCreatedResult.REACHED_MAX_ROOMS))
        .when(roomManager).createRoomWithOwner(setting, roomOwner);
    serverApi.createRoom(setting, roomOwner);
    Mockito.verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, Optional.empty(), setting,
        RoomCreatedResult.REACHED_MAX_ROOMS);
  }

  @Test
  @DisplayName("All getter methods should give expected results")
  void getterMethodsShouldReturnExpectedResults() {
    Mockito.when(server.getPlayerManager()).thenReturn(playerManager);
    Mockito.when(server.getRoomManager()).thenReturn(roomManager);
    Mockito.when(server.getUdpChannelManager()).thenReturn(udpChannelManager);

    var playerName = "test";
    var player = Mockito.mock(Player.class);
    var session = Mockito.mock(Session.class);
    var playerIterator = Mockito.mock(Iterator.class);
    var playerList = Mockito.mock(List.class);
    var room = Mockito.mock(Room.class);
    var roomIterator = Mockito.mock(Iterator.class);
    var roomList = Mockito.mock(List.class);

    // getPlayerByName(String playerName)
    Mockito.when(playerManager.getPlayerByName(playerName)).thenReturn(player);
    Assertions.assertEquals(Optional.ofNullable(player), serverApi.getPlayerByName(playerName));

    // getPlayerCount()
    Mockito.when(playerManager.getPlayerCount()).thenReturn(10);
    Assertions.assertEquals(10, serverApi.getPlayerCount());

    // getPlayerIterator()
    Mockito.when(playerManager.getPlayerIterator()).thenReturn(playerIterator);
    Assertions.assertEquals(playerIterator, serverApi.getPlayerIterator());

    // getReadonlyPlayersList()
    Mockito.when(playerManager.getReadonlyPlayersList()).thenReturn(playerList);
    Assertions.assertEquals(playerList, serverApi.getReadonlyPlayersList());

    // getRoomById(long roomId)
    Mockito.when(roomManager.getRoomById(10L)).thenReturn(room);
    Assertions.assertEquals(Optional.ofNullable(room), serverApi.getRoomById(10L));

    // getRoomIterator()
    Mockito.when(roomManager.getRoomIterator()).thenReturn(roomIterator);
    Assertions.assertEquals(roomIterator, serverApi.getRoomIterator());

    // getReadonlyRoomsList()
    Mockito.when(roomManager.getReadonlyRoomsList()).thenReturn(roomList);
    Assertions.assertEquals(roomList, serverApi.getReadonlyRoomsList());

    // getCurrentAvailableUdpPort()
    Mockito.when(udpChannelManager.getCurrentAvailableUdpPort()).thenReturn(8048);
    Assertions.assertEquals(8048, serverApi.getCurrentAvailableUdpPort());

    // getStartedTime
    Mockito.when(server.getStartedTime()).thenReturn(100000L);
    Assertions.assertEquals(100000L, serverApi.getStartedTime());

    // getUptime()
    Mockito.when(server.getUptime()).thenReturn(10000L);
    Assertions.assertEquals(10000L, serverApi.getUptime());
  }

  @Test
  @DisplayName("When it tries to join an available player to nonexistent room, it should be " +
      "failed")
  void itJoinNullPlayerOrNullRoomShouldBeFailed() {
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var room = Mockito.mock(Room.class);
    var player = Mockito.mock(Player.class);
    serverApi.joinRoom(null, room, "test", 1, true);
    serverApi.joinRoom(player, null, "test", 0, false);
    serverApi.joinRoom(null, null, "test", 1, true);
    serverApi.joinRoom(null, room);
    serverApi.joinRoom(player, null);
    serverApi.joinRoom(null, null);

    Mockito.verify(eventManager, Mockito.times(2)).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT,
        null, room, PlayerJoinedRoomResult.PLAYER_OR_ROOM_UNAVAILABLE);
    Mockito.verify(eventManager, Mockito.times(2)).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT,
        player, null, PlayerJoinedRoomResult.PLAYER_OR_ROOM_UNAVAILABLE);
    Mockito.verify(eventManager, Mockito.times(2)).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT,
        null, null, PlayerJoinedRoomResult.PLAYER_OR_ROOM_UNAVAILABLE);
  }

  @Test
  @DisplayName("When a player is already in a room, it cannot join another")
  void itCannotJoinInRoomPlayerToAnotherRoom() {
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var player = Mockito.mock(Player.class);
    var room = Mockito.mock(Room.class);
    Mockito.when(player.isInRoom()).thenReturn(true);
    serverApi.joinRoom(player, room, "test", 1, false);
    serverApi.joinRoom(player, room);

    Mockito.verify(eventManager, Mockito.times(2)).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT,
        player, room, PlayerJoinedRoomResult.PLAYER_IS_IN_ANOTHER_ROOM);
  }

  @Test
  @DisplayName("When a player can join a room, it should be successful")
  void whenPlayerJoinRoomSuccessfully() {
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var player = Mockito.mock(Player.class);
    var room = Mockito.mock(Room.class);
    serverApi.joinRoom(player, room, "test", 1, false);
    serverApi.joinRoom(player, room);

    Mockito.verify(eventManager, Mockito.times(2)).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT,
        player, room, PlayerJoinedRoomResult.SUCCESS);
  }

  @Test
  @DisplayName("When a player failed to join room, it should throw exceptions")
  void whenPlayerFailedToJoinRoomWithAnyReason() {
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var player = Mockito.mock(Player.class);
    var room = Mockito.mock(Room.class);
    Mockito.doThrow(new PlayerJoinedRoomException(
        "failed", PlayerJoinedRoomResult.ROOM_IS_FULL)).when(room).addPlayer(player, "test", false
        , 1);
    serverApi.joinRoom(player, room, "test", 1, false);

    Mockito.verify(eventManager, Mockito.times(1)).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT,
        player, room, PlayerJoinedRoomResult.ROOM_IS_FULL);
  }

  @Test
  @DisplayName("When a player was added twice to a room, it should throw exception")
  void whenPlayerWasAddedTwiceToRoom() {
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var player = Mockito.mock(Player.class);
    var room = Mockito.mock(Room.class);
    Mockito.doThrow(new AddedDuplicatedPlayerException(player, room)).when(room).addPlayer(player
        , "test", false, 1);
    serverApi.joinRoom(player, room, "test", 1, false);

    Mockito.verify(eventManager, Mockito.times(1)).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT,
        player, room, PlayerJoinedRoomResult.DUPLICATED_PLAYER);
  }

  @Test
  @DisplayName("When a player is asked to leave from a room, but he is not in any room")
  void whenItRemovePlayerFromRoomWhoIsNotInAnyRoom() {
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var player = Mockito.mock(Player.class);
    Mockito.when(player.isInRoom()).thenReturn(false);
    serverApi.leaveRoom(player, PlayerLeaveRoomMode.LOG_OUT);

    Mockito.verify(eventManager, Mockito.times(1))
        .emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, Optional.empty(),
            PlayerLeaveRoomMode.LOG_OUT, PlayerLeftRoomResult.PLAYER_ALREADY_LEFT_ROOM);
  }

  @Test
  @DisplayName("When it tries to remove a null room from from server")
  void itRemoveNullRoomFromServerShouldDoNothing() {
    serverApi.removeRoom(null, RoomRemoveMode.WHEN_EMPTY);
    Mockito.verifyNoMoreInteractions(roomManager, eventManager);
  }

  @Test
  @DisplayName("When it tries to remove a room from from server")
  void itRemoveRoomFromServer() {
    Mockito.when(server.getRoomManager()).thenReturn(roomManager);
    Mockito.when(server.getEventManager()).thenReturn(eventManager);

    var room = Mockito.mock(Room.class);
    Mockito.when(optionalRoom.get()).thenReturn(room);

    var playerList = new ArrayList<Player>(5);
    for (int i = 0; i < 5; i++) {
      var player = Mockito.mock(Player.class);
      optionalRoom.get().addPlayer(player);
      Mockito.when(player.isInRoom()).thenReturn(true);
      Mockito.when(player.getCurrentRoom()).thenReturn(optionalRoom);
      playerList.add(player);
    }

    Mockito.when(room.getPlayerIterator()).thenReturn(playerList.iterator());

    serverApi.removeRoom(room, RoomRemoveMode.WHEN_EMPTY);

    Mockito.verify(eventManager, Mockito.times(1)).emit(ServerEvent.ROOM_WILL_BE_REMOVED, room,
        RoomRemoveMode.WHEN_EMPTY);
    playerList.forEach(player -> {
      Mockito.verify(eventManager, Mockito.times(1))
          .emit(ServerEvent.PLAYER_BEFORE_LEAVE_ROOM, player, optionalRoom,
              PlayerLeaveRoomMode.ROOM_REMOVED);
      Mockito.verify(eventManager, Mockito.times(1))
          .emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, optionalRoom,
              PlayerLeaveRoomMode.ROOM_REMOVED, PlayerLeftRoomResult.SUCCESS);
    });
  }

  @Test
  @DisplayName("Unsupported methods should not be called")
  void unsupportedMethodsShouldNotBeCalled() {
    Assertions.assertThrows(UnsupportedOperationException.class,
        () -> serverApi.kickPlayer(Mockito.mock(Player.class), "reason", 1));
    Assertions.assertThrows(UnsupportedOperationException.class,
        () -> serverApi.banPlayer(Mockito.mock(Player.class), "reason", PlayerBanMode.BY_NAME, 1,
            1));
    Assertions.assertThrows(UnsupportedOperationException.class,
        () -> serverApi.switchParticipantToSpectator(Mockito.mock(Player.class),
            Mockito.mock(Room.class)));
    Assertions.assertThrows(UnsupportedOperationException.class,
        () -> serverApi.switchSpectatorToParticipant(Mockito.mock(Player.class),
            Mockito.mock(Room.class), 1));
    Assertions.assertThrows(UnsupportedOperationException.class,
        () -> serverApi.sendPublicMessage(Mockito.mock(Player.class),
            Mockito.mock(Room.class), Mockito.mock(DataCollection.class)));
    Assertions.assertThrows(UnsupportedOperationException.class,
        () -> serverApi.sendPrivateMessage(Mockito.mock(Player.class), Mockito.mock(Player.class)
            , Mockito.mock(DataCollection.class)));
  }
}
