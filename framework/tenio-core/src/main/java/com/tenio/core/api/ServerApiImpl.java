package com.tenio.core.api;

import java.util.ArrayList;

import com.tenio.common.data.ZeroObject;
import com.tenio.core.configuration.defines.CoreMessageCode;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.defines.PlayerBanMode;
import com.tenio.core.entities.defines.PlayerDisconnectedReason;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.defines.TransportType;
import com.tenio.core.network.entities.session.Session;

public final class ServerApiImpl extends AbstractManager implements ServerApi {

	public static ServerApi newInstance(EventManager eventManager) {
		return new ServerApiImpl(eventManager);
	}

	private ServerApiImpl(EventManager eventManager) {
		super(eventManager);
	}

	@Override
	public void login(Player player, Session session) {
		__eventManager.emit(ServerEvent.PLAYER_DO_LOGIN, player, session);
//		if (player.getName() == null) {
//			// fire an event
//			__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_FAILED, player,
//					CoreMessageCode.PLAYER_INFO_IS_INVALID);
//			throw new NullPlayerNameException();
//		}
//
//		synchronized (__players) {
//			if (__players.containsKey(player.getName())) {
//				// fire an event
//				__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_FAILED, player,
//						CoreMessageCode.PLAYER_WAS_EXISTED);
//				throw new DuplicatedPlayerException(player.getName());
//			}
//
//			// add the main connection
//			connection.setPlayerName(player.getName());
//			int size = 0;
//			if (connection.isType(TransportType.WEB_SOCKET)) {
//				size = __webSocketPortsSize;
//			} else {
//				size = __socketPortsSize;
//			}
//			player.initializeConnections(size);
//			player.setConnection(connection, CoreConstants.MAIN_CONNECTION_INDEX);
//
//			__players.put(player.getName(), player);
//			__count = __players.size();
//			__countPlayers = (int) __players.values().stream().filter(p -> !p.isNPC()).count();
//
//			// fire an event
//			__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_SUCCESS, player);
//		}
	}

	@Override
	public void logout(Player player) {
		__eventManager.emit(ServerEvent.PLAYER_DO_LOGOUT, player);
		
//		if (player == null) {
//			return;
//		}
//
//		synchronized (__players) {
//			if (!__players.containsKey(player.getName())) {
//				return;
//			}
//
//			// force player to leave its current room, fire a logic event
//			__eventManager.getInternal().emit(ServerEvent.PLAYER_WAS_FORCED_TO_LEAVE_ROOM, player);
//
//			// remove all player's connections from the player
//			removeAllConnections(player);
//
//			__players.remove(player.getName());
//			__count = __players.size();
//			__countPlayers = (int) __players.values().stream().filter(p -> !p.isNPC()).count();
//		}
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
		synchronized (__rooms) {
			if (__rooms.containsKey(room.getId())) {
				// fire an event
				__eventManager.getExtension().emit(ExtEvent.ROOM_WAS_CREATED, room, CoreMessageCode.ROOM_WAS_EXISTED);
				throw new DuplicatedRoomIdException(room.getId());
			}
			__rooms.put(room.getId(), room);
			// fire an event
			__eventManager.getExtension().emit(ExtEvent.ROOM_WAS_CREATED, room);
		}
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
		if (room.containPlayerName(player.getName())) {
			__eventManager.getExtension().emit(ExtEvent.PLAYER_JOIN_ROOM_HANDLE, player, room, false,
					CoreMessageCode.PLAYER_WAS_IN_ROOM);
			return CoreMessageCode.PLAYER_WAS_IN_ROOM;
		}

		if (room.isFull()) {
			__eventManager.getExtension().emit(ExtEvent.PLAYER_JOIN_ROOM_HANDLE, player, room, false,
					CoreMessageCode.ROOM_IS_FULL);
			return CoreMessageCode.ROOM_IS_FULL;
		}

		room.addPlayer(player);
		player.setCurrentRoom(room);
		// fire an event
		__eventManager.getExtension().emit(ExtEvent.PLAYER_JOIN_ROOM_HANDLE, player, room, true, null);

	}

	@Override
	public void switchPlayerToSpectator(Player player, Room room) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchSpectatorToPlayer(Player player, Room room, int targetSlot) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchSpectatorToPlayer(Player player, Room room) {
		// TODO Auto-generated method stub

	}

	@Override
	public void leaveRoom(Player player, Room room) {
		var room = player.getCurrentRoom();
		if (room == null) {
			return CoreMessageCode.PLAYER_ALREADY_LEFT_ROOM;
		}

		// fire an event
		__eventManager.getExtension().emit(ExtEvent.PLAYER_BEFORE_LEAVE_ROOM, player, room);
		room.removePlayer(player);
		player.setCurrentRoom(null);
		// fire an event
		__eventManager.getExtension().emit(ExtEvent.PLAYER_AFTER_LEFT_ROOM, player, room, force);

		return null;
	}

	@Override
	public void removeRoom(Room room) {
		synchronized (__rooms) {
			if (!__rooms.containsKey(room.getId())) {
				throw new NullRoomException(room.getId());
			}

			// fire an event
			__eventManager.getExtension().emit(ExtEvent.ROOM_WILL_BE_REMOVED, room);
			// force all players to leave this room
			__forceAllPlayersLeaveRoom(room);
			// remove itself from the current list
			__rooms.remove(room.getId());
		}
	}

	private void __forceAllPlayersLeaveRoom(IRoom room) {
		final List<IPlayer> removePlayers = new ArrayList<IPlayer>();
		var players = room.getPlayers().values();
		players.forEach(player -> {
			removePlayers.add(player);
		});
		for (var player : removePlayers) {
			makePlayerLeaveRoom(player, true);
		}
		removePlayers.clear();
	}

	@Override
	public void sendPublicMessage(Room room, Player sender, ZeroObject message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendPrivateMessage(Player sender, Player recipient, ZeroObject message) {
		// TODO Auto-generated method stub

	}

	private EventManager __getEvent() {
		return __server.getEventManger().getInternal();
	}

	private PlayerManager __getPlayerManager() {
		return __server.getPlayerManager();
	}

	private RoomManager __getRoomManager() {
		return __server.getRoomManager();
	}

}
