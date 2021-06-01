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
package com.tenio.core.entities.managers;

import java.util.Collection;
import java.util.List;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.exceptions.AddedDuplicatedRoomException;
import com.tenio.core.exceptions.CreatedRoomException;
import com.tenio.core.manager.Manager;

public interface RoomManager extends Manager {

	void setMaxRooms(int maxRooms);

	int getMaxRooms();

	void addRoom(Room room) throws AddedDuplicatedRoomException;

	default Room createRoom(InitialRoomSetting roomSetting) throws IllegalArgumentException, CreatedRoomException {
		return createRoomWithOwner(roomSetting, null);
	}

	Room createRoomWithOwner(InitialRoomSetting roomSetting, Player player)
			throws IllegalArgumentException, CreatedRoomException;

	boolean containsRoomId(long roomId);

	boolean containsRoomName(String roomName);

	Room getRoomById(long roomId);

	List<Room> getRoomListByName(String roomName);

	Collection<Room> getRoomList();

	void removeRoomById(long roomId);

	void changeRoomName(Room room, String roomName) throws IllegalArgumentException;

	void changeRoomPassword(Room room, String roomPassword) throws IllegalArgumentException;

	void changeRoomCapacity(Room room, int maxPlayers, int maxSpectators) throws IllegalArgumentException;

	int getRoomCount();

	default void clear() {
		throw new UnsupportedOperationException();
	}

}
