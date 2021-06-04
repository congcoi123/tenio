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
package com.tenio.core.bootstrap.event.handlers;

import java.util.Optional;
import java.util.function.Consumer;

import com.tenio.common.bootstrap.annotations.AutowiredAcceptNull;
import com.tenio.common.bootstrap.annotations.Component;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.defines.modes.PlayerLeaveRoomMode;
import com.tenio.core.entities.defines.modes.RoomRemoveMode;
import com.tenio.core.entities.defines.results.PlayerJoinedRoomResult;
import com.tenio.core.entities.defines.results.PlayerLeftRoomResult;
import com.tenio.core.entities.defines.results.RoomCreatedResult;
import com.tenio.core.entities.defines.results.SwitchedPlayerSpectatorResult;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.event.Subscriber;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.extension.events.EventPlayerAfterLeftRoom;
import com.tenio.core.extension.events.EventPlayerBeforeLeaveRoom;
import com.tenio.core.extension.events.EventPlayerJoinedRoomResult;
import com.tenio.core.extension.events.EventRoomCreatedResult;
import com.tenio.core.extension.events.EventRoomWillBeRemoved;
import com.tenio.core.extension.events.EventSwitchPlayerToSpectatorResult;
import com.tenio.core.extension.events.EventSwitchSpectatorToPlayerResult;

@Component
public final class RoomEventHandler {

	@AutowiredAcceptNull
	private EventPlayerAfterLeftRoom __eventPlayerAfterLeftRoom;

	@AutowiredAcceptNull
	private EventPlayerBeforeLeaveRoom __eventPlayerBeforeLeaveRoom;

	@AutowiredAcceptNull
	private EventPlayerJoinedRoomResult __eventPlayerJoinedRoomResult;

	@AutowiredAcceptNull
	private EventRoomCreatedResult __eventRoomCreatedResult;

	@AutowiredAcceptNull
	private EventRoomWillBeRemoved __eventRoomWillBeRemoved;

	@AutowiredAcceptNull
	private EventSwitchPlayerToSpectatorResult __eventSwitchPlayerToSpectatorResult;

	@AutowiredAcceptNull
	private EventSwitchSpectatorToPlayerResult __eventSwitchSpectatorToPlayerResult;

	public void initialize(EventManager eventManager) {

		Optional<EventPlayerAfterLeftRoom> eventPlayerAfterLeftRoomOp = Optional.ofNullable(__eventPlayerAfterLeftRoom);
		Optional<EventPlayerBeforeLeaveRoom> eventPlayerBeforeLeaveRoomOp = Optional
				.ofNullable(__eventPlayerBeforeLeaveRoom);
		Optional<EventPlayerJoinedRoomResult> eventPlayerJoinedRoomResultOp = Optional
				.ofNullable(__eventPlayerJoinedRoomResult);

		Optional<EventRoomCreatedResult> eventRoomCreatedResultOp = Optional.ofNullable(__eventRoomCreatedResult);
		Optional<EventRoomWillBeRemoved> eventRoomWillBeRemovedOp = Optional.ofNullable(__eventRoomWillBeRemoved);

		Optional<EventSwitchPlayerToSpectatorResult> eventSwitchPlayerToSpectatorResultOp = Optional
				.ofNullable(__eventSwitchPlayerToSpectatorResult);
		Optional<EventSwitchSpectatorToPlayerResult> eventSwitchSpectatorToPlayerResultOp = Optional
				.ofNullable(__eventSwitchSpectatorToPlayerResult);

		eventPlayerAfterLeftRoomOp.ifPresent(new Consumer<EventPlayerAfterLeftRoom>() {

			@Override
			public void accept(EventPlayerAfterLeftRoom event) {
				eventManager.on(ServerEvent.PLAYER_AFTER_LEFT_ROOM, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						Player player = (Player) params[0];
						Room room = (Room) params[1];
						PlayerLeftRoomResult result = (PlayerLeftRoomResult) (params[1]);

						event.handle(player, room, result);

						return null;
					}
				});
			}
		});

		eventPlayerBeforeLeaveRoomOp.ifPresent(new Consumer<EventPlayerBeforeLeaveRoom>() {

			@Override
			public void accept(EventPlayerBeforeLeaveRoom event) {
				eventManager.on(ServerEvent.PLAYER_BEFORE_LEAVE_ROOM, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						Player player = (Player) params[0];
						Room room = (Room) params[1];
						PlayerLeaveRoomMode mode = (PlayerLeaveRoomMode) (params[1]);

						event.handle(player, room, mode);

						return null;
					}
				});
			}
		});

		eventPlayerJoinedRoomResultOp.ifPresent(new Consumer<EventPlayerJoinedRoomResult>() {

			@Override
			public void accept(EventPlayerJoinedRoomResult event) {
				eventManager.on(ServerEvent.PLAYER_JOINED_ROOM_RESULT, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						Player player = (Player) params[0];
						Room room = (Room) params[1];
						PlayerJoinedRoomResult result = (PlayerJoinedRoomResult) params[2];

						event.handle(player, room, result);

						return null;
					}
				});
			}
		});

		eventRoomCreatedResultOp.ifPresent(new Consumer<EventRoomCreatedResult>() {

			@Override
			public void accept(EventRoomCreatedResult event) {
				eventManager.on(ServerEvent.ROOM_CREATED_RESULT, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						Room room = (Room) params[0];
						InitialRoomSetting setting = (InitialRoomSetting) params[1];
						RoomCreatedResult result = (RoomCreatedResult) params[2];

						event.handle(room, setting, result);

						return null;
					}
				});
			}
		});

		eventRoomWillBeRemovedOp.ifPresent(new Consumer<EventRoomWillBeRemoved>() {

			@Override
			public void accept(EventRoomWillBeRemoved event) {
				eventManager.on(ServerEvent.ROOM_WILL_BE_REMOVED, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						Room room = (Room) params[0];
						RoomRemoveMode mode = (RoomRemoveMode) params[1];

						event.handle(room, mode);

						return null;
					}
				});
			}
		});

		eventSwitchPlayerToSpectatorResultOp.ifPresent(new Consumer<EventSwitchPlayerToSpectatorResult>() {

			@Override
			public void accept(EventSwitchPlayerToSpectatorResult event) {
				eventManager.on(ServerEvent.SWITCH_PLAYER_TO_SPECTATOR, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						Player player = (Player) params[0];
						Room room = (Room) params[1];
						SwitchedPlayerSpectatorResult result = (SwitchedPlayerSpectatorResult) params[2];

						event.handle(player, room, result);

						return null;
					}
				});
			}
		});

		eventSwitchSpectatorToPlayerResultOp.ifPresent(new Consumer<EventSwitchSpectatorToPlayerResult>() {

			@Override
			public void accept(EventSwitchSpectatorToPlayerResult event) {
				eventManager.on(ServerEvent.SWITCH_SPECTATOR_TO_PLAYER, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						Player player = (Player) params[0];
						Room room = (Room) params[1];
						SwitchedPlayerSpectatorResult result = (SwitchedPlayerSpectatorResult) params[2];

						event.handle(player, room, result);

						return null;
					}
				});
			}
		});

	}

}
