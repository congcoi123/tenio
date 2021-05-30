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
package com.tenio.core.entities.implement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tenio.common.utilities.TimeUtility;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.PlayerState;
import com.tenio.core.entities.Room;
import com.tenio.core.network.entities.session.Session;

public final class PlayerImpl implements Player {

	private final String __name;

	private volatile Session __session;

	private volatile Room __currentRoom;

	private final Map<String, Object> __properties;
	private volatile PlayerState __state;

	private volatile long __lastLoginedTime;
	private volatile long __lastJoinedRoomTime;
	private volatile int __playerSlotInCurrentRoom;

	private volatile boolean __loggedIn;
	private volatile boolean __activated;
	private volatile boolean __spectator;
	private volatile boolean __hasSession;

	public static Player newInstance(String name) {
		return new PlayerImpl(name);
	}

	public static Player newInstance(String name, Session session) {
		return new PlayerImpl(name, session);
	}

	private PlayerImpl(String name) {
		this(name, null);
	}

	private PlayerImpl(String name, Session session) {
		__name = name;

		__properties = new ConcurrentHashMap<String, Object>();

		__lastLoginedTime = 0L;
		__lastJoinedRoomTime = 0L;

		setCurrentRoom(null);
		setSession(session);
		setLoggedIn(false);
		setActivated(false);
		setSpectator(true);
	}

	@Override
	public String getName() {
		return __name;
	}

	@Override
	public boolean containsSession() {
		return __hasSession;
	}

	@Override
	public boolean isState(PlayerState state) {
		return __state == state;
	}

	@Override
	public PlayerState getState() {
		return __state;
	}

	@Override
	public void setState(PlayerState state) {
		__state = state;
	}

	@Override
	public boolean isActivated() {
		return __activated;
	}

	@Override
	public void setActivated(boolean activated) {
		__activated = activated;
	}

	@Override
	public boolean isLoggedIn() {
		return __loggedIn;
	}

	@Override
	public void setLoggedIn(boolean loggedIn) {
		__loggedIn = loggedIn;
		if (__loggedIn) {
			__setLastLoggedInTime();
		}
	}

	@Override
	public long getLastLoggedInTime() {
		return __lastLoginedTime;
	}

	private void __setLastLoggedInTime() {
		__lastLoginedTime = TimeUtility.currentTimeMillis();
	}

	@Override
	public Session getSession() {
		return __session;
	}

	@Override
	public void setSession(Session session) {
		__session = session;
		if (__session == null) {
			__hasSession = false;
		} else {
			__hasSession = true;
		}
	}

	@Override
	public boolean isInRoom() {
		return __playerSlotInCurrentRoom >= RoomImpl.NIL_SLOT;
	}

	@Override
	public boolean isSpectator() {
		return __spectator;
	}

	@Override
	public void setSpectator(boolean spectator) {
		__spectator = spectator;
	}

	@Override
	public Room getCurrentRoom() {
		return __currentRoom;
	}

	@Override
	public void setCurrentRoom(Room room) {
		__currentRoom = room;
		if (__currentRoom == null) {
			__playerSlotInCurrentRoom = RoomImpl.NIL_SLOT;
		} else {
			__playerSlotInCurrentRoom = RoomImpl.DEFAULT_SLOT;
		}
		__setLastJoinedRoomTime();
	}

	@Override
	public long getLastJoinedRoomTime() {
		return __lastJoinedRoomTime;
	}

	private void __setLastJoinedRoomTime() {
		__lastJoinedRoomTime = TimeUtility.currentTimeMillis();
	}

	@Override
	public int getPlayerSlotInCurrentRoom() {
		return __playerSlotInCurrentRoom;
	}

	@Override
	public void setPlayerSlotInCurrentRoom(int slot) {
		__playerSlotInCurrentRoom = slot;
	}

	@Override
	public Object getProperty(String key) {
		return __properties.get(key);
	}

	@Override
	public void setProperty(String key, Object value) {
		__properties.put(key, value);
	}

	@Override
	public boolean containsProperty(String key) {
		return __properties.containsKey(key);
	}

	@Override
	public void removeProperty(String key) {
		__properties.remove(key);
	}

	@Override
	public void clearProperties() {
		__properties.clear();
	}

	@Override
	public void clean() {
		setActivated(false);
		setCurrentRoom(null);
		setSession(null);
		clearProperties();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Player)) {
			return false;
		} else {
			Player player = (Player) object;
			return getName().equals(player.getName());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((__name == null) ? 0 : __name.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return String.format("{ name: %s, session: %b, loggedin: %b, spectator: %b, activated: %b }", __name,
				__hasSession, __loggedIn, __spectator, __activated);
	}

}
