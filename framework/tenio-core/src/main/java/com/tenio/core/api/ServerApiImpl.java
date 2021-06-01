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
import java.util.Collection;

import com.tenio.common.loggers.SystemLogger;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.defines.modes.ConnectionDisconnectMode;
import com.tenio.core.entities.defines.modes.PlayerDisconnectMode;
import com.tenio.core.entities.defines.modes.PlayerLeaveRoomMode;
import com.tenio.core.entities.defines.modes.RoomRemoveMode;
import com.tenio.core.entities.defines.results.PlayerJoinedRoomResult;
import com.tenio.core.entities.defines.results.PlayerLeftRoomResult;
import com.tenio.core.entities.defines.results.PlayerLoggedinResult;
import com.tenio.core.entities.defines.results.RoomCreatedResult;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.AddedDuplicatedPlayerException;
import com.tenio.core.exceptions.CreatedRoomException;
import com.tenio.core.exceptions.PlayerJoinedRoomException;
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
	public int getPlayerCount() {
		return __getPlayerManager().getPlayerCount();
	}

	@Override
	public Collection<Player> getAllPlayers() {
		return __getPlayerManager().getAllPlayers();
	}

	@Override
	public Room getRoomById(long roomId) {
		return __getRoomManager().getRoomById(roomId);
	}

	@Override
	public void joinRoom(Player player, Room room, String roomPassword, int slotInRoom, boolean asSpectator) {
		if (player == null || room == null) {
			__getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
					PlayerJoinedRoomResult.PLAYER_OR_ROOM_UNAVAILABLE);
			return;
		}

		if (player.isInRoom()) {
			__getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
					PlayerJoinedRoomResult.PLAYER_IS_IN_ANOTHER_ROOM);
			return;
		}

		try {
			room.addPlayer(player, asSpectator, slotInRoom);
			player.setCurrentRoom(room);
			__getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
					PlayerJoinedRoomResult.SUCCESS);
		} catch (PlayerJoinedRoomException e) {
			__getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room, e.getResult());
		} catch (AddedDuplicatedPlayerException e) {
			error(e, e.getMessage());
			__getEventManager().emit(ServerEvent.PLAYER_JOINED_ROOM_RESULT, player, room,
					PlayerJoinedRoomResult.DUPLICATED_PLAYER);
		}

	}

	@Override
	public void leaveRoom(Player player, PlayerLeaveRoomMode leaveRoomMode) {
		if (!player.isInRoom()) {
			__getEventManager().emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, null,
					PlayerLeftRoomResult.PLAYER_ALREADY_LEFT_ROOM);
			return;
		}

		var room = player.getCurrentRoom();

		__getEventManager().emit(ServerEvent.PLAYER_BEFORE_LEAVE_ROOM, player, room, leaveRoomMode);

		try {
			room.removePlayer(player);
			__getEventManager().emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, room, PlayerLeftRoomResult.SUCCESS);
		} catch (RemovedNonExistentPlayerException e) {
			__getEventManager().emit(ServerEvent.PLAYER_AFTER_LEFT_ROOM, player, room,
					PlayerLeftRoomResult.PLAYER_ALREADY_LEFT_ROOM);
		}

	}

	@Override
	public void removeRoom(Room room, RoomRemoveMode removeRoomMode) {
		if (room == null) {
			// nothing needs to do
			return;
		}

		__getEventManager().emit(ServerEvent.ROOM_WILL_BE_REMOVED, room, removeRoomMode);

		var players = room.getAllPlayersList();
		var iterator = players.iterator();

		while (iterator.hasNext()) {
			var player = iterator.next();
			leaveRoom(player, PlayerLeaveRoomMode.ROOM_REMOVED);
		}

		__getRoomManager().removeRoomById(room.getId());

		room = null;

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
