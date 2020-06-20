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
package com.tenio.entity;

import java.util.HashMap;
import java.util.Map;

import com.tenio.annotation.Column;
import com.tenio.annotation.Entity;
import com.tenio.entity.manager.PlayerManager;

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
public abstract class AbstractRoom {

	/**
	 * List of reference players
	 */
	@Column(name = "players")
	private Map<String, AbstractPlayer> __players = new HashMap<String, AbstractPlayer>();
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
	/**
	 * To simple hold some states
	 */
	@Column(name = "state")
	private int __state;

	public AbstractRoom(final String id, final String name, final int capacity) {
		__id = id;
		__name = name;
		__capacity = capacity;
	}

	public boolean contain(final String playerName) {
		return __players.containsKey(playerName);
	}

	public AbstractPlayer getFirstPlayer() {
		if (isEmpty()) {
			return null;
		}
		return (AbstractPlayer) __players.values().stream().findFirst().get();
	}

	/**
	 * Add a player to this room, this action was handled by system logic. Be
	 * careful when handling it yourself. You are warned!
	 * 
	 * @param player the player ({@link AbstractPlayer}) who wants to join this room
	 */
	public void add(final AbstractPlayer player) {
		__players.put(player.getName(), player);
	}

	/**
	 * Remove a player from this room, this action was handled by system logic. Be
	 * careful when handling it yourself. You are warned!
	 * 
	 * @param player the player ({@link AbstractPlayer}) who wants to leave or be
	 *               forced to leave this room
	 */
	public void remove(final AbstractPlayer player) {
		__players.remove(player.getName());
	}

	/**
	 * Remove all players from this room
	 */
	public void clear() {
		__players.clear();
	}

	public boolean isState(final int state) {
		return (__state == state);
	}

	public int getState() {
		return __state;
	}

	public void setState(final int state) {
		__state = state;
	}

	public int getCapacity() {
		return __capacity;
	}

	public void setCapacity(final int capacity) {
		__capacity = capacity;
	}

	public String getId() {
		return __id;
	}

	public String getName() {
		return __name;
	}

	public void setName(final String name) {
		__name = name;
	}

	/**
	 * @return the number of players in this room
	 */
	public int count() {
		return __players.size();
	}

	public Map<String, AbstractPlayer> getPlayers() {
		return __players;
	}

	/**
	 * @return <b>true</b> if this room is full, <b>false</b> otherwise
	 */
	public boolean isFull() {
		// the counter starts from element 0, remember it
		return (count() >= __capacity);
	}

	public boolean isEmpty() {
		return __players.isEmpty();
	}

	/**
	 * Set a new state for all players in this room
	 * 
	 * @param state the desired state
	 */
	public void setPlayersState(final int state) {
		__players.values().stream().forEach(player -> {
			player.setState(state);
		});
	}

}
