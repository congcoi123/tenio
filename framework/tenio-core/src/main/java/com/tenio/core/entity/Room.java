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
package com.tenio.core.entity;

import java.util.Map;

import com.tenio.core.entity.implement.AbstractPlayer;
import com.tenio.core.entity.manager.implement.PlayerManagerImpl;

/**
 * A room or simpler is a group of related players, see {@link AbstractPlayer}.
 * These players can be played in the same game or in the same location. This
 * class is only for logic handling. You can manage a list of players in a room
 * as well as hold the players' common data for sharing. For simple handling,
 * one room can hold a number of players, but one player only appears in one
 * room at a time. The player can be a free one and no need to join any rooms,
 * there, it is only under the {@link PlayerManagerImpl} class' management.
 * 
 * @author kong
 * 
 */
public interface Room {

	boolean containPlayerName(String playerName);

	/**
	 * Retrieve a first player in this room. Remember that it doesn't guarantee the
	 * order. We let a player escape from its scope, so it is necessary to make sure
	 * that the player after process is thread-safe.
	 * 
	 * @return the player ({@link Player})
	 */
	Player getFirstPlayer();

	/**
	 * Add a player to this room, this action was handled by system logic. Be
	 * careful when handling it yourself. You are warned!
	 * 
	 * @param player the player ({@link Player}) who wants to join this room
	 */
	void addPlayer(Player player);

	/**
	 * Remove a player from this room, this action was handled by system logic. Be
	 * careful when handling it yourself. You are warned!
	 * 
	 * @param player the player ({@link Player}) who wants to leave or be forced to
	 *               leave this room
	 */
	void removePlayer(Player player);

	/**
	 * Remove all players from this room
	 */
	void removeAllPlayers();

	int getCapacity();

	void setCapacity(int capacity);

	String getId();

	String getName();

	void setName(String name);

	/**
	 * @return the current number of players in this room
	 */
	int countPlayers();

	/**
	 * We let a player escape from its scope, so it is necessary to make sure that
	 * the player after process is thread-safe.
	 * 
	 * @return a synchronized list of players
	 */
	public Map<String, Player> getPlayers();

	/**
	 * @return <b>true</b> if this room is full, <b>false</b> otherwise
	 */
	boolean isFull();

	boolean isEmpty();

}