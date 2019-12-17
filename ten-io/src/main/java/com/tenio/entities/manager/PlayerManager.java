/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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
package com.tenio.entities.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.tenio.configuration.constant.ErrorMsg;
import com.tenio.configuration.constant.LogicEvent;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.AbstractPlayer;
import com.tenio.event.EventManager;
import com.tenio.exception.DuplicatedElementException;
import com.tenio.logger.AbstractLogger;
import com.tenio.net.Connection;

/**
 * Manage all your players @see {@link AbstractPlayer} on the server. It is a
 * singleton pattern class, which can be called anywhere. But it's better that
 * you use the {@link PlayerApi} interface for easy management.
 * 
 * @author kong
 * 
 */
public final class PlayerManager extends AbstractLogger {

	/**
	 * A map object to manage your players with the key must be a player's name
	 */
	private Map<String, AbstractPlayer> __players = new HashMap<String, AbstractPlayer>();

	/**
	 * @return the number of all current players instance (include bots)
	 */
	public int count() {
		return __players.size();
	}

	/**
	 * @return the number of all current players that have connection (without bots)
	 */
	public int countPlayers() {
		AtomicInteger ordinal = new AtomicInteger(0);
		__players.forEach((key, value) -> {
			if (!value.isNPC()) {
				ordinal.incrementAndGet();
			}
		});
		return ordinal.get();
	}

	/**
	 * @return all current players
	 */
	public Map<String, AbstractPlayer> gets() {
		return __players;
	}

	public void clear() {
		__players.clear();
		__players = null;
	}

	public boolean contain(final String name) {
		return __players.containsKey(name);
	}

	public AbstractPlayer get(final String name) {
		return __players.get(name);
	}

	/**
	 * Add a new player to your server (the player is upgraded from one connection).
	 * 
	 * @param player     that is created from your server @see
	 *                   {@link AbstractPlayer}
	 * @param connection the corresponding connection @see {@link Connection}
	 */
	public void add(final AbstractPlayer player, final Connection connection) {
		try {
			if (player.getName() == null) {
				throw new NullPointerException();
			}
			if (contain(player.getName())) {
				throw new DuplicatedElementException();
			}
		} catch (DuplicatedElementException e) {
			// fire event
			EventManager.getEvent().emit(TEvent.PLAYER_IN_FAILED, player, ErrorMsg.PLAYER_IS_EXISTED);
			error("ADD PLAYER CONNECTION", player.getName(), e);
			return;
		} catch (NullPointerException e) {
			// fire event
			EventManager.getEvent().emit(TEvent.PLAYER_IN_FAILED, player, ErrorMsg.PLAYER_IS_INVALID);
			error("ADD PLAYER CONNECTION", player.getName(), e);
			return;
		}

		connection.setId(player.getName());
		player.setConnection(connection);

		__players.put(player.getName(), player);

		// fire event
		EventManager.getEvent().emit(TEvent.PLAYER_IN_SUCCESS, player);
	}

	/**
	 * Add a new player to your server (the player is known as one bot) without a
	 * attached connection.
	 * 
	 * @param player that is created from your server @see {@link AbstractPlayer}
	 */
	public void add(final AbstractPlayer player) {
		try {
			if (contain(player.getName())) {
				throw new DuplicatedElementException();
			}
		} catch (DuplicatedElementException e) {
			// fire event
			EventManager.getEvent().emit(TEvent.PLAYER_IN_FAILED, player, ErrorMsg.PLAYER_IS_EXISTED);
			error("ADD PLAYER", player.getName(), e);
			return;
		}

		__players.put(player.getName(), player);

		// fire event
		EventManager.getEvent().emit(TEvent.PLAYER_IN_SUCCESS, player);
	}

	/**
	 * Remove a player from your server.
	 * 
	 * @param player that is removed @see {@link AbstractPlayer}
	 */
	public void remove(final AbstractPlayer player) {
		if (player == null || !contain(player.getName())) {
			return;
		}

		// force player leave room
		EventManager.getLogic().emit(LogicEvent.FORCE_PLAYER_LEAVE_ROOM, player);

		// remove connection, player
		if (player.hasConnection()) {
			player.getConnection().close();
		}
		// remove sub-connection (no need to close, because of the UDP behavior)
		/*
		 * if (player.hasSubConnection()) {
		 * 
		 * }
		 */
		clearConnections(player);
		__players.remove(player.getName());

	}

	/**
	 * When a player is disconnected, all the related connections need to be deleted
	 * too.
	 * 
	 * @param player the corresponding player @see {@link AbstractPlayer}
	 */
	public void clearConnections(final AbstractPlayer player) {
		player.setConnection(null);
		player.setSubConnection(null);
	}

	/**
	 * Make sure one player is removed from this management (as well as your
	 * server). It is used when you don't want your player can re-connect with any
	 * interruption's reason.
	 * 
	 * @param player that is removed @see {@link AbstractPlayer}
	 */
	public void clean(final AbstractPlayer player) {
		if (player == null || !contain(player.getName())) {
			return;
		}

		__players.remove(player.getName());
		
	}

}
