package com.tenio.core.entities.managers.implement;

import java.util.List;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.settings.InitialRoomSetting;

public final class RoomManagerImpl implements RoomManager {

	@Override
	public void addRoom(Room room) {
		// TODO Auto-generated method stub

	}

	@Override
	public Room createRoom(InitialRoomSetting roomSetting) throws RuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Room createRoom(InitialRoomSetting roomSetting, Player player) throws RuntimeException {
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
	public void changeRoomName(Room room, String roomName) throws RuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeRoomPassword(Room room, String roomPassword) throws RuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeRoomCapacity(Room room, int maxPlayers, int maxSpectators) {
		// TODO Auto-generated method stub

	}

}
