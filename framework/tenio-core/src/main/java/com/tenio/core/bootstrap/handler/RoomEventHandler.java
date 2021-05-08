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
package com.tenio.core.bootstrap.handler;

import java.util.Optional;
import java.util.function.Consumer;

import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.configuration.define.ExtensionEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.event.Subscriber;
import com.tenio.core.exception.ExtensionValueCastException;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.event.EventPlayerAfterLeftRoom;
import com.tenio.core.extension.event.EventPlayerBeforeLeaveRoom;
import com.tenio.core.extension.event.EventPlayerJoinRoomHandle;
import com.tenio.core.extension.event.EventRoomWasCreated;
import com.tenio.core.extension.event.EventRoomWillBeRemoved;

/**
 * @author kong
 */
@Component
public final class RoomEventHandler extends AbstractExtensionHandler {

	@AutowiredAcceptNull
	private EventPlayerAfterLeftRoom __eventPlayerAfterLeftRoom;

	@AutowiredAcceptNull
	private EventPlayerBeforeLeaveRoom __eventPlayerBeforeLeaveRoom;

	@AutowiredAcceptNull
	private EventPlayerJoinRoomHandle __eventPlayerJoinRoomHandle;

	@AutowiredAcceptNull
	private EventRoomWasCreated __eventRoomWasCreated;

	@AutowiredAcceptNull
	private EventRoomWillBeRemoved __eventRoomWillBeRemoved;

	public void initialize() {
		Optional<EventPlayerAfterLeftRoom> eventPlayerAfterLeftRoomOp = Optional
				.ofNullable(__eventPlayerAfterLeftRoom);
		Optional<EventPlayerBeforeLeaveRoom> eventPlayerBeforeLeaveRoomOp = Optional
				.ofNullable(__eventPlayerBeforeLeaveRoom);
		Optional<EventPlayerJoinRoomHandle> eventPlayerJoinRoomHandleOp = Optional
				.ofNullable(__eventPlayerJoinRoomHandle);

		Optional<EventRoomWasCreated> eventRoomWasCreatedOp = Optional.ofNullable(__eventRoomWasCreated);
		Optional<EventRoomWillBeRemoved> eventRoomWillBeRemovedOp = Optional.ofNullable(__eventRoomWillBeRemoved);

		eventPlayerAfterLeftRoomOp.ifPresent(new Consumer<EventPlayerAfterLeftRoom>() {

			@Override
			public void accept(EventPlayerAfterLeftRoom event) {
				_on(ExtensionEvent.PLAYER_AFTER_LEFT_ROOM, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Player player = _getPlayer(params[0]);
						Room room = _getRoom(params[1]);
						boolean force = _getBoolean(params[2]);

						event.handle(player, room, force);

						return null;
					}
				});
			}
		});

		eventPlayerBeforeLeaveRoomOp.ifPresent(new Consumer<EventPlayerBeforeLeaveRoom>() {

			@Override
			public void accept(EventPlayerBeforeLeaveRoom event) {
				_on(ExtensionEvent.PLAYER_BEFORE_LEAVE_ROOM, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Player player = _getPlayer(params[0]);
						Room room = _getRoom(params[1]);

						event.handle(player, room);

						return null;
					}
				});
			}
		});

		eventPlayerJoinRoomHandleOp.ifPresent(new Consumer<EventPlayerJoinRoomHandle>() {

			@Override
			public void accept(EventPlayerJoinRoomHandle event) {
				_on(ExtensionEvent.PLAYER_JOIN_ROOM_HANDLE, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Player player = _getPlayer(params[0]);
						Room room = _getRoom(params[1]);
						boolean success = _getBoolean(params[2]);
						CoreMessageCode code = _getCoreMessageCode(params[3]);

						event.handle(player, room, success, code);

						return null;
					}
				});
			}
		});

		eventRoomWasCreatedOp.ifPresent(new Consumer<EventRoomWasCreated>() {

			@Override
			public void accept(EventRoomWasCreated event) {
				_on(ExtensionEvent.ROOM_WAS_CREATED, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Room room = _getRoom(params[0]);
						CoreMessageCode code = _getCoreMessageCode(params[1]);

						event.handle(room, code);

						return null;
					}
				});
			}
		});

		eventRoomWillBeRemovedOp.ifPresent(new Consumer<EventRoomWillBeRemoved>() {

			@Override
			public void accept(EventRoomWillBeRemoved event) {
				_on(ExtensionEvent.ROOM_WILL_BE_REMOVED, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Room room = _getRoom(params[0]);

						event.handle(room);

						return null;
					}
				});
			}
		});

	}

}
