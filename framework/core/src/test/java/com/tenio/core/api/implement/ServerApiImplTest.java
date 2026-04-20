/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.common.data.DataCollection;
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
import com.tenio.core.network.zero.engine.manager.DatagramChannelManager;
import com.tenio.core.server.Server;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ServerApiImpl (extended)")
class ServerApiImplTest {

  private Server server;
  private PlayerManager playerManager;
  private RoomManager roomManager;
  private ChannelManager channelManager;
  private DatagramChannelManager datagramChannelManager;
  private EventManager eventManager;
  private ServerApi api;

  @BeforeEach
  void setUp() {
    server = mock(Server.class);
    playerManager = mock(PlayerManager.class);
    roomManager = mock(RoomManager.class);
    channelManager = mock(ChannelManager.class);
    datagramChannelManager = mock(DatagramChannelManager.class);
    eventManager = mock(EventManager.class);
    when(server.getPlayerManager()).thenReturn(playerManager);
    when(server.getRoomManager()).thenReturn(roomManager);
    when(server.getChannelManager()).thenReturn(channelManager);
    when(server.getDatagramChannelManager()).thenReturn(datagramChannelManager);
    when(server.getEventManager()).thenReturn(eventManager);
    api = ServerApiImpl.newInstance(server);
  }

  // --- addRoom ---

