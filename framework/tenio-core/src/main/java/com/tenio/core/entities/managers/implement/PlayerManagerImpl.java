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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.Zone;
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
	private Zone __ownerZone;

	private PlayerManagerImpl() {
		__playerByIds = new HashMap<Long, Player>();
		__playerByNames = new HashMap<String, Player>();
		__playerBySessions = new HashMap<Session, Player>();

		__ownerRoom = null;
		__ownerZone = null;
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
			throw new DuplicatedPlayerException(
					String.format("Unable to add player: %s, it already exists in room: %s, zone: %s", player.getName(),
							__ownerRoom.toString(), __ownerZone.toString()));
		}

		synchronized (this) {
			__playerByIds.put(player.getId(), player);
			__playerByNames.put(player.getName(), player);
			__playerBySessions.put(player.getSession(), player);
		}
	}

	@Override
	public void removePlayer(Player player) {
		if (!containsPlayer(player)) {
			throw new RemovedPlayerNotExistedException(
					String.format("Unable to remove player: %s, the player did not exist in room: %s, zone: %s",
							player.getName(), __ownerRoom.toString(), __ownerZone.toString()));
		}

		__removePlayer(player);
	}

	private void __removePlayer(Player player) {
		synchronized (this) {
			__playerByIds.remove(player.getId());
			__playerByNames.remove(player.getName());
			__playerBySessions.remove(player.getSession());
		}
	}

	@Override
	public void removePlayerByName(String playerName) {

	}

	@Override
	public void removePlayerById(long playerId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePlayerBySession(Session session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnectPlayer(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnectPlayerByName(String playerName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnectPlayerById(long playerId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnectPlayerBySession(Session session) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsPlayer(Player player) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsPlayerId(long playerId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsPlayerName(String playerName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsPlayerSession(Session session) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Zone getOwnerZone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOwnerZone(Zone zone) {
		// TODO Auto-generated method stub

	}

	@Override
	public Room getOwnerRoom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOwnerRoom(Room room) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPlayerCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
