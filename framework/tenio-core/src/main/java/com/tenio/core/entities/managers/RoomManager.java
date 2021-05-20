package com.tenio.core.entities.managers;

import java.util.List;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.exceptions.AddedDuplicatedRoomException;
import com.tenio.core.exceptions.CoreMessageCodeException;

public interface RoomManager {

	void addRoom(Room room) throws AddedDuplicatedRoomException;

	Room createRoom(InitialRoomSetting roomSetting) throws IllegalArgumentException, CoreMessageCodeException;

	Room createRoomWithOwner(InitialRoomSetting roomSetting, Player player)
			throws IllegalArgumentException, CoreMessageCodeException;

	boolean containsRoomId(long roomId);

	boolean containsRoomName(String roomName);

	boolean containsRoom(Room room);

	Room getRoomById(long roomId);

	Room getRoomByName(String roomName);

	List<Room> getRoomList();

	void checkAndRemove(Room room);

	void removeRoomById(long roomId);

	void removeRoomByName(String roomName);

	void removeRoom(Room room);

	void removePlayer(Player player);

	void removePlayerFromRoom(Player player, Room room);

	void changeRoomName(Room room, String roomName) throws IllegalArgumentException;

	void changeRoomPassword(Room room, String roomPassword) throws IllegalArgumentException;

	void changeRoomCapacity(Room room, int maxPlayers, int maxSpectators) throws IllegalArgumentException;

	int getRoomCount();

}
