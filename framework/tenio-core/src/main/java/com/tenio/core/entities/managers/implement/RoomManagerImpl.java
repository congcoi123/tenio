package com.tenio.core.entities.managers.implement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.implement.RoomImpl;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.exceptions.AddedDuplicatedRoomException;
import com.tenio.core.exceptions.CoreMessageCodeException;

public final class RoomManagerImpl implements RoomManager {

	private final Map<Long, Room> __roomByIds;

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
		if (containsRoom(room)) {
			throw new AddedDuplicatedRoomException(room);
		}
		synchronized (this) {
			__roomByIds.put(room.getId(), room);
			__roomByNames.put(room.getName(), room);
			__roomCount = __roomByIds.size();
		}
	}

	@Override
	public Room createRoom(InitialRoomSetting roomSetting) {
		return createRoomWithOwner(roomSetting, null);
	}

	@Override
	public Room createRoomWithOwner(InitialRoomSetting roomSetting, Player player) {
		if (getRoomCount() >= MAX_NUMBER_ROOM) {
			throw new CoreMessageCodeException(null, null);
		}

		Room newRoom = RoomImpl.newInstance();
		newRoom.setName(roomSetting.getName());
		newRoom.setPassword(roomSetting.getPassword());
		newRoom.setActivated(roomSetting.isActivated());
		newRoom.setCapacity(roomSetting.getMaxPlayers(), roomSetting.getMaxSpectators());
		newRoom.setOwner(player);
		newRoom.setPlayerManager(null);
		newRoom.setPlayerSlotGeneratedStrategy(roomSetting.getRoomPlayerSlotGeneratedStrategy());
		newRoom.setRoomCredentialValidatedStrategy(roomSetting.getRoomCredentialValidatedStrategy());
		newRoom.setRoomRemoveMode(roomSetting.getRoomRemoveMode());

		synchronized (this) {
			__roomByIds.put(newRoom.getId(), newRoom);
			__roomByNames.put(newRoom.getName(), newRoom);
			return newRoom;
		}
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
