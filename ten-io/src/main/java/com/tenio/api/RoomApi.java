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
package com.tenio.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tenio.entities.AbstractRoom;
import com.tenio.entities.manager.IRoomManager;
import com.tenio.logger.AbstractLogger;

/**
 * This class provides you a necessary interface for managing rooms.
 * 
 * @see {@link IRoomManager}
 * 
 * @author kong
 * 
 */
public final class RoomApi extends AbstractLogger {

	/**
	 * @see {@link IRoomManager}
	 */
	private final IRoomManager __roomManager;

	public RoomApi(IRoomManager roomManager) {
		__roomManager = roomManager;
	}

	/**
	 * @see IRoomManager#gets()
	 */
	public Map<String, AbstractRoom> gets() {
		return __roomManager.gets();
	}

	/**
	 * @see IRoomManager#add(AbstractRoom)
	 */
	public void add(final AbstractRoom room) {
		__roomManager.add(room);
	}

	/**
	 * @see IRoomManager#remove(AbstractRoom)
	 */
	public void remove(final AbstractRoom room) {
		__roomManager.remove(room);
	}

	/**
	 * @return Returns all rooms' information data
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
