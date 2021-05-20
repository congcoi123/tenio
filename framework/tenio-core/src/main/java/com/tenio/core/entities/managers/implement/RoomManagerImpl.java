package com.tenio.core.entities.managers.implement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.settings.InitialRoomSetting;

public final class RoomManagerImpl implements RoomManager {

	@GuardedBy("this")
	private final Map<Long, Room> __roomByIds;
	@GuardedBy("this")
	private final Map<String, Room> __roomByNames;

	private volatile int __roomCount;

	public static RoomManager newInstance() {
		return new RoomManagerImpl();
	}

	private RoomManagerImpl() {
		__roomByIds = new HashMap<Long, Room>();
		__roomByNames = new HashMap<String, Room>();

		__roomCount = 0;
	}

	@Override
	public void addRoom(Room room) {
		synchronized (this) {
			__roomByIds.put(room.getId(), room);
			__roomByNames.put(room.getName(), room);
		}
	}

	@Override
	public Room createRoom(InitialRoomSetting roomSetting) {
		return createRoomWithOwner(roomSetting, null);
	}

	@Override
	public Room createRoomWithOwner(InitialRoomSetting roomSetting, Player player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsRoomId(long roomId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsRoomName(String roomName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsRoom(Room room) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Room getRoomById(long roomId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Room getRoomByName(String roomName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Room> getRoomList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkAndRemove(Room room) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRoomById(long roomId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRoomByName(String roomName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRoom(Room room) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePlayer(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePlayerFromRoom(Player player, Room room) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeRoomName(Room room, String roomName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeRoomPassword(Room room, String roomPassword) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeRoomCapacity(Room room, int maxPlayers, int maxSpectators) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getRoomCount() {
		return __roomCount;
	}

}
