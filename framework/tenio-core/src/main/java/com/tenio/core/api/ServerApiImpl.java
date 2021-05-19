package com.tenio.core.api;

import com.tenio.common.data.ZeroObject;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.defines.PlayerBanMode;
import com.tenio.core.entities.defines.PlayerDisconnectedReason;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.network.entities.session.Session;

public final class ServerApiImpl implements ServerApi {

	@Override
	public void login(Player player, Session session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void kickPlayer(Player player, String message, int delayInSeconds) {
		// TODO Auto-generated method stub

	}

	@Override
	public void banPlayer(Player player, String message, PlayerBanMode banMode, int durationInMinutes,
			int delayInSeconds) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnectPlayer(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnectPlayer(Player player, PlayerDisconnectedReason disconnectedReason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnectSession(Session session) {
		// TODO Auto-generated method stub

	}

	@Override
	public Room createRoom(InitialRoomSetting setting, Player owner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Room createRoom(InitialRoomSetting setting, Player ownder, Room roomToLeave) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Player getPlayerById(long playerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Player getPlayerByName(String playerName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Player getPlayerBySession(Session session) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void joinRoom(Player player, Room room, int slotInRoom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void joinRoom(Player player, Room room) {
		// TODO Auto-generated method stub

	}

	@Override
	public void joinRoom(Player player, Room room, String roomPassword, boolean asSpectator, Room roomToLeave) {
		// TODO Auto-generated method stub

	}

	@Override
	public void joinRoom(Player player, Room room, String roomPassword, boolean asSpectator, Room roomToLeave,
			int slotInRoom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void leaveRoom(Player player, Room room) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRoom(Room room) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendPublicMessage(Room room, Player sender, ZeroObject message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendPrivateMessage(Player sender, Player recipient, ZeroObject message) {
		// TODO Auto-generated method stub

	}

}
