package com.tenio.core.api;

import java.util.ArrayList;

import com.tenio.common.data.ZeroObject;
import com.tenio.common.loggers.SystemLogger;
import com.tenio.core.configuration.defines.CoreMessageCode;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.defines.PlayerBanMode;
import com.tenio.core.entities.defines.PlayerDisconnectedReason;
import com.tenio.core.entities.defines.PlayerLeftRoomReason;
import com.tenio.core.entities.implement.RoomImpl;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.AddedDuplicatedPlayerException;
import com.tenio.core.exceptions.CoreMessageCodeException;
import com.tenio.core.exceptions.RemovedNonExistentPlayerException;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.server.Server;

public final class ServerApiImpl extends SystemLogger implements ServerApi {

	private final Server __server;

	public static ServerApi newInstance(Server server) {
		return new ServerApiImpl(server);
	}

	private ServerApiImpl(Server server) {
		__server = server;
	}

	@Override
	public void login(Player player) {
		login(player, null);
	}

	@Override
	public void login(Player player, Session session) {
		if (player.getName() == null) {
			// fire an event
			__getEventManager().emit(ServerEvent.PLAYER_LOGINED_FAILURE, player,
					CoreMessageCode.PLAYER_INFO_IS_INVALID);
			return;
		}

		try {
			__getPlayerManager().addPlayer(player);
			if (session != null) {
				session.setName(player.getName());
				player.setSession(session);
			}
			// fire an event
			__getEventManager().emit(ServerEvent.PLAYER_LOGINED_SUCCESS, player);
		} catch (AddedDuplicatedPlayerException e) {
			__getEventManager().emit(ServerEvent.PLAYER_LOGINED_FAILURE, player, CoreMessageCode.PLAYER_WAS_EXISTED);
		}
	}

	@Override
	public void logout(Player player) {
		if (player == null) {
			return;
		}

		try {
			if (player.isInRoom()) {
				leaveRoom(player, PlayerLeftRoomReason.LOG_OUT);
			}
			if (player.containsSession()) {
				disconnectPlayer(player, PlayerDisconnectedReason.DEFAULT);
			}
			__getPlayerManager().removePlayerByName(player.getName());
		} catch (RemovedNonExistentPlayerException e) {
			error(e, "Removed player: ", player.getName(), " with issue");
		}
	}

	@Override
	public void kickPlayer(Player player, String message, int delayInSeconds) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void banPlayer(Player player, String message, PlayerBanMode banMode, int durationInMinutes,
			int delayInSeconds) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void disconnectPlayer(Player player, PlayerDisconnectedReason disconnectedReason) {

	}

	@Override
	public void disconnectSession(Session session) {
		// TODO Auto-generated method stub

	}

	@Override
	public Room createRoom(InitialRoomSetting setting, Player owner) {
		Room room = null;
		try {
			room = __getRoomManager().createRoomWithOwner(setting, owner);
			__getEventManager().emit(ServerEvent.ROOM_WAS_CREATED, room);
		} catch (IllegalArgumentException e) {
			// FIXME:
			__getEventManager().emit(ServerEvent.ROOM_WAS_CREATED_WITH_ERROR, setting, null);
		} catch (CoreMessageCodeException e) {
			__getEventManager().emit(ServerEvent.ROOM_WAS_CREATED_WITH_ERROR, setting, e.getMessageCode());
		}
		return room;
	}

	@Override
	public Room createRoom(InitialRoomSetting setting, Player ownder, Room roomToLeave) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Player getPlayerByName(String playerName) {
		return __getPlayerManager().getPlayerByName(playerName);
	}

	@Override
	public Player getPlayerBySession(Session session) {
		return __getPlayerManager().getPlayerBySession(session);
	}

	@Override
	public void joinRoom(Player player, Room room, int slotInRoom) {
		joinRoom(player, room, null, false, room, slotInRoom);
	}

	@Override
	public void joinRoom(Player player, Room room) {
		joinRoom(player, room, RoomImpl.DEFAULT_SLOT);
	}

	@Override
	public void joinRoom(Player player, Room room, String roomPassword, boolean asSpectator, Room roomToLeave) {
		joinRoom(player, room, roomPassword, asSpectator, roomToLeave, RoomImpl.DEFAULT_SLOT);
	}

	@Override
	public void joinRoom(Player player, Room room, String roomPassword, boolean asSpectator, Room roomToLeave,
			int slotInRoom) {
//		if (room.containPlayerName(player.getName())) {
//			__eventManager.getExtension().emit(ExtEvent.PLAYER_JOIN_ROOM_HANDLE, player, room, false,
//					CoreMessageCode.PLAYER_WAS_IN_ROOM);
//			return CoreMessageCode.PLAYER_WAS_IN_ROOM;
//		}
//
//		if (room.isFull()) {
//			__eventManager.getExtension().emit(ExtEvent.PLAYER_JOIN_ROOM_HANDLE, player, room, false,
//					CoreMessageCode.ROOM_IS_FULL);
//			return CoreMessageCode.ROOM_IS_FULL;
//		}
//
//		room.addPlayer(player);
//		player.setCurrentRoom(room);
//		// fire an event
//		__eventManager.getExtension().emit(ExtEvent.PLAYER_JOIN_ROOM_HANDLE, player, room, true, null);

	}

	@Override
	public void switchPlayerToSpectator(Player player, Room room) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void switchSpectatorToPlayer(Player player, Room room, int targetSlot) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void switchSpectatorToPlayer(Player player, Room room) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void leaveRoom(Player player, PlayerLeftRoomReason leftRoomReason) {
//		var room = player.getCurrentRoom();
//		if (room == null) {
//			return CoreMessageCode.PLAYER_ALREADY_LEFT_ROOM;
//		}
//
//		// fire an event
//		__eventManager.getExtension().emit(ExtEvent.PLAYER_BEFORE_LEAVE_ROOM, player, room);
//		room.removePlayer(player);
//		player.setCurrentRoom(null);
//		// fire an event
//		__eventManager.getExtension().emit(ExtEvent.PLAYER_AFTER_LEFT_ROOM, player, room, force);
//
//		return null;
	}

	@Override
	public void removeRoom(Room room) {
//		synchronized (__rooms) {
//			if (!__rooms.containsKey(room.getId())) {
//				throw new NullRoomException(room.getId());
//			}
//
//			// fire an event
//			__eventManager.getExtension().emit(ExtEvent.ROOM_WILL_BE_REMOVED, room);
//			// force all players to leave this room
//			__forceAllPlayersLeaveRoom(room);
//			// remove itself from the current list
//			__rooms.remove(room.getId());
//		}
	}

//	private void __forceAllPlayersLeaveRoom(IRoom room) {
//		final List<IPlayer> removePlayers = new ArrayList<IPlayer>();
//		var players = room.getPlayers().values();
//		players.forEach(player -> {
//			removePlayers.add(player);
//		});
//		for (var player : removePlayers) {
//			makePlayerLeaveRoom(player, true);
//		}
//		removePlayers.clear();
//	}

	@Override
	public void sendPublicMessage(Room room, Player sender, ZeroObject message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendPrivateMessage(Player sender, Player recipient, ZeroObject message) {
		throw new UnsupportedOperationException();
	}

	private EventManager __getEventManager() {
		return __server.getEventManager();
	}

	private PlayerManager __getPlayerManager() {
		return __server.getPlayerManager();
	}

	private RoomManager __getRoomManager() {
		return __server.getRoomManager();
	}

}
