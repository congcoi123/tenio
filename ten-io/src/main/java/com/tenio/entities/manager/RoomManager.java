/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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
package com.tenio.entities.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.tenio.configuration.constant.ErrorMsg;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.AbstractRoom;
import com.tenio.event.EventManager;
import com.tenio.exception.NullRoomException;
import com.tenio.logger.AbstractLogger;

/**
 * 
 * @see {@link IRoomManager}
 * 
 * @author kong
 * 
 */
public final class RoomManager extends AbstractLogger implements IRoomManager {

	/**
	 * A map object to manage your rooms with the key must be a room's id
	 */
	private final Map<String, AbstractRoom> __rooms = new HashMap<String, AbstractRoom>();

	@Override
	public int count() {
		synchronized (__rooms) {
			return __rooms.size();
		}
	}

	@Override
	public Map<String, AbstractRoom> gets() {
		return __deepCopy();
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
			__rooms.put(room.getId(), room);
			// fire an event
			EventManager.getEvent().emit(TEvent.CREATED_ROOM, room);
		}
	}

	@Override
	public void remove(final AbstractRoom room) {
		try {
			if (!contain(room.getId())) {
				throw new NullRoomException();
			}
		} catch (NullRoomException e) {
			error("REMOVE ROOM", room.getName(), e);
			return;
		}

		synchronized (__rooms) {
			// fire an event
			EventManager.getEvent().emit(TEvent.REMOVE_ROOM, room);
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
		room.getPlayers().values().forEach(player -> {
			playerLeaveRoom(player, true);
		});
	}

	@Override
	public void playerJoinRoom(final AbstractRoom room, final AbstractPlayer player) {
		if (room.contain(player.getName())) {
			EventManager.getEvent().emit(TEvent.PLAYER_JOIN_ROOM, player, room, false, ErrorMsg.PLAYER_WAS_IN_ROOM);
			return;
		}

		if (room.isFull()) {
			EventManager.getEvent().emit(TEvent.PLAYER_JOIN_ROOM, player, room, false, ErrorMsg.ROOM_IS_FULL);
			return;
		}

		// the player need to leave his room (if existed) first
		playerLeaveRoom(player, false);

		room.add(player);
		player.setRoom(room);
		// fire an event
		EventManager.getEvent().emit(TEvent.PLAYER_JOIN_ROOM, player, room, true);
	}

	@Override
	public void playerLeaveRoom(final AbstractPlayer player, final boolean force) {
		var room = player.getRoom();
		if (room == null) {
			return;
		}

		// fire an event
		EventManager.getEvent().emit(TEvent.PLAYER_BEFORE_LEAVE_ROOM, player, room);
		room.remove(player);
		player.setRoom(null);
		// fire an event
		EventManager.getEvent().emit(TEvent.PLAYER_LEFT_ROOM, player, room, force);
	}
	
	private Map<String, AbstractRoom> __deepCopy() {
		Set<Entry<String, AbstractRoom>> entries = __rooms.entrySet();
		HashMap<String, AbstractRoom> shallowCopy = (HashMap<String, AbstractRoom>) entries.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		return shallowCopy;
	}

}
