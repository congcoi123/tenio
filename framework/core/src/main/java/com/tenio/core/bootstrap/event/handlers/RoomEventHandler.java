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

package com.tenio.core.bootstrap.event.handlers;

import com.tenio.common.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.common.bootstrap.annotation.Component;
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
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.extension.events.EventPlayerAfterLeftRoom;
import com.tenio.core.extension.events.EventPlayerBeforeLeaveRoom;
import com.tenio.core.extension.events.EventPlayerJoinedRoomResult;
import com.tenio.core.extension.events.EventRoomCreatedResult;
import com.tenio.core.extension.events.EventRoomWillBeRemoved;
import com.tenio.core.extension.events.EventSwitchParticipantToSpectatorResult;
import com.tenio.core.extension.events.EventSwitchSpectatorToParticipantResult;
import java.util.Optional;

/**
 * Dispatching all events related to rooms.
 */
@Component
public final class RoomEventHandler {

  @AutowiredAcceptNull
  private EventPlayerAfterLeftRoom eventPlayerAfterLeftRoom;

  @AutowiredAcceptNull
  private EventPlayerBeforeLeaveRoom eventPlayerBeforeLeaveRoom;

  @AutowiredAcceptNull
  private EventPlayerJoinedRoomResult eventPlayerJoinedRoomResult;

  @AutowiredAcceptNull
  private EventRoomCreatedResult eventRoomCreatedResult;

  @AutowiredAcceptNull
  private EventRoomWillBeRemoved eventRoomWillBeRemoved;

  @AutowiredAcceptNull
  private EventSwitchParticipantToSpectatorResult eventSwitchParticipantToSpectatorResult;

  @AutowiredAcceptNull
  private EventSwitchSpectatorToParticipantResult eventSwitchSpectatorToParticipantResult;

  /**
   * Initialization.
   *
   * @param eventManager the event manager
   */
  public void initialize(EventManager eventManager) {

    final var eventPlayerAfterLeftRoomOp =
        Optional.ofNullable(eventPlayerAfterLeftRoom);
    final var eventPlayerBeforeLeaveRoomOp =
        Optional.ofNullable(eventPlayerBeforeLeaveRoom);
    final var eventPlayerJoinedRoomResultOp =
        Optional.ofNullable(eventPlayerJoinedRoomResult);

    final var eventRoomCreatedResultOp =
        Optional.ofNullable(eventRoomCreatedResult);
    final var eventRoomWillBeRemovedOp =
        Optional.ofNullable(eventRoomWillBeRemoved);

    final var eventSwitchParticipantToSpectatorResultOp =
        Optional.ofNullable(eventSwitchParticipantToSpectatorResult);
    final var eventSwitchSpectatorToParticipantResultOp =
        Optional.ofNullable(eventSwitchSpectatorToParticipantResult);

    eventPlayerAfterLeftRoomOp.ifPresent(
        event -> eventManager.on(ServerEvent.PLAYER_AFTER_LEFT_ROOM, params -> {
          var player = (Player) params[0];
          var room = (Room) params[1];
          var result = (PlayerLeftRoomResult) (params[2]);

          event.handle(player, room, result);

          return null;
        }));

    eventPlayerBeforeLeaveRoomOp.ifPresent(
        event -> eventManager.on(ServerEvent.PLAYER_BEFORE_LEAVE_ROOM, params -> {
          var player = (Player) params[0];
          var room = (Room) params[1];
          var mode = (PlayerLeaveRoomMode) (params[2]);

          event.handle(player, room, mode);

          return null;
        }));

    eventPlayerJoinedRoomResultOp.ifPresent(
        event -> eventManager.on(ServerEvent.PLAYER_JOINED_ROOM_RESULT, params -> {
          var player = (Player) params[0];
          var room = (Room) params[1];
          var result = (PlayerJoinedRoomResult) params[2];

          event.handle(player, room, result);

          return null;
        }));

    eventRoomCreatedResultOp.ifPresent(event -> eventManager.on(ServerEvent.ROOM_CREATED_RESULT,
        params -> {
          var room = (Room) params[0];
          var setting = (InitialRoomSetting) params[1];
          var result = (RoomCreatedResult) params[2];

          event.handle(room, setting, result);

          return null;
        }));

    eventRoomWillBeRemovedOp.ifPresent(
        event -> eventManager.on(ServerEvent.ROOM_WILL_BE_REMOVED, params -> {
          var room = (Room) params[0];
          var mode = (RoomRemoveMode) params[1];

          event.handle(room, mode);

          return null;
        }));

    eventSwitchParticipantToSpectatorResultOp.ifPresent(
        event -> eventManager.on(ServerEvent.SWITCH_PARTICIPANT_TO_SPECTATOR, params -> {
          var player = (Player) params[0];
          var room = (Room) params[1];
          var result = (SwitchedPlayerRoleInRoomResult) params[2];

          event.handle(player, room, result);

          return null;
        }));

    eventSwitchSpectatorToParticipantResultOp.ifPresent(
        event -> eventManager.on(ServerEvent.SWITCH_SPECTATOR_TO_PARTICIPANT, params -> {
          var player = (Player) params[0];
          var room = (Room) params[1];
          var result = (SwitchedPlayerRoleInRoomResult) params[2];

          event.handle(player, room, result);

          return null;
        }));
  }
}
