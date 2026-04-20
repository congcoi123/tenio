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

package com.tenio.core.bootstrap.event.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.mode.PlayerLeaveRoomMode;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.define.result.PlayerJoinedRoomResult;
import com.tenio.core.entity.define.result.PlayerLeftRoomResult;
import com.tenio.core.entity.define.result.RoomCreatedResult;
import com.tenio.core.entity.define.result.SwitchedPlayerRoleInRoomResult;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.event.handler.implement.RoomEventHandler;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.handler.event.EventPlayerAfterLeftRoom;
import com.tenio.core.handler.event.EventPlayerBeforeLeaveRoom;
import com.tenio.core.handler.event.EventPlayerJoinedRoomResult;
import com.tenio.core.handler.event.EventRoomCreatedResult;
import com.tenio.core.handler.event.EventRoomWillBeRemoved;
import com.tenio.core.handler.event.EventSwitchParticipantToSpectatorResult;
import com.tenio.core.handler.event.EventSwitchSpectatorToParticipantResult;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For RoomEventHandler")
class RoomEventHandlerTest {

  @Test
  @DisplayName("Test initialize with all null event listeners does not throw")
  void testInitialize() {
    RoomEventHandler roomEventHandler = new RoomEventHandler();
    roomEventHandler.initialize(EventManager.newInstance());
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test PLAYER_AFTER_LEFT_ROOM event dispatches to listener")
  void testPlayerAfterLeftRoomEvent() throws Exception {
    RoomEventHandler handler = new RoomEventHandler();
    EventManager em = EventManager.newInstance();
    EventPlayerAfterLeftRoom<Player, Room> mockEvent = mock(EventPlayerAfterLeftRoom.class);
    Field field = RoomEventHandler.class.getDeclaredField("eventPlayerAfterLeftRoom");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Player player = mock(Player.class);
    Room room = mock(Room.class);
    em.emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, room,
        PlayerLeaveRoomMode.LOG_OUT, PlayerLeftRoomResult.SUCCESS);
    verify(mockEvent).onPlayerAfterLeftRoom(player, room,
        PlayerLeaveRoomMode.LOG_OUT, PlayerLeftRoomResult.SUCCESS);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test PLAYER_AFTER_LEFT_ROOM event with null room")
  void testPlayerAfterLeftRoomEventWithNullRoom() throws Exception {
    RoomEventHandler handler = new RoomEventHandler();
    EventManager em = EventManager.newInstance();
    EventPlayerAfterLeftRoom<Player, Room> mockEvent = mock(EventPlayerAfterLeftRoom.class);
    Field field = RoomEventHandler.class.getDeclaredField("eventPlayerAfterLeftRoom");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Player player = mock(Player.class);
    em.emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, null,
        PlayerLeaveRoomMode.LOG_OUT, PlayerLeftRoomResult.SUCCESS);
    verify(mockEvent).onPlayerAfterLeftRoom(player, null,
        PlayerLeaveRoomMode.LOG_OUT, PlayerLeftRoomResult.SUCCESS);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test PLAYER_BEFORE_LEAVE_ROOM event dispatches to listener")
  void testPlayerBeforeLeaveRoomEvent() throws Exception {
    RoomEventHandler handler = new RoomEventHandler();
    EventManager em = EventManager.newInstance();
    EventPlayerBeforeLeaveRoom<Player, Room> mockEvent = mock(EventPlayerBeforeLeaveRoom.class);
    Field field = RoomEventHandler.class.getDeclaredField("eventPlayerBeforeLeaveRoom");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Player player = mock(Player.class);
    Room room = mock(Room.class);
    em.emit(ServerEvent.PLAYER_BEFORE_LEAVE_ROOM, player, room, PlayerLeaveRoomMode.LOG_OUT);
    verify(mockEvent).onPlayerBeforeLeaveRoom(player, room, PlayerLeaveRoomMode.LOG_OUT);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test PLAYER_JOINED_ROOM_RESULT event dispatches to listener")
  void testPlayerJoinedRoomResultEvent() throws Exception {
    RoomEventHandler handler = new RoomEventHandler();
    EventManager em = EventManager.newInstance();
    EventPlayerJoinedRoomResult<Player, Room> mockEvent = mock(EventPlayerJoinedRoomResult.class);
    Field field = RoomEventHandler.class.getDeclaredField("eventPlayerJoinedRoomResult");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Player player = mock(Player.class);
    Room room = mock(Room.class);
    em.emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room, PlayerJoinedRoomResult.SUCCESS);
    verify(mockEvent).onPlayerJoinedRoomResult(player, room, PlayerJoinedRoomResult.SUCCESS);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test ROOM_CREATED_RESULT event dispatches to listener")
  void testRoomCreatedResultEvent() throws Exception {
    RoomEventHandler handler = new RoomEventHandler();
    EventManager em = EventManager.newInstance();
    EventRoomCreatedResult<Room> mockEvent = mock(EventRoomCreatedResult.class);
    Field field = RoomEventHandler.class.getDeclaredField("eventRoomCreatedResult");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Room room = mock(Room.class);
    InitialRoomSetting setting = mock(InitialRoomSetting.class);
    em.emit(ServerEvent.ROOM_CREATED_RESULT, room, setting, RoomCreatedResult.SUCCESS);
    verify(mockEvent).onRoomCreatedResult(room, setting, RoomCreatedResult.SUCCESS);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test ROOM_CREATED_RESULT event with null room")
  void testRoomCreatedResultEventWithNullRoom() throws Exception {
    RoomEventHandler handler = new RoomEventHandler();
    EventManager em = EventManager.newInstance();
    EventRoomCreatedResult<Room> mockEvent = mock(EventRoomCreatedResult.class);
    Field field = RoomEventHandler.class.getDeclaredField("eventRoomCreatedResult");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    InitialRoomSetting setting = mock(InitialRoomSetting.class);
    em.emit(ServerEvent.ROOM_CREATED_RESULT, null, setting, RoomCreatedResult.REACHED_MAX_ROOMS);
    verify(mockEvent).onRoomCreatedResult(null, setting, RoomCreatedResult.REACHED_MAX_ROOMS);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test ROOM_WILL_BE_REMOVED event dispatches to listener")
  void testRoomWillBeRemovedEvent() throws Exception {
    RoomEventHandler handler = new RoomEventHandler();
    EventManager em = EventManager.newInstance();
    EventRoomWillBeRemoved<Room> mockEvent = mock(EventRoomWillBeRemoved.class);
    Field field = RoomEventHandler.class.getDeclaredField("eventRoomWillBeRemoved");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Room room = mock(Room.class);
    em.emit(ServerEvent.ROOM_WILL_BE_REMOVED, room, RoomRemoveMode.WHEN_EMPTY);
    verify(mockEvent).onRoomWillBeRemoved(room, RoomRemoveMode.WHEN_EMPTY);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test SWITCH_PARTICIPANT_TO_SPECTATOR event dispatches to listener")
  void testSwitchParticipantToSpectatorEvent() throws Exception {
    RoomEventHandler handler = new RoomEventHandler();
    EventManager em = EventManager.newInstance();
    EventSwitchParticipantToSpectatorResult<Player, Room> mockEvent =
        mock(EventSwitchParticipantToSpectatorResult.class);
    Field field = RoomEventHandler.class.getDeclaredField("eventSwitchParticipantToSpectatorResult");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Player player = mock(Player.class);
    Room room = mock(Room.class);
    em.emit(ServerEvent.SWITCH_PARTICIPANT_TO_SPECTATOR, player, room,
        SwitchedPlayerRoleInRoomResult.SUCCESS);
    verify(mockEvent).onSwitchParticipantToSpectatorResult(player, room,
        SwitchedPlayerRoleInRoomResult.SUCCESS);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test SWITCH_SPECTATOR_TO_PARTICIPANT event dispatches to listener")
  void testSwitchSpectatorToParticipantEvent() throws Exception {
    RoomEventHandler handler = new RoomEventHandler();
    EventManager em = EventManager.newInstance();
    EventSwitchSpectatorToParticipantResult<Player, Room> mockEvent =
        mock(EventSwitchSpectatorToParticipantResult.class);
    Field field = RoomEventHandler.class.getDeclaredField("eventSwitchSpectatorToParticipantResult");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Player player = mock(Player.class);
    Room room = mock(Room.class);
    em.emit(ServerEvent.SWITCH_SPECTATOR_TO_PARTICIPANT, player, room,
        SwitchedPlayerRoleInRoomResult.SUCCESS);
    verify(mockEvent).onSwitchSpectatorToParticipantResult(player, room,
        SwitchedPlayerRoleInRoomResult.SUCCESS);
  }
}
