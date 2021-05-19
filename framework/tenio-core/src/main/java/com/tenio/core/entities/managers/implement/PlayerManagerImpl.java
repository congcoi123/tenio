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
package com.tenio.core.entities.managers.implement;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.exceptions.DuplicatedPlayerException;
import com.tenio.core.exceptions.RemovedPlayerNotExistedException;
import com.tenio.core.network.entities.session.Session;

/**
 * @author kong
 */
// TODO: Add description
public final class PlayerManagerImpl implements PlayerManager {

	@GuardedBy("this")
	private final Map<Long, Player> __playerByIds;
	@GuardedBy("this")
	private final Map<String, Player> __playerByNames;
	@GuardedBy("this")
	private final Map<Session, Player> __playerBySessions;

	private Room __ownerRoom;

	private volatile int __playerCount;

	private PlayerManagerImpl() {
		__playerByIds = new HashMap<Long, Player>();
		__playerByNames = new HashMap<String, Player>();
		__playerBySessions = new HashMap<Session, Player>();

		__ownerRoom = null;

		__playerCount = 0;
	}

	@Override
	public Player getPlayerByName(String playerName) {
		synchronized (__playerByNames) {
			return __playerByNames.get(playerName);
		}
	}

	@Override
	public Player getPlayerById(long playerId) {
		synchronized (__playerByIds) {
			return __playerByIds.get(playerId);
		}
	}

	@Override
	public Player getPlayerBySession(Session session) {
		synchronized (__playerBySessions) {
			return __playerBySessions.get(session);
		}
	}

	@Override
	public Collection<Player> getAllPlayers() {
		synchronized (__playerByIds) {
			return __playerByIds.values();
		}
	}

	@Override
	public Collection<Session> getAllSessions() {
		synchronized (__playerBySessions) {
			return __playerBySessions.keySet();
		}
	}

	@Override
	public void addPlayer(Player player) {
		if (containsPlayer(player)) {
			throw new DuplicatedPlayerException(String.format("Unable to add player: %s, it already exists in room: %s",
					player.getName(), __ownerRoom.toString()));
		}

		synchronized (this) {
			__playerByIds.put(player.getId(), player);
			__playerByNames.put(player.getName(), player);
			__playerBySessions.put(player.getSession(), player);
			__playerCount = __playerByIds.size();
		}
	}

	@Override
	public void removePlayer(Player player) {
		if (player == null) {
			throw new RemovedPlayerNotExistedException(String
					.format("Unable to remove player, the player did not exist in room: %s", __ownerRoom.toString()));
		}

		__removePlayer(player);
	}

	private void __removePlayer(Player player) {
		synchronized (this) {
			__playerByIds.remove(player.getId());
			__playerByNames.remove(player.getName());
			__playerBySessions.remove(player.getSession());
			__playerCount = __playerByIds.size();
		}
	}

	@Override
	public void removePlayerByName(String playerName) {
		var player = getPlayerByName(playerName);
		if (player == null) {
			throw new RemovedPlayerNotExistedException(
					String.format("Unable to remove player: %s, the player did not exist in room: %s", playerName,
							__ownerRoom.toString()));
		}

		__removePlayer(player);
	}

	@Override
	public void removePlayerById(long playerId) {
		var player = getPlayerById(playerId);
		if (player == null) {
			throw new RemovedPlayerNotExistedException(
					String.format("Unable to remove player with id: %d, the player did not exist in room: %s", playerId,
							__ownerRoom.toString()));
		}

		__removePlayer(player);
	}

	@Override
	public void removePlayerBySession(Session session) {
		var player = getPlayerBySession(session);
		if (player == null) {
			throw new RemovedPlayerNotExistedException(
					String.format("Unable to remove player with session: %s, the player did not exist in room: %s",
							session.toString(), __ownerRoom.toString()));
		}

		__removePlayer(player);
	}

	@Override
	public void disconnectPlayer(Player player) throws IOException {
		if (player == null || !player.containsSession()) {
			throw new IllegalArgumentException(
					"Unable to disconnect player, the player does not exist or does not contain session");
		}

		player.getSession().close();
	}

	@Override
	public void disconnectPlayerByName(String playerName) throws IOException {
		var player = getPlayerByName(playerName);
		if (player == null) {
			throw new IllegalArgumentException(
					String.format("Unable to disconnect player: %s, the player does not exist", playerName));
		}
		if (!player.containsSession()) {
			throw new IllegalArgumentException(
					String.format("Unable to disconnect player: %s, the player does not contain session", playerName));
		}

		player.getSession().close();
	}

	@Override
	public void disconnectPlayerById(long playerId) throws IOException {
		var player = getPlayerById(playerId);
		if (player == null) {
			throw new IllegalArgumentException(
					String.format("Unable to disconnect player with id: %d, the player does not exist", playerId));
		}
		if (!player.containsSession()) {
			throw new IllegalArgumentException(String
					.format("Unable to disconnect player with id: %d, the player does not contain session", playerId));
		}

		player.getSession().close();
	}

	@Override
	public void disconnectPlayerBySession(Session session) throws IOException {
		if (session == null) {
			throw new IllegalArgumentException(
					String.format("Unable to disconnect player, the player's session does not exist"));
		}
		var player = getPlayerBySession(session);
		if (player == null) {
			throw new IllegalArgumentException(String.format(
					"Unable to disconnect player with session: %s, the player does not exist", session.toString()));
		}

		player.getSession().close();
	}

	@Override
	public boolean containsPlayer(Player player) {
		if (player == null) {
			return false;
		}
		synchronized (__playerByIds) {
			return __playerByIds.containsKey(player.getId());
		}
	}

	@Override
	public boolean containsPlayerId(long playerId) {
		synchronized (__playerByIds) {
			return __playerByIds.containsKey(playerId);
		}
	}

	@Override
	public boolean containsPlayerName(String playerName) {
		synchronized (__playerByNames) {
			return __playerByNames.containsKey(playerName);
		}
	}

	@Override
	public boolean containsPlayerSession(Session session) {
		synchronized (__playerBySessions) {
			return __playerBySessions.containsKey(session);
		}
	}

	@Override
	public Room getOwnerRoom() {
		return __ownerRoom;
	}

	@Override
	public void setOwnerRoom(Room room) {
		__ownerRoom = room;
	}

	@Override
	public int getPlayerCount() {
		return __playerCount;
	}

}
