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

import com.tenio.core.bootstrap.annotation.ExtAutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.ExtComponent;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.configuration.define.ZeroEvent;
import com.tenio.core.entity.ZeroPlayer;
import com.tenio.core.entity.ZeroRoom;
import com.tenio.core.event.ISubscriber;
import com.tenio.core.exception.ExtensionValueCastException;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.event.IEventPlayerAfterLeftRoom;
import com.tenio.core.extension.event.IEventPlayerBeforeLeaveRoom;
import com.tenio.core.extension.event.IEventPlayerJoinRoomHandle;
import com.tenio.core.extension.event.IEventRoomWasCreated;
import com.tenio.core.extension.event.IEventRoomWillBeRemoved;

/**
 * @author kong
 */
@ExtComponent
public final class RoomEventHandler extends AbstractExtensionHandler {

	@ExtAutowiredAcceptNull
	private IEventPlayerAfterLeftRoom __eventPlayerAfterLeftRoom;

	@ExtAutowiredAcceptNull
	private IEventPlayerBeforeLeaveRoom __eventPlayerBeforeLeaveRoom;

	@ExtAutowiredAcceptNull
	private IEventPlayerJoinRoomHandle __eventPlayerJoinRoomHandle;

	@ExtAutowiredAcceptNull
	private IEventRoomWasCreated __eventRoomWasCreated;

	@ExtAutowiredAcceptNull
	private IEventRoomWillBeRemoved __eventRoomWillBeRemoved;

	public void initialize() {
		Optional<IEventPlayerAfterLeftRoom> eventPlayerAfterLeftRoomOp = Optional
				.ofNullable(__eventPlayerAfterLeftRoom);
		Optional<IEventPlayerBeforeLeaveRoom> eventPlayerBeforeLeaveRoomOp = Optional
				.ofNullable(__eventPlayerBeforeLeaveRoom);
		Optional<IEventPlayerJoinRoomHandle> eventPlayerJoinRoomHandleOp = Optional
				.ofNullable(__eventPlayerJoinRoomHandle);

		Optional<IEventRoomWasCreated> eventRoomWasCreatedOp = Optional.ofNullable(__eventRoomWasCreated);
		Optional<IEventRoomWillBeRemoved> eventRoomWillBeRemovedOp = Optional.ofNullable(__eventRoomWillBeRemoved);

		eventPlayerAfterLeftRoomOp.ifPresent(new Consumer<IEventPlayerAfterLeftRoom>() {

			@Override
			public void accept(IEventPlayerAfterLeftRoom event) {
				_on(ZeroEvent.PLAYER_AFTER_LEFT_ROOM, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						ZeroPlayer player = _getPlayer(params[0]);
						ZeroRoom room = _getRoom(params[1]);
						boolean force = _getBoolean(params[2]);

						event.handle(player, room, force);

						return null;
					}
				});
			}
		});

		eventPlayerBeforeLeaveRoomOp.ifPresent(new Consumer<IEventPlayerBeforeLeaveRoom>() {

			@Override
			public void accept(IEventPlayerBeforeLeaveRoom event) {
				_on(ZeroEvent.PLAYER_BEFORE_LEAVE_ROOM, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						ZeroPlayer player = _getPlayer(params[0]);
						ZeroRoom room = _getRoom(params[1]);

						event.handle(player, room);

						return null;
					}
				});
			}
		});

		eventPlayerJoinRoomHandleOp.ifPresent(new Consumer<IEventPlayerJoinRoomHandle>() {

			@Override
			public void accept(IEventPlayerJoinRoomHandle event) {
				_on(ZeroEvent.PLAYER_JOIN_ROOM_HANDLE, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						ZeroPlayer player = _getPlayer(params[0]);
						ZeroRoom room = _getRoom(params[1]);
						boolean success = _getBoolean(params[2]);
						CoreMessageCode code = _getCoreMessageCode(params[3]);

						event.handle(player, room, success, code);

						return null;
					}
				});
			}
		});

		eventRoomWasCreatedOp.ifPresent(new Consumer<IEventRoomWasCreated>() {

			@Override
			public void accept(IEventRoomWasCreated event) {
				_on(ZeroEvent.ROOM_WAS_CREATED, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						ZeroRoom room = _getRoom(params[0]);
						CoreMessageCode code = _getCoreMessageCode(params[1]);

						event.handle(room, code);

						return null;
					}
				});
			}
		});

		eventRoomWillBeRemovedOp.ifPresent(new Consumer<IEventRoomWillBeRemoved>() {

			@Override
			public void accept(IEventRoomWillBeRemoved event) {
				_on(ZeroEvent.ROOM_WILL_BE_REMOVED, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						ZeroRoom room = _getRoom(params[0]);

						event.handle(room);

						return null;
					}
				});
			}
		});

	}

}
