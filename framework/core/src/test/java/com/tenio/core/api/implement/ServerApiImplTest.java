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
import com.tenio.core.entity.define.mode.PlayerLeaveRoomMode;
import com.tenio.core.entity.define.result.PlayerJoinedRoomResult;
import com.tenio.core.entity.define.result.PlayerLeftRoomResult;
import com.tenio.core.entity.define.result.RoomCreatedResult;
import com.tenio.core.entity.manager.ChannelManager;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.CreatedRoomException;
import com.tenio.core.network.zero.engine.manager.DatagramChannelManager;
import com.tenio.core.server.Server;
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

  private PlayerManager playerManager;
  private RoomManager roomManager;
  private ChannelManager channelManager;
  private EventManager eventManager;
  private ServerApi api;

  @BeforeEach
  void setUp() {
    Server server = mock(Server.class);
    playerManager = mock(PlayerManager.class);
    roomManager = mock(RoomManager.class);
    channelManager = mock(ChannelManager.class);
    DatagramChannelManager datagramChannelManager = mock(DatagramChannelManager.class);
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
}