  @Test
  @DisplayName("addRoom success emits ROOM_CREATED_RESULT with SUCCESS")
  void testAddRoomSuccess() {
    var setting = mock(InitialRoomSetting.class);
    var room = mock(Room.class);
    var owner = mock(Player.class);
    api.addRoom(room, setting, owner);
    verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, room, setting,
        RoomCreatedResult.SUCCESS);
  }

  @Test
  @DisplayName("addRoom with invalid name emits ROOM_CREATED_RESULT with INVALID_NAME_OR_PASSWORD")
  void testAddRoomInvalidName() {
    var setting = mock(InitialRoomSetting.class);
    var room = mock(Room.class);
    var owner = mock(Player.class);
    doThrow(IllegalArgumentException.class).when(roomManager).addRoomWithOwner(room, setting,
        owner);
    api.addRoom(room, setting, owner);
    verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, null, setting,
        RoomCreatedResult.INVALID_NAME_OR_PASSWORD);
  }

  @Test
  @DisplayName("addRoom with CreatedRoomException emits ROOM_CREATED_RESULT with exception result")
  void testAddRoomCreatedRoomException() {
    var setting = mock(InitialRoomSetting.class);
    var room = mock(Room.class);
    var owner = mock(Player.class);
    doThrow(new CreatedRoomException("fail", RoomCreatedResult.REACHED_MAX_ROOMS))
        .when(roomManager).addRoomWithOwner(room, setting, owner);
    api.addRoom(room, setting, owner);
    verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, null, setting,
        RoomCreatedResult.REACHED_MAX_ROOMS);
  }

  // --- computePlayers ---

  @Test
  @DisplayName("computePlayers delegates to playerManager")
  @SuppressWarnings("unchecked")
  void testComputePlayers() {
    Consumer<Iterator<Player>> consumer = mock(Consumer.class);
    api.computePlayers(consumer);
    verify(playerManager).computePlayers(consumer);
  }

  // --- computeRooms ---

  @Test
  @DisplayName("computeRooms delegates to roomManager")
  @SuppressWarnings("unchecked")
  void testComputeRooms() {
    Consumer<Iterator<Room>> consumer = mock(Consumer.class);
    api.computeRooms(consumer);
    verify(roomManager).computeRooms(consumer);
  }

  // --- getRoomCount ---

  @Test
  @DisplayName("getRoomCount delegates to roomManager")
  void testGetRoomCount() {
    when(roomManager.getRoomCount()).thenReturn(7);
    assertEquals(7, api.getRoomCount());
  }

  // --- getPlayerByIdentity when not found ---

  @Test
  @DisplayName("getPlayerByIdentity returns empty Optional when player not found")
  void testGetPlayerByIdentityNotFound() {
    when(playerManager.getPlayerByIdentity("ghost")).thenReturn(null);
    assertTrue(api.getPlayerByIdentity("ghost").isEmpty());
  }

  // --- channel operations ---

  @Test
  @DisplayName("createChannel delegates to channelManager")
  void testCreateChannel() {
    api.createChannel("ch1", "first channel");
    verify(channelManager).createChannel("ch1", "first channel");
  }

  @Test
  @DisplayName("removeChannel delegates to channelManager")
  void testRemoveChannel() {
    api.removeChannel("ch1");
    verify(channelManager).removeChannel("ch1");
  }

  @Test
  @DisplayName("subscribeToChannel delegates to channelManager")
  void testSubscribeToChannel() {
    var channel = mock(Channel.class);
    var player = mock(Player.class);
    api.subscribeToChannel(channel, player);
    verify(channelManager).subscribe(channel, player);
  }

  @Test
  @DisplayName("unsubscribeFromChannel delegates to channelManager")
  void testUnsubscribeFromChannel() {
    var channel = mock(Channel.class);
    var player = mock(Player.class);
    api.unsubscribeFromChannel(channel, player);
    verify(channelManager).unsubscribe(channel, player);
  }

  @Test
  @DisplayName("unsubscribeFromAllChannels delegates to channelManager")
  void testUnsubscribeFromAllChannels() {
    var player = mock(Player.class);
    api.unsubscribeFromAllChannels(player);
    verify(channelManager).unsubscribe(player);
  }

  @Test
  @DisplayName("broadcastToChannel delegates to channelManager")
  void testBroadcastToChannel() {
    var channel = mock(Channel.class);
    var message = mock(DataCollection.class);
    api.broadcastToChannel(channel, message);
    verify(channelManager).broadcast(channel, message);
  }

  @Test
  @DisplayName("getSubscribedChannelsForPlayer delegates to channelManager")
  void testGetSubscribedChannelsForPlayer() {
    var player = mock(Player.class);
    @SuppressWarnings("unchecked")
    Map<String, Channel> expected = mock(Map.class);
    when(channelManager.getSubscribedChannelsForPlayer(player)).thenReturn(expected);
    assertEquals(expected, api.getSubscribedChannelsForPlayer(player));
  }

  // --- changeRoom ---

  @Test
  @DisplayName("changeRoom when player is in a room first leaves then joins new room")
  void testChangeRoomPlayerInRoomLeavesFirst() {
    var player = mock(Player.class);
    var newRoom = mock(Room.class);
    var currentRoom = mock(Room.class);

    when(player.isInRoom()).thenReturn(true, true, false);
    when(player.getCurrentRoom()).thenReturn(Optional.of(currentRoom));

    api.changeRoom(player, newRoom, "", 0, false);

    verify(eventManager).emit(ServerEvent.PLAYER_BEFORE_LEAVE_ROOM, player, currentRoom,
            PlayerLeaveRoomMode.CHANGE_ROOM);

    verify(eventManager).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, newRoom,
            PlayerJoinedRoomResult.SUCCESS);
  }

  @Test
  @DisplayName("changeRoom when player is not in a room just joins new room")
  void testChangeRoomPlayerNotInRoomJoinsDirectly() {
    var player = mock(Player.class);
    var newRoom = mock(Room.class);
    when(player.isInRoom()).thenReturn(false);

    api.changeRoom(player, newRoom, "", 0, false);

    verify(eventManager).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, newRoom,
        PlayerJoinedRoomResult.SUCCESS);
  }

  @Test
  @DisplayName("changeRoom with null player emits PLAYER_OR_ROOM_UNAVAILABLE")
  void testChangeRoomNullPlayerEmitsUnavailable() {
    var newRoom = mock(Room.class);
    api.changeRoom(null, newRoom, "", 0, false);
    verify(eventManager).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, null, newRoom,
        PlayerJoinedRoomResult.PLAYER_OR_ROOM_UNAVAILABLE);
  }

  // --- leaveRoom ---

  @Test
  @DisplayName("leaveRoom when player is in a room emits PLAYER_AFTER_LEFT_ROOM with SUCCESS")
  void testLeaveRoomSuccess() {
    var player = mock(Player.class);
    var room = mock(Room.class);
    when(player.isInRoom()).thenReturn(true);
    when(player.getCurrentRoom()).thenReturn(Optional.of(room));

    api.leaveRoom(player, PlayerLeaveRoomMode.LOG_OUT);

    verify(eventManager).emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, room,
        PlayerLeaveRoomMode.LOG_OUT, PlayerLeftRoomResult.SUCCESS);
  }

  // --- getReadonlyRoomsList ---

  @Test
  @DisplayName("getReadonlyRoomsList delegates to roomManager")
  void testGetReadonlyRoomsList() {
    List<Room> rooms = Collections.emptyList();
    when(roomManager.getReadonlyRoomsList()).thenReturn(rooms);
    assertEquals(rooms, api.getReadonlyRoomsList());
  }

  // --- logout ---

  @Test
  @DisplayName("logout calls session.close() even when session is not activated")
  void testLogoutCallsCloseOnUnactivatedSession() throws IOException {
    var player = mock(Player.class);
    var session = mock(Session.class);
    when(player.containsSession()).thenReturn(true);
    when(player.getSession()).thenReturn(Optional.of(session));
    when(session.isActivated()).thenReturn(false);

    api.logout(player, ConnectionDisconnectMode.CLIENT_REQUEST,
        PlayerDisconnectMode.CLIENT_REQUEST);

    verify(session).close(ConnectionDisconnectMode.CLIENT_REQUEST,
        PlayerDisconnectMode.CLIENT_REQUEST);
  }

  @Test
  @DisplayName("logout calls session.close() when session is activated")
  void testLogoutCallsCloseOnActivatedSession() throws IOException {
    var player = mock(Player.class);
    var session = mock(Session.class);
    when(player.containsSession()).thenReturn(true);
    when(player.getSession()).thenReturn(Optional.of(session));
    when(session.isActivated()).thenReturn(true);

    api.logout(player, ConnectionDisconnectMode.CLIENT_REQUEST,
        PlayerDisconnectMode.CLIENT_REQUEST);

    verify(session).close(ConnectionDisconnectMode.CLIENT_REQUEST,
        PlayerDisconnectMode.CLIENT_REQUEST);
  }

  @Test
  @DisplayName("logout with null player returns immediately without side effects")
  void testLogoutNullPlayerReturnsImmediately() {
    api.logout(null, ConnectionDisconnectMode.CLIENT_REQUEST, PlayerDisconnectMode.CLIENT_REQUEST);
    // no exception, no event emitted
  }

  @Test
  @DisplayName("logout with no-session player emits DISCONNECT_PLAYER and removes player")
  void testLogoutWithNoSessionDisconnectsPlayer() {
    var player = mock(Player.class);
    when(player.containsSession()).thenReturn(false);
    when(player.isInRoom()).thenReturn(false);
    when(player.getIdentity()).thenReturn("alice");

    api.logout(player, ConnectionDisconnectMode.CLIENT_REQUEST,
        PlayerDisconnectMode.CLIENT_REQUEST);

    verify(eventManager).emit(ServerEvent.DISCONNECT_PLAYER, player,
        PlayerDisconnectMode.CLIENT_REQUEST);
    verify(playerManager).removePlayerByIdentity("alice");
    verify(player).clean();
  }

  // --- login ---

  @Test
  @DisplayName("login(String) creates player and emits PLAYER_LOGIN")
  void testLoginWithPlayerName() {
    var player = mock(Player.class);
    when(playerManager.createPlayer("alice")).thenReturn(player);
    api.login("alice");
    verify(playerManager).createPlayer("alice");
    verify(eventManager).emit(ServerEvent.PLAYER_LOGIN, player);
  }

  @Test
  @DisplayName("login(String, Session) creates player with session and emits PLAYER_LOGIN")
  void testLoginWithPlayerNameAndSession() {
    var player = mock(Player.class);
    var session = mock(Session.class);
    when(playerManager.createPlayerWithSession("alice", session)).thenReturn(player);
    api.login("alice", session);
    verify(playerManager).createPlayerWithSession("alice", session);
    verify(eventManager).emit(ServerEvent.PLAYER_LOGIN, player);
  }

  @Test
  @DisplayName("login(Player) adds player and emits PLAYER_LOGIN")
  void testLoginWithPlayer() {
    var player = mock(Player.class);
    api.login(player);
    verify(playerManager).addPlayer(player);
    verify(eventManager).emit(ServerEvent.PLAYER_LOGIN, player);
  }

  // --- createRoom ---

  @Test
  @DisplayName("createRoom with null owner creates room and emits SUCCESS")
  void testCreateRoomWithNullOwner() {
    var setting = mock(InitialRoomSetting.class);
    var room = mock(Room.class);
    when(roomManager.createRoom(setting)).thenReturn(room);
    Room result = api.createRoom(setting, null);
    assertEquals(room, result);
    verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, room, setting,
        RoomCreatedResult.SUCCESS);
  }

  @Test
  @DisplayName("createRoom with owner creates room and emits SUCCESS")
  void testCreateRoomWithOwner() {
    var setting = mock(InitialRoomSetting.class);
    var owner = mock(Player.class);
    var room = mock(Room.class);
    when(roomManager.createRoomWithOwner(setting, owner)).thenReturn(room);
    Room result = api.createRoom(setting, owner);
    assertEquals(room, result);
    verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, room, setting,
        RoomCreatedResult.SUCCESS);
  }

  @Test
  @DisplayName("createRoom with IllegalArgumentException emits INVALID_NAME_OR_PASSWORD")
  void testCreateRoomIllegalArgumentException() {
    var setting = mock(InitialRoomSetting.class);
    doThrow(IllegalArgumentException.class).when(roomManager).createRoom(setting);
    Room result = api.createRoom(setting, null);
    assertNull(result);
    verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, null, setting,
        RoomCreatedResult.INVALID_NAME_OR_PASSWORD);
  }

  @Test
  @DisplayName("createRoom with CreatedRoomException emits the exception result")
  void testCreateRoomCreatedRoomException() {
    var setting = mock(InitialRoomSetting.class);
    doThrow(new CreatedRoomException("fail", RoomCreatedResult.REACHED_MAX_ROOMS))
        .when(roomManager).createRoom(setting);
    Room result = api.createRoom(setting, null);
    assertNull(result);
    verify(eventManager).emit(ServerEvent.ROOM_CREATED_RESULT, null, setting,
        RoomCreatedResult.REACHED_MAX_ROOMS);
  }

  // --- joinRoom ---

  @Test
  @DisplayName("joinRoom with null player emits PLAYER_OR_ROOM_UNAVAILABLE")
  void testJoinRoomNullPlayer() {
    var room = mock(Room.class);
    api.joinRoom(null, room, "", 0, false);
    verify(eventManager).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, null, room,
        PlayerJoinedRoomResult.PLAYER_OR_ROOM_UNAVAILABLE);
  }

  @Test
  @DisplayName("joinRoom with null room emits PLAYER_OR_ROOM_UNAVAILABLE")
  void testJoinRoomNullRoom() {
    var player = mock(Player.class);
    api.joinRoom(player, null, "", 0, false);
    verify(eventManager).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, null,
        PlayerJoinedRoomResult.PLAYER_OR_ROOM_UNAVAILABLE);
  }

  @Test
  @DisplayName("joinRoom when player is already in another room emits PLAYER_IS_IN_ANOTHER_ROOM")
  void testJoinRoomPlayerAlreadyInRoom() {
    var player = mock(Player.class);
    var room = mock(Room.class);
    when(player.isInRoom()).thenReturn(true);
    api.joinRoom(player, room, "", 0, false);
    verify(eventManager).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
        PlayerJoinedRoomResult.PLAYER_IS_IN_ANOTHER_ROOM);
  }

  @Test
  @DisplayName("joinRoom success adds player to room and emits SUCCESS")
  void testJoinRoomSuccess() {
    var player = mock(Player.class);
    var room = mock(Room.class);
    when(player.isInRoom()).thenReturn(false);
    api.joinRoom(player, room, "", 0, false);
    verify(room).addPlayer(player, "", false, 0);
    verify(eventManager).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
        PlayerJoinedRoomResult.SUCCESS);
  }

  @Test
  @DisplayName("joinRoom with PlayerJoinedRoomException emits the exception result")
  void testJoinRoomPlayerJoinedRoomException() {
    var player = mock(Player.class);
    var room = mock(Room.class);
    when(player.isInRoom()).thenReturn(false);
    doThrow(new PlayerJoinedRoomException("fail", PlayerJoinedRoomResult.ROOM_IS_FULL))
        .when(room).addPlayer(player, "", false, 0);
    api.joinRoom(player, room, "", 0, false);
    verify(eventManager).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
        PlayerJoinedRoomResult.ROOM_IS_FULL);
  }

  @Test
  @DisplayName("joinRoom with AddedDuplicatedPlayerException emits DUPLICATED_PLAYER")
  void testJoinRoomAddedDuplicatedPlayerException() {
    var player = mock(Player.class);
    var room = mock(Room.class);
    when(player.isInRoom()).thenReturn(false);
    when(player.getIdentity()).thenReturn("alice");
    doThrow(new AddedDuplicatedPlayerException(player))
        .when(room).addPlayer(player, "", false, 0);
    api.joinRoom(player, room, "", 0, false);
    verify(eventManager).emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
        PlayerJoinedRoomResult.DUPLICATED_PLAYER);
  }

  // --- leaveRoom edge cases ---

  @Test
  @DisplayName("leaveRoom when player is not in a room emits PLAYER_ALREADY_LEFT_ROOM")
  void testLeaveRoomPlayerNotInRoom() {
    var player = mock(Player.class);
    when(player.isInRoom()).thenReturn(false);
    api.leaveRoom(player, PlayerLeaveRoomMode.LOG_OUT);
    verify(eventManager).emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, null,
        PlayerLeaveRoomMode.LOG_OUT, PlayerLeftRoomResult.PLAYER_ALREADY_LEFT_ROOM);
  }

  @Test
  @DisplayName("leaveRoom with RemovedNonExistentPlayerException emits PLAYER_ALREADY_LEFT_ROOM")
  void testLeaveRoomRemovedNonExistentPlayerException() {
    var player = mock(Player.class);
    var room = mock(Room.class);
    when(player.isInRoom()).thenReturn(true);
    when(player.getCurrentRoom()).thenReturn(Optional.of(room));
    doThrow(RemovedNonExistentPlayerException.class).when(room).removePlayer(player);
    api.leaveRoom(player, PlayerLeaveRoomMode.LOG_OUT);
    verify(eventManager).emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, room,
        PlayerLeaveRoomMode.LOG_OUT, PlayerLeftRoomResult.PLAYER_ALREADY_LEFT_ROOM);
  }

  // --- removeRoom ---

  @Test
  @DisplayName("removeRoom with null room returns immediately without side effects")
  void testRemoveRoomWithNull() {
    api.removeRoom(null, RoomRemoveMode.WHEN_EMPTY);
    // just verify no NPE is thrown
  }

  @Test
  @DisplayName("removeRoom emits ROOM_WILL_BE_REMOVED and removes room by id")
  void testRemoveRoomSuccess() {
    var room = mock(Room.class);
    when(room.getId()).thenReturn(42L);
    api.removeRoom(room, RoomRemoveMode.WHEN_EMPTY);
    verify(eventManager).emit(ServerEvent.ROOM_WILL_BE_REMOVED, room, RoomRemoveMode.WHEN_EMPTY);
    verify(roomManager).removeRoomById(42L);
  }

  // --- additional simple delegates ---

  @Test
  @DisplayName("getPlayerCount delegates to playerManager")
  void testGetPlayerCount() {
    when(playerManager.getPlayerCount()).thenReturn(5);
    assertEquals(5, api.getPlayerCount());
  }

  @Test
  @DisplayName("getReadonlyPlayersList delegates to playerManager")
  void testGetReadonlyPlayersList() {
    List<Player> list = Collections.emptyList();
    when(playerManager.getReadonlyPlayersList()).thenReturn(list);
    assertEquals(list, api.getReadonlyPlayersList());
  }

  @Test
  @DisplayName("getRoomById returns non-empty Optional when room is found")
  void testGetRoomByIdFound() {
    var room = mock(Room.class);
    when(roomManager.getRoomById(1L)).thenReturn(room);
    assertTrue(api.getRoomById(1L).isPresent());
    assertEquals(room, api.getRoomById(1L).get());
  }

  @Test
  @DisplayName("getRoomById returns empty Optional when room is not found")
  void testGetRoomByIdNotFound() {
    when(roomManager.getRoomById(99L)).thenReturn(null);
    assertTrue(api.getRoomById(99L).isEmpty());
  }

  @Test
  @DisplayName("getPlayerByIdentity returns non-empty Optional when player is found")
  void testGetPlayerByIdentityFound() {
    var player = mock(Player.class);
    when(playerManager.getPlayerByIdentity("alice")).thenReturn(player);
    assertTrue(api.getPlayerByIdentity("alice").isPresent());
    assertEquals(player, api.getPlayerByIdentity("alice").get());
  }

  @Test
  @DisplayName("getUdpPort delegates to datagramChannelManager")
  void testGetUdpPort() {
    when(datagramChannelManager.getUdpPort()).thenReturn(9090);
    assertEquals(9090, api.getUdpPort());
  }

  @Test
  @DisplayName("getStartedTime delegates to server")
  void testGetStartedTime() {
    when(server.getStartedTime()).thenReturn(12345L);
    assertEquals(12345L, api.getStartedTime());
  }

  @Test
  @DisplayName("getUptime delegates to server")
  void testGetUptime() {
    when(server.getUptime()).thenReturn(99L);
    assertEquals(99L, api.getUptime());
  }

  @Test
  @DisplayName("logout catches IOException from session.close without propagating")
  void testLogoutHandlesIOExceptionFromSessionClose() throws IOException {
    Player player = mock(Player.class);
    Session session = mock(Session.class);
    when(player.containsSession()).thenReturn(true);
    when(player.getSession()).thenReturn(Optional.of(session));
    doThrow(new IOException("close failed")).when(session).close(
        org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    org.junit.jupiter.api.Assertions.assertDoesNotThrow(
        () -> api.logout(player, ConnectionDisconnectMode.CLIENT_REQUEST,
            PlayerDisconnectMode.CLIENT_REQUEST));
  }

  @Test
  @DisplayName("logout without session emits DISCONNECT_PLAYER and removes player")
  void testLogoutWithoutSessionEmitsDisconnectAndRemovesPlayer() {
    Player player = mock(Player.class);
    when(player.containsSession()).thenReturn(false);
    when(player.getSession()).thenReturn(Optional.empty());
    when(player.isInRoom()).thenReturn(false);
    when(player.getIdentity()).thenReturn("test-player");
    api.logout(player, ConnectionDisconnectMode.CLIENT_REQUEST, PlayerDisconnectMode.CLIENT_REQUEST);
    verify(eventManager).emit(ServerEvent.DISCONNECT_PLAYER, player, PlayerDisconnectMode.CLIENT_REQUEST);
    verify(playerManager).removePlayerByIdentity("test-player");
  }
}
