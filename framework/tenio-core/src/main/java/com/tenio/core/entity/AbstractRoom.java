/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.core.entity;

import java.util.HashMap;
import java.util.Map;

import com.tenio.core.entity.annotation.Column;
import com.tenio.core.entity.annotation.Entity;
import com.tenio.core.entity.manager.PlayerManager;

/**
 * A room or simpler is a group of related players, see {@link AbstractPlayer}.
 * These players can be played in the same game or in the same location. This
 * class is only for logic handling. You can manage a list of players in a room
 * as well as hold the players' common data for sharing. For simple handling,
 * one room can hold a number of players, but one player only appears in one
 * room at a time. The player can be a free one and no need to join any rooms,
 * there, it is only under the {@link PlayerManager} class' management.
 * 
 * @author kong
 * 
 */
@Entity
public abstract class AbstractRoom implements IRoom {

	/**
	 * List of reference players
	 */
	@Column(name = "players")
	private Map<String, IPlayer> __players;
	/**
	 * Each room has its own unique id
	 */
	@Column(name = "id")
	private String __id;
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

	public AbstractRoom(final String id, final String name, final int capacity) {
		__id = id;
		__name = name;
		__capacity = capacity;
		__players = new HashMap<String, IPlayer>();
	}

	@Override
	public boolean contain(final String playerName) {
		return __players.containsKey(playerName);
	}

	@Override
	public IPlayer getFirstPlayer() {
		if (isEmpty()) {
			return null;
		}
		return (IPlayer) __players.values().stream().findFirst().get();
	}

	@Override
	public void add(final IPlayer player) {
		__players.put(player.getName(), player);
	}

	@Override
	public void remove(final IPlayer player) {
		__players.remove(player.getName());
	}

	@Override
	public void clear() {
		__players.clear();
	}

	@Override
	public int getCapacity() {
		return __capacity;
	}

	@Override
	public void setCapacity(final int capacity) {
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
	public void setName(final String name) {
		__name = name;
	}

	@Override
	public int count() {
		return __players.size();
	}

	@Override
	public Map<String, IPlayer> getPlayers() {
		return __players;
	}

	@Override
	public boolean isFull() {
		// the counter starts from element 0, remember it
		return (count() >= __capacity);
	}

	@Override
	public boolean isEmpty() {
		return __players.isEmpty();
	}

}
