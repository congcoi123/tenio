/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.core.api;

import java.io.IOException;

import com.tenio.common.loggers.SystemLogger;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.data.ServerMessage;
import com.tenio.core.entities.defines.modes.ConnectionDisconnectMode;
import com.tenio.core.entities.defines.modes.PlayerBanMode;
import com.tenio.core.entities.defines.modes.PlayerDisconnectMode;
import com.tenio.core.entities.defines.modes.PlayerLeaveRoomMode;
import com.tenio.core.entities.defines.results.PlayerLoggedinResult;
import com.tenio.core.entities.defines.results.RoomCreatedResult;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.AddedDuplicatedPlayerException;
import com.tenio.core.exceptions.CreatedRoomException;
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
	public void login(String playerName) {
		try {
			var player = __getPlayerManager().createPlayer(playerName);

			__getEventManager().emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, player, PlayerLoggedinResult.SUCCESS);
		} catch (AddedDuplicatedPlayerException e) {
			error(e, "Loggedin with same player name: ", playerName);
			__getEventManager().emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, null, PlayerLoggedinResult.DUPPLICATED_PLAYER);
		}
	}

	@Override
	public void login(String playerName, Session session) {
		try {
			var player = __getPlayerManager().createPlayerWithSession(playerName, session);
			session.setName(playerName);
			session.setConnected(true);
			session.activate();

			__getEventManager().emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, player, PlayerLoggedinResult.SUCCESS);
		} catch (NullPointerException e) {
			error(e, "Unable to find session when loggedin with the player name: ", playerName);
			__getEventManager().emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, null, PlayerLoggedinResult.SESSION_NOT_FOUND);
		} catch (AddedDuplicatedPlayerException e) {
			error(e, "Loggedin with same player name: ", playerName);
			__getEventManager().emit(ServerEvent.PLAYER_LOGGEDIN_RESULT, null, PlayerLoggedinResult.DUPPLICATED_PLAYER);
		}
	}

	@Override
	public void logout(Player player) {
		if (player == null) {
			// maybe we needn't do anything
			return;
		}

		try {
			if (player.isInRoom()) {
				leaveRoom(player, PlayerLeaveRoomMode.LOG_OUT);
			}

			__disconnectPlayer(player);

			player = null;
		} catch (RemovedNonExistentPlayerException e) {
			error(e, "Removed player: ", player.getName(), " with issue");
		} catch (IOException e) {
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

	private void __disconnectPlayer(Player player) throws IOException {
		if (player.containsSession()) {
			player.getSession().close(ConnectionDisconnectMode.DEFAULT, PlayerDisconnectMode.DEFAULT);
		} else {
			__getEventManager().emit(ServerEvent.DISCONNECT_PLAYER, player, PlayerDisconnectMode.DEFAULT);
			__getPlayerManager().removePlayerByName(player.getName());
			player.clean();
			player = null;
		}
	}

	@Override
	public Room createRoom(InitialRoomSetting setting) {
		return createRoom(setting, null);
	}

	@Override
	public Room createRoom(InitialRoomSetting setting, Player owner) {
		Room room = null;
		try {
			if (owner == null) {
				room = __getRoomManager().createRoom(setting);
			} else {
				room = __getRoomManager().createRoomWithOwner(setting, owner);
				__getEventManager().emit(ServerEvent.ROOM_CREATED_RESULT, room, setting, RoomCreatedResult.SUCCESS);
			}
		} catch (IllegalArgumentException e) {
			__getEventManager().emit(ServerEvent.ROOM_CREATED_RESULT, null, setting,
					RoomCreatedResult.INVALID_NAME_OR_PASSWORD);
		} catch (CreatedRoomException e) {
			__getEventManager().emit(ServerEvent.ROOM_CREATED_RESULT, null, setting, e.getResult());
		}

		return room;
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
	public Room getRoomById(long roomId) {
		return __getRoomManager().getRoomById(roomId);
	}

	@Override
	public void joinRoom(Player player, Room room, String roomPassword, int slotInRoom, boolean asSpectator) {
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
	public void joinRoom(Player player, Room room) {

	}

	@Override
	public void switchPlayerToSpectator(Player player, Room room) {

	}

	@Override
	public void switchSpectatorToPlayer(Player player, Room room, int targetSlot) {

	}

	@Override
	public void leaveRoom(Player player, PlayerLeaveRoomMode leaveRoomMode) {
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
	public void sendPublicMessage(Player sender, Room room, ServerMessage message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendPrivateMessage(Player sender, Player recipient, ServerMessage message) {
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
