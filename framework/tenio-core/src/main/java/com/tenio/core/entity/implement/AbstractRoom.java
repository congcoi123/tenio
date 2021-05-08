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
package com.tenio.core.entity.implement;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.backup.annotation.Column;
import com.tenio.core.entity.backup.annotation.Entity;
import com.tenio.core.entity.manager.implement.PlayerManagerImpl;

/**
 * A room or simpler is a group of related players, see {@link Player}. These
 * players can be played in the same game or in the same location. This class is
 * only for logic handling. You can manage a list of players in a room as well
 * as hold the players' common data for sharing. For simple handling, one room
 * can hold a number of players, but one player only appears in one room at a
 * time. The player can be a free one and no need to join any rooms, there, it
 * is only under the {@link PlayerManagerImpl} class' management.
 * 
 * @author kong
 * 
 */
@Entity
@ThreadSafe
public abstract class AbstractRoom implements Room {

	/**
	 * List of reference players
	 */
	@Column(name = "players")
	private final Map<String, Player> __players;
	/**
	 * Each room has its own unique id
	 */
	@Column(name = "id")
	private final String __id;
	/**
	 * Should be set a human-readable name
	 */
	@Column(name = "name")
	private String __name;
	/**
	 * The maximum of players that this room can handle
	 */
	@Column(name = "capacity")
	private int __capacity;

	private volatile int __countPlayers;

	public AbstractRoom(String id, String name, int capacity) {
		__id = id;
		__name = name;
		__capacity = capacity;
		__players = new HashMap<String, Player>();
		__countPlayers = 0;
	}

	@Override
	public boolean containPlayerName(String playerName) {
		synchronized (__players) {
			return __players.containsKey(playerName);
		}
	}

	@Override
	public Player getFirstPlayer() {
		if (isEmpty()) {
			return null;
		}
		synchronized (__players) {
			return (Player) __players.values().stream().findFirst().get();
		}
	}

	@Override
	public void addPlayer(Player player) {
		synchronized (__players) {
			__players.put(player.getName(), player);
			__countPlayers = __players.size();
		}
	}

	@Override
	public void removePlayer(Player player) {
		synchronized (__players) {
			__players.remove(player.getName());
			__countPlayers = __players.size();
		}
	}

	@Override
	public synchronized void removeAllPlayers() {
		synchronized (__players) {
			__players.clear();
			__countPlayers = __players.size();
		}
	}

	@Override
	public int getCapacity() {
		return __capacity;
	}

	@Override
	public void setCapacity(int capacity) {
		__capacity = capacity;
	}

	@Override
	public String getId() {
		return __id;
	}

	@Override
	public String getName() {
		return __name;
	}

	@Override
	public void setName(String name) {
		__name = name;
	}

	@Override
	public int countPlayers() {
		return __countPlayers;
	}

	@Override
	public Map<String, Player> getPlayers() {
		synchronized (__players) {
			return __players;
		}
	}

	@Override
	public boolean isFull() {
		return (countPlayers() >= __capacity);
	}

	@Override
	public boolean isEmpty() {
		return (countPlayers() == 0);
	}

	@Override
	public String toString() {
		return __name;
	}

}