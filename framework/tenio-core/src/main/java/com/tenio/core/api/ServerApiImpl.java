package com.tenio.core.api;

import com.tenio.common.data.ZeroObject;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.defines.PlayerBanMode;
import com.tenio.core.entities.defines.PlayerDisconnectedReason;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.network.entities.session.Session;

public class ServerApiImpl implements ServerApi {

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
	public com.tenio.core.entities.Room createRoom(InitialRoomSetting setting, Player owner) throws RuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public com.tenio.core.entities.Room createRoom(InitialRoomSetting setting, Player ownder,
			com.tenio.core.entities.Room roomToLeave) throws RuntimeException {
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
	public void joinRoom(Player player, com.tenio.core.entities.Room room, int slotInRoom) throws RuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void joinRoom(Player player, com.tenio.core.entities.Room room) throws RuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void joinRoom(Player player, com.tenio.core.entities.Room room, String roomPassword, boolean asSpectator,
			com.tenio.core.entities.Room roomToLeave) throws RuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void joinRoom(Player player, com.tenio.core.entities.Room room, String roomPassword, boolean asSpectator,
			com.tenio.core.entities.Room roomToLeave, int slotInRoom, boolean allowHolding) throws RuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void joinRoom(Player player, com.tenio.core.entities.Room room, String roomPassword, boolean asSpectator,
			com.tenio.core.entities.Room roomToLeave, int slotInRoom) throws RuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void leaveRoom(Player player, com.tenio.core.entities.Room room) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRoom(com.tenio.core.entities.Room room) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendPublicMessage(com.tenio.core.entities.Room room, Player sender, ZeroObject message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendPrivateMessage(Player sender, Player recipient, ZeroObject message) {
		// TODO Auto-generated method stub

	}

}
