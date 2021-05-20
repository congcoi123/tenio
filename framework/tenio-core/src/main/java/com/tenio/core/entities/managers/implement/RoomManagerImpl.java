package com.tenio.core.entities.managers.implement;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.implement.RoomImpl;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.events.EventManager;
import com.tenio.core.exceptions.AddedDuplicatedRoomException;
import com.tenio.core.exceptions.CoreMessageCodeException;
import com.tenio.core.manager.AbstractManager;

public final class RoomManagerImpl extends AbstractManager implements RoomManager {

	private static final int DEFAULT_MAX_ROOMS = 100;

	private final Map<Long, Room> __roomByIds;
	private int __maxRooms;

	public static RoomManager newInstance(EventManager eventManager) {
		return new RoomManagerImpl(eventManager);
	}

	private RoomManagerImpl(EventManager eventManager) {
		super(eventManager);

		__roomByIds = new ConcurrentHashMap<Long, Room>();
		__maxRooms = DEFAULT_MAX_ROOMS;
	}

	@Override
	public void setMaxRooms(int maxRooms) {
		__maxRooms = maxRooms;
	}

	@Override
	public int getMaxRooms() {
		return __maxRooms;
	}

	@Override
	public void addRoom(Room room) {
		if (containsRoomId(room.getId())) {
			throw new AddedDuplicatedRoomException(room);
		}
		__roomByIds.put(room.getId(), room);
	}

	@Override
	public Room createRoom(InitialRoomSetting roomSetting) {
		return createRoomWithOwner(roomSetting, null);
	}

	@Override
	public Room createRoomWithOwner(InitialRoomSetting roomSetting, Player player) {
		if (getRoomCount() >= getMaxRooms()) {
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

		addRoom(newRoom);

		return newRoom;
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
	public Room getRoomById(long roomId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Room> getRoomListByName(String roomName) {
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
	public void removePlayer(Player player) {
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
