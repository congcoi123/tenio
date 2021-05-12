package com.tenio.core.entities.managers;

import java.util.List;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.Zone;
import com.tenio.core.entities.settings.InitialRoomSetting;

public interface RoomManager {

	void addRoom(Room room);

	Room createRoom(InitialRoomSetting roomSetting) throws RuntimeException;

	Room createRoom(InitialRoomSetting roomSetting, Player player) throws RuntimeException;

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

	Zone getOwnerZone();

	void setOwnerZone(Zone zone);

	void removePlayer(Player player);

	void removePlayerFromRoom(Player player, Room room);

	void changeRoomName(Room room, String roomName) throws RuntimeException;

	void changeRoomPassword(Room room, String roomPassword) throws RuntimeException;

	void changeRoomCapacity(Room room, int maxPlayers, int maxSpectators);

}
