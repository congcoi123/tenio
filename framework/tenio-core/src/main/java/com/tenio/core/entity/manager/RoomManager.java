/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.core.entity.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.core.api.RoomApi;
import com.tenio.core.configuration.CoreConfiguration;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.entity.AbstractPlayer;
import com.tenio.core.entity.AbstractRoom;
import com.tenio.core.event.IEventManager;
import com.tenio.core.exception.DuplicatedRoomException;
import com.tenio.core.exception.NullRoomException;

/**
 * Manage all your rooms ({@link AbstractRoom}) on the server. It is a singleton
 * pattern class, which can be called anywhere. But it's better that you use the
 * {@link RoomApi} interface for easy management.
 * 
 * @see IRoomManager
 * 
 * @author kong
 * 
 */
public final class RoomManager extends AbstractLogger implements IRoomManager {

	/**
	 * A map object to manage your rooms with the key must be a room's id
	 */
	private final Map<String, AbstractRoom> __rooms = new HashMap<String, AbstractRoom>();
	private final IEventManager __eventManager;

	public RoomManager(IEventManager eventManager) {
		__eventManager = eventManager;
	}

	@Override
	public void initialize(CoreConfiguration configuration) {
		// temporary do nothing
	}

	@Override
	public int count() {
		synchronized (__rooms) {
			return __rooms.size();
		}
	}

	@Override
	public Map<String, AbstractRoom> gets() {
		synchronized (__rooms) {
			return __rooms;
		}
	}

	@Override
	public void clear() {
		synchronized (__rooms) {
			__rooms.clear();
		}
	}

	@Override
	public AbstractRoom get(final String roomId) {
		synchronized (__rooms) {
			return __rooms.get(roomId);
		}
	}

	@Override
	public boolean contain(final String roomId) {
		synchronized (__rooms) {
			return __rooms.containsKey(roomId);
		}
	}

	@Override
	public void add(final AbstractRoom room) {
		synchronized (__rooms) {
			if (__rooms.containsKey(room.getId())) {
				// fire an event
				__eventManager.getExtension().emit(ExtEvent.ROOM_WAS_CREATED, room, CoreMessageCode.ROOM_WAS_EXISTED);
				var e = new DuplicatedRoomException();
				error(e, "room id: ", room.getId());
				throw e;
			}
			__rooms.put(room.getId(), room);
			// fire an event
			__eventManager.getExtension().emit(ExtEvent.ROOM_WAS_CREATED, room);
		}
	}

	@Override
	public void remove(final AbstractRoom room) {
		synchronized (__rooms) {
			if (!__rooms.containsKey(room.getId())) {
				var e = new NullRoomException();
				error(e, "room id: ", room.getId());
				throw e;
			}

			// fire an event
			__eventManager.getExtension().emit(ExtEvent.ROOM_WILL_BE_REMOVED, room);
			// force all players leave this room
			__forceAllPlayersLeaveRoom(room);
			// remove itself from the current list
			__rooms.remove(room.getId());
		}

	}

	/**
	 * Force all players remove in one room without their desire. It's useful when
	 * you want to kick someone from his room because of his cheating or something
	 * else.
	 *
	 * @param room the corresponding room @see {@link AbstractRoom}
	 */
	private void __forceAllPlayersLeaveRoom(final AbstractRoom room) {
		final List<AbstractPlayer> removePlayers = new ArrayList<AbstractPlayer>();
		room.getPlayers().values().forEach(player -> {
			removePlayers.add(player);
		});
		for (var player : removePlayers) {
			makePlayerLeaveRoom(player, true);
		}
		removePlayers.clear();
	}

	@Override
	public CoreMessageCode makePlayerJoinRoom(final AbstractRoom room, final AbstractPlayer player) {
		if (room.contain(player.getName())) {
			__eventManager.getExtension().emit(ExtEvent.PLAYER_JOIN_ROOM_HANDLE, player, room, false,
					CoreMessageCode.PLAYER_WAS_IN_ROOM);
			return CoreMessageCode.PLAYER_WAS_IN_ROOM;
		}

		if (room.isFull()) {
			__eventManager.getExtension().emit(ExtEvent.PLAYER_JOIN_ROOM_HANDLE, player, room, false, CoreMessageCode.ROOM_IS_FULL);
			return CoreMessageCode.ROOM_IS_FULL;
		}

		// the player need to leave his room (if existed) first
		makePlayerLeaveRoom(player, false);

		room.add(player);
		player.setRoom(room);
		// fire an event
		__eventManager.getExtension().emit(ExtEvent.PLAYER_JOIN_ROOM_HANDLE, player, room, true);

		return null;
	}

	@Override
	public CoreMessageCode makePlayerLeaveRoom(final AbstractPlayer player, final boolean force) {
		var room = player.getRoom();
		if (room == null) {
			return CoreMessageCode.PLAYER_ALREADY_LEFT_ROOM;
		}

		// fire an event
		__eventManager.getExtension().emit(ExtEvent.PLAYER_BEFORE_LEAVE_ROOM, player, room);
		room.remove(player);
		player.setRoom(null);
		// fire an event
		__eventManager.getExtension().emit(ExtEvent.PLAYER_AFTER_LEFT_ROOM, player, room, force);

		return null;
	}

}
