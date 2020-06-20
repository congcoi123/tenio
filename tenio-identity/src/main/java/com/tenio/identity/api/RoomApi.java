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
package com.tenio.identity.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.identity.entity.AbstractRoom;
import com.tenio.identity.entity.manager.IRoomManager;

/**
 * This class provides you a necessary interface for managing rooms.
 * 
 * @see IRoomManager
 * 
 * @author kong
 * 
 */
public final class RoomApi extends AbstractLogger {

	private final IRoomManager __roomManager;

	public RoomApi(IRoomManager roomManager) {
		__roomManager = roomManager;
	}

	/**
	 * @return all the current rooms in your server
	 */
	public Map<String, AbstractRoom> gets() {
		return __roomManager.gets();
	}

	/**
	 * Add a new room to your server. You need create your own room first.
	 * 
	 * @param room that is added, see {@link AbstractRoom}
	 */
	public void add(final AbstractRoom room) {
		__roomManager.add(room);
	}

	/**
	 * Determine if the room has existed or not.
	 * 
	 * @param roomId the unique ID
	 * @return Returns <b>true</b> if the room has existed, <b>null</b> otherwise
	 */
	public boolean contain(final String roomId) {
		return __roomManager.contain(roomId);
	}

	/**
	 * Retrieve a room by its ID.
	 * 
	 * @param roomId the unique ID
	 * @return Returns a room's instance if it has existed, <b>null</b> otherwise
	 */
	public AbstractRoom get(final String roomId) {
		return __roomManager.get(roomId);
	}

	/**
	 * Remove a room from your server.
	 * 
	 * @param room that is removed, see {@link AbstractRoom}
	 */
	public void remove(final AbstractRoom room) {
		__roomManager.remove(room);
	}

	/**
	 * @return all rooms' information data
	 */
	public List<List<Object>> getAllRoomInfos() {
		var list = new ArrayList<List<Object>>();
		gets().values().forEach((room) -> {
			var data = new ArrayList<Object>();
			data.add(room.getId());
			data.add(room.getName());
			data.add(strgen(room.getPlayers().size(), "/", room.getCapacity()));
			data.add(room.getState());
			var players = new StringBuilder();
			players.append("{ ");
			room.getPlayers().forEach((key, player) -> {
				players.append(player.getName());
				players.append(", ");
			});
			players.append("}");
			data.add(players.toString());
			list.add(data);
		});
		return list;
	}

}
