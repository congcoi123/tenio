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
import com.tenio.core.entities.implement.PlayerImpl;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.AddedDuplicatedPlayerException;
import com.tenio.core.exceptions.RemovedNonExistentPlayerException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.entities.session.Session;

public final class PlayerManagerImpl extends AbstractManager implements PlayerManager {

	@GuardedBy("this")
	private final Map<String, Player> __playerByNames;
	@GuardedBy("this")
	private final Map<Session, Player> __playerBySessions;

	private Room __ownerRoom;

	private volatile int __playerCount;

	public static PlayerManager newInstance(EventManager eventManager) {
		return new PlayerManagerImpl(eventManager);
	}

	private PlayerManagerImpl(EventManager eventManager) {
		super(eventManager);

		__playerByNames = new HashMap<String, Player>();
		__playerBySessions = new HashMap<Session, Player>();

		__ownerRoom = null;

		__playerCount = 0;
	}

	@Override
	public void addPlayer(Player player) {
		if (containsPlayerName(player.getName())) {
			throw new AddedDuplicatedPlayerException(player, __ownerRoom);
		}

		synchronized (this) {
			__playerByNames.put(player.getName(), player);
			if (player.containsSession()) {
				__playerBySessions.put(player.getSession(), player);
			}
			__playerCount = __playerByNames.size();
		}
	}

	@Override
	public Player createPlayer(String name) {
		Player newPlayer = PlayerImpl.newInstance(name);
		newPlayer.setActivated(true);
		newPlayer.setLoggedIn(true);

		addPlayer(newPlayer);

		return newPlayer;
	}

	@Override
	public Player createPlayerWithSession(String name, Session session) {
		if (session == null) {
			throw new NullPointerException("Unable to assign a null session for the player");
		}

		Player newPlayer = PlayerImpl.newInstance(name, session);
		newPlayer.setActivated(true);
		newPlayer.setLoggedIn(true);

		addPlayer(newPlayer);

		return newPlayer;
	}

	@Override
	public Player getPlayerByName(String playerName) {
		synchronized (__playerByNames) {
			return __playerByNames.get(playerName);
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
		synchronized (__playerByNames) {
			return __playerByNames.values();
		}
	}

	@Override
	public Collection<Session> getAllSessions() {
		synchronized (__playerBySessions) {
			return __playerBySessions.keySet();
		}
	}

	@Override
	public void removePlayerByName(String playerName) {
		var player = getPlayerByName(playerName);
		if (player == null) {
			throw new RemovedNonExistentPlayerException(playerName, __ownerRoom);
		}

		__removePlayer(player);
	}

	@Override
	public void removePlayerBySession(Session session) {
		var player = getPlayerBySession(session);
		if (player == null) {
			throw new RemovedNonExistentPlayerException(session.toString(), __ownerRoom);
		}

		__removePlayer(player);
	}

	private void __removePlayer(Player player) {
		synchronized (this) {
			__playerByNames.remove(player.getName());
			if (player.containsSession()) {
				__playerBySessions.remove(player.getSession());
			}
			__playerCount = __playerByNames.size();
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

	@Override
	public void clear() {
		synchronized (this) {
			var iterator = __playerByNames.values().iterator();
			while (iterator.hasNext()) {
				var player = iterator.next();
				__removePlayer(player);
			}
		}
	}

}
