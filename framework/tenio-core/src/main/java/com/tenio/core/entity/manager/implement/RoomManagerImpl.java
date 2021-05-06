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
package com.tenio.core.entity.manager.implement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.api.RoomApi;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.configuration.define.ZeroEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.event.EventManager;
import com.tenio.core.exception.DuplicatedRoomIdException;
import com.tenio.core.exception.NullRoomException;

/**
 * Manage all your rooms ({@link Room}) on the server. It is a singleton
 * pattern class, which can be called anywhere. But it's better that you use the
 * {@link RoomApi} interface for easy management.
 * 
 * @see RoomManager
 * 
 * @author kong
 * 
 */
@ThreadSafe
public final class RoomManagerImpl implements RoomManager {

	/**
	 * A map object to manage your rooms with the key must be a room's id
	 */
	private final Map<String, Room> __rooms;
	private final EventManager __eventManager;

	public RoomManagerImpl(EventManager eventManager) {
		__eventManager = eventManager;
		__rooms = new HashMap<String, Room>();
	}

	@Override
	public synchronized void initialize(Configuration configuration) {
		// temporary do nothing
	}

	@Override
	public int count() {
		synchronized (__rooms) {
			return __rooms.size();
		}
	}

	@Override
	public Map<String, Room> gets() {
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
	public Room get(String roomId) {
		synchronized (__rooms) {
			return __rooms.get(roomId);
		}
	}

	@Override
	public boolean contain(String roomId) {
		synchronized (__rooms) {
			return __rooms.containsKey(roomId);
		}
	}

	@Override
	public void add(Room room) throws DuplicatedRoomIdException {
		synchronized (__rooms) {
			if (__rooms.containsKey(room.getId())) {
				// fire an event
				__eventManager.getExtension().emit(ZeroEvent.ROOM_WAS_CREATED, room, CoreMessageCode.ROOM_WAS_EXISTED);
				throw new DuplicatedRoomIdException(room.getId());
			}
			__rooms.put(room.getId(), room);
			// fire an event
			__eventManager.getExtension().emit(ZeroEvent.ROOM_WAS_CREATED, room);
		}
	}

	@Override
	public void remove(Room room) throws NullRoomException {
		synchronized (__rooms) {
			if (!__rooms.containsKey(room.getId())) {
				throw new NullRoomException(room.getId());
			}

			// fire an event
			__eventManager.getExtension().emit(ZeroEvent.ROOM_WILL_BE_REMOVED, room);
			// force all players to leave this room
			__forceAllPlayersLeaveRoom(room);
			// remove itself from the current list
			__rooms.remove(room.getId());
		}

	}

	/**
	 * Force all players to remove in one room without their desires. It's useful
	 * when you want to kick someone from his room because of his cheating or
	 * something else.
	 *
	 * @param room the corresponding room @see {@link Room}
	 */
	private void __forceAllPlayersLeaveRoom(Room room) {
		final List<Player> removePlayers = new ArrayList<Player>();
		var players = room.getPlayers().values();
		players.forEach(player -> {
			removePlayers.add(player);
		});
		for (var player : removePlayers) {
			makePlayerLeaveRoom(player, true);
		}
		removePlayers.clear();
	}

	@Override
	public CoreMessageCode makePlayerJoinRoom(Room room, Player player) {
		if (room.containPlayerName(player.getName())) {
			__eventManager.getExtension().emit(ZeroEvent.PLAYER_JOIN_ROOM_HANDLE, player, room, false,
					CoreMessageCode.PLAYER_WAS_IN_ROOM);
			return CoreMessageCode.PLAYER_WAS_IN_ROOM;
		}

		if (room.isFull()) {
			__eventManager.getExtension().emit(ZeroEvent.PLAYER_JOIN_ROOM_HANDLE, player, room, false,
					CoreMessageCode.ROOM_IS_FULL);
			return CoreMessageCode.ROOM_IS_FULL;
		}

		room.addPlayer(player);
		player.setCurrentRoom(room);
		// fire an event
		__eventManager.getExtension().emit(ZeroEvent.PLAYER_JOIN_ROOM_HANDLE, player, room, true, null);

		return null;
	}

	@Override
	public CoreMessageCode makePlayerLeaveRoom(Player player, boolean force) {
		var room = player.getCurrentRoom();
		if (room == null) {
			return CoreMessageCode.PLAYER_ALREADY_LEFT_ROOM;
		}

		// fire an event
		__eventManager.getExtension().emit(ZeroEvent.PLAYER_BEFORE_LEAVE_ROOM, player, room);
		room.removePlayer(player);
		player.setCurrentRoom(null);
		// fire an event
		__eventManager.getExtension().emit(ZeroEvent.PLAYER_AFTER_LEFT_ROOM, player, room, force);

		return null;
	}

}
