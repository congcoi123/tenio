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

import com.tenio.api.RoomApi;
import com.tenio.configuration.constant.ErrorMsg;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.AbstractRoom;
import com.tenio.event.EventManager;
import com.tenio.logger.AbstractLogger;

/**
 * Manage all your rooms @see {@link AbstractRoom} on the server. It is a
 * singleton pattern class, which can be called anywhere. But it's better that
 * you use the {@link RoomApi} interface for easy management.
 * 
 * @author kong
 * 
 */
public final class RoomManager extends AbstractLogger {

	private static volatile RoomManager __instance;

	private RoomManager() {
	} // prevent creation manually

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static RoomManager getInstance() {
		if (__instance == null) {
			__instance = new RoomManager();
		}
		return __instance;
	}

	/**
	 * An instance to push events @see {@link EventManager}
	 */
	private EventManager __events = EventManager.getInstance();
	/**
	 * A map object to manage your rooms with the key must be a room's id
	 */
	private Map<String, AbstractRoom> __rooms = new HashMap<String, AbstractRoom>();

	/**
	 * @return the number of rooms in your server
	 */
	public int count() {
		return __rooms.size();
	}

	/**
	 * @return all the current rooms in your server
	 */
	public Map<String, AbstractRoom> gets() {
		return __rooms;
	}

	public void clear() {
		__rooms.clear();
		__rooms = null;
	}

	public AbstractRoom get(final String roomId) {
		return __rooms.get(roomId);
	}

	public boolean contain(final String roomId) {
		return __rooms.containsKey(roomId);
	}

	/**
	 * Add a new room to your server. You need create your own room first.
	 * 
	 * @param room that is added @see {@link AbstractRoom}
	 */
	public synchronized void add(final AbstractRoom room) {
		__rooms.put(room.getId(), room);
		// fire event
		__events.emit(TEvent.CREATED_ROOM, room);
	}

	/**
	 * Remove a room from your server.
	 * 
	 * @param room that is removed @see {@link AbstractRoom}
	 */
	public synchronized void remove(final AbstractRoom room) {
		try {
			if (!contain(room.getId())) {
				throw new NullPointerException();
			}
		} catch (NullPointerException e) {
			error("REMOVE ROOM", room.getName(), e);
			return;
		}

		// fire event
		__events.emit(TEvent.REMOVE_ROOM, room);
		// force all player leave from room
		__forceAllPlayersLeaveRoom(room);
		// remove itself from the current list
		__rooms.remove(room.getId());
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

	/**
	 * Request one player to join a room. This request can be refused with some
	 * reason. You can handle these results in the corresponding events.
	 * 
	 * @param room   the desired room @see {@link AbstractRoom}
	 * @param player the current player @see {@link AbstractPlayer}
	 */
	public synchronized void playerJoinRoom(final AbstractRoom room, final AbstractPlayer player) {
		if (room.contain(player.getName())) {
			__events.emit(TEvent.PLAYER_JOIN_ROOM, player, room, false, ErrorMsg.PLAYER_WAS_IN_ROOM);
			return;
		}

		if (room.isFull()) {
			__events.emit(TEvent.PLAYER_JOIN_ROOM, player, room, false, ErrorMsg.ROOM_IS_FULL);
			return;
		}

		// player need to leave his room (if existed) first
		playerLeaveRoom(player, false);

		room.add(player);
		player.setRoom(room);
		// fire event
		__events.emit(TEvent.PLAYER_JOIN_ROOM, player, room, true);
	}

	/**
	 * Allow a player to leave his current room. You can handle your own logic in
	 * the corresponding events.
	 * 
	 * @param player that will be left his current room @see {@link AbstractPlayer}
	 * @param force  it's set <code>true</code> if you want to force the player
	 *               leave. Otherwise, it's set <code>false</code>
	 */
	public synchronized void playerLeaveRoom(final AbstractPlayer player, final boolean force) {
		AbstractRoom room = player.getRoom();
		if (room == null) {
			return;
		}

		// fire event
		__events.emit(TEvent.PLAYER_BEFORE_LEAVE_ROOM, player, room);
		room.remove(player);
		player.setRoom(null);
		// fire event
		__events.emit(TEvent.PLAYER_LEFT_ROOM, player, room, force);
	}

}
