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
import java.util.concurrent.atomic.AtomicLong;

import com.tenio.common.utilities.TimeUtility;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.PlayerState;
import com.tenio.core.entities.Room;
import com.tenio.core.network.entities.session.Session;

/**
 * @author kong
 */
// TODO: Add description
public final class PlayerImpl implements Player {

	private static AtomicLong __idCounter = new AtomicLong();

	private final long __id;
	private final String __name;

	private Session __session;

	private volatile Room __currentRoom;
	private volatile ZoneImpl __currentZone;

	private final Map<String, Object> __properties;
	private volatile PlayerState __state;

	private volatile long __lastLoginedTime;
	private volatile long __lastJoinedRoomTime;
	private volatile int __playerSlotInCurrentRoom;

	private volatile boolean __loggedIn;
	private volatile boolean __activated;
	private volatile boolean __npc;
	private volatile boolean __spectator;

	public static Player newInstance(String name) {
		return new PlayerImpl(name);
	}

	public static Player newInstance(String name, Session session) {
		return new PlayerImpl(name, session);
	}

	private PlayerImpl(String name) {
		this(name, null);
		__npc = true;
	}

	private PlayerImpl(String name, Session session) {
		__id = __idCounter.getAndIncrement();
		__name = name;
		__session = session;

		__currentRoom = null;
		__currentZone = null;

		__properties = new ConcurrentHashMap<String, Object>();

		__lastLoginedTime = 0L;
		__lastJoinedRoomTime = 0L;
		__playerSlotInCurrentRoom = -1;

		__loggedIn = false;
		__activated = false;
		__npc = false;
		__spectator = true;
	}

	@Override
	public long getId() {
		return __id;
	}

	@Override
	public String getName() {
		return __name;
	}

	@Override
	public boolean isNpc() {
		return __npc;
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
			__npc = true;
		}
	}

	@Override
	public boolean isInRoom() {
		return __playerSlotInCurrentRoom >= 0L;
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
			__playerSlotInCurrentRoom = -1;
		} else {
			__setLastJoinedRoomTime();
		}
	}

	@Override
	public long getLastJoinedRoomTime() {
		return __lastJoinedRoomTime;
	}

	private void __setLastJoinedRoomTime() {
		__lastJoinedRoomTime = TimeUtility.currentTimeMillis();
	}

	@Override
	public long getPlayerSlotInCurrentRoom() {
		return __playerSlotInCurrentRoom;
	}

	@Override
	public void setPlayerSlotInCurrentRoom(int slot) {
		__playerSlotInCurrentRoom = slot;
	}

	@Override
	public ZoneImpl getCurrentZone() {
		return __currentZone;
	}

	@Override
	public void setCurrentZone(ZoneImpl zone) {
		__currentZone = zone;
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
	public boolean equals(Object player) {
		if (!(player instanceof Player)) {
			return false;
		} else {
			return getId() == ((Player) player).getId();
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return String.format("{ id: %d, name: %s }", __id, __name);
	}

}
