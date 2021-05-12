package com.tenio.core.api;

import com.tenio.common.data.ZeroObject;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.entities.settings.PlayerBanMode;
import com.tenio.core.entities.settings.PlayerDisconnectedReason;
import com.tenio.core.network.entities.session.Session;

public interface ServerApi {

	void login(Player player, Session session);

	void logout(Player player);

	void kickPlayer(Player player, String message, int delayInSeconds);

	void banPlayer(Player player, String message, PlayerBanMode banMode, int durationInMinutes, int delayInSeconds);

	void disconnectPlayer(Player player);

	void disconnectPlayer(Player player, PlayerDisconnectedReason disconnectedReason);

	void disconnectSession(Session session);

	Room createRoom(InitialRoomSetting setting, Player owner) throws RuntimeException;

	Room createRoom(InitialRoomSetting setting, Player ownder, Room roomToLeave) throws RuntimeException;

	Player getPlayerById(long playerId);

	Player getPlayerByName(String playerName);

	Player getPlayerBySession(Session session);

	void joinRoom(Player player, Room room, int slotInRoom) throws RuntimeException;

	void joinRoom(Player player, Room room) throws RuntimeException;

	void joinRoom(Player player, Room room, String roomPassword, boolean asSpectator, Room roomToLeave)
			throws RuntimeException;

	void joinRoom(Player player, Room room, String roomPassword, boolean asSpectator, Room roomToLeave, int slotInRoom,
			boolean allowHolding) throws RuntimeException;

	void joinRoom(Player player, Room room, String roomPassword, boolean asSpectator, Room roomToLeave, int slotInRoom)
			throws RuntimeException;

	void leaveRoom(Player player, Room room);

	void removeRoom(Room room);

	void sendPublicMessage(Room room, Player sender, ZeroObject message);

	void sendPrivateMessage(Player sender, Player recipient, ZeroObject message);

}
