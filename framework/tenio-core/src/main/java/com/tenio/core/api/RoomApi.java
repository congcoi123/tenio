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
package com.tenio.core.api;

import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.exception.DuplicatedRoomIdException;
import com.tenio.core.exception.NullRoomException;

/**
 * This class provides you a necessary interface for managing rooms.
 * 
 * @see RoomManager
 * 
 * @author kong
 */
@ThreadSafe
public final class RoomApi extends AbstractLogger {

	private final RoomManager __roomManager;

	public RoomApi(RoomManager roomManager) {
		__roomManager = roomManager;
	}

	/**
	 * @return all the current rooms in your server
	 */
	public Map<String, Room> gets() {
		return __roomManager.gets();
	}

	/**
	 * Add a new room to your server. You need create your own room first.
	 * 
	 * @param room that is added, see {@link Room}
	 */
	public void add(Room room) {
		try {
			__roomManager.add(room);
		} catch (DuplicatedRoomIdException e) {
			error(e, e.getMessage());
		}
	}

	/**
	 * Determine if the room has existed or not.
	 * 
	 * @param roomId the unique ID
	 * @return Returns <b>true</b> if the room has existed, <b>null</b> otherwise
	 */
	public boolean contain(String roomId) {
		return __roomManager.contain(roomId);
	}

	/**
	 * Retrieve a room by its ID.
	 * 
	 * @param roomId the unique ID
	 * @return Returns a room's instance if it has existed, <b>null</b> otherwise
	 */
	public Room get(String roomId) {
		return __roomManager.get(roomId);
	}

	/**
	 * Remove a room from your server.
	 * 
	 * @param room that is removed, see {@link Room}
	 */
	public void remove(Room room) {
		try {
			__roomManager.remove(room);
		} catch (NullRoomException e) {
			error(e, e.getMessage());
		}
	}

}
