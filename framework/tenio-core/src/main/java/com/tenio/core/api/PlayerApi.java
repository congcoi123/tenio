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
package com.tenio.core.api;

import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.exception.DuplicatedPlayerException;
import com.tenio.core.exception.NullPlayerNameException;
import com.tenio.core.network.entity.connection.Connection;

/**
 * This class provides you a necessary interface for managing players.
 * 
 * @see PlayerManager
 * 
 * @author kong
 * 
 */
@ThreadSafe
public final class PlayerApi extends AbstractLogger {

	private final PlayerManager __playerManager;
	private final RoomManager __roomManager;

	public PlayerApi(PlayerManager playerManager, RoomManager roomManager) {
		__playerManager = playerManager;
		__roomManager = roomManager;
	}

	/**
	 * Determine if the player has existed or not.
	 * 
	 * @param name the player's name (unique ID)
	 * @return <b>true</b> if the player has existed, <b>false</b> otherwise
	 */
	public boolean contain(String name) {
		return __playerManager.contain(name);
	}

	/**
	 * Retrieve a player by the player's name.
	 * 
	 * @param name the player's name (unique ID)
	 * @return the player's instance if that player has existed, <b>null</b>
	 *         otherwise
	 */
	public Player get(String name) {
		return __playerManager.get(name);
	}

	/**
	 * @return the number of all current players' instance (include NPC or BOT)
	 */
	public int count() {
		return __playerManager.count();
	}

	/**
	 * @return the number of all current players that have connections (without NPC
	 *         or BOT)
	 */
	public int countPlayers() {
		return __playerManager.countPlayers();
	}

	/**
	 * @return all current players
	 */
	public Map<String, Player> gets() {
		return __playerManager.gets();
	}

	/**
	 * Add a new player to your server (this player was upgraded from one
	 * connection).
	 * 
	 * @param player     that is created from your server, see: {@link Player}
	 * @param connection the corresponding connection, see: {@link Connection}
	 */
	public void login(Player player, Connection connection) {
		try {
			__playerManager.add(player, connection);
		} catch (NullPlayerNameException e1) {
			_error(e1);
		} catch (DuplicatedPlayerException e2) {
			_error(e2, e2.getMessage());
		}
	}

	/**
	 * Add a new player to your server (this player is known as one NCP or a BOT)
	 * without a attached connection.
	 * 
	 * @param player that is created from your server, see: {@link Player}
	 */
	public void login(Player player) {
		try {
			__playerManager.add(player);
		} catch (DuplicatedPlayerException e) {
			_error(e, e.getMessage());
		}
	}

	/**
	 * Request one player to join a room. This request can be refused with some
	 * reason. You can handle these results in the corresponding events.
	 * 
	 * @param room   the desired room, see: {@link Room}
	 * @param player the current player, see: {@link Player}
	 * @return the action' result if it existed in, see {@link CoreMessageCode},
	 *         <b>null</b> otherwise
	 */
	public CoreMessageCode makePlayerJoinRoom(Room room, Player player) {
		return __roomManager.makePlayerJoinRoom(room, player);
	}

	/**
	 * Allow a player to leave his current room. You can handle your own logic in
	 * the corresponding events.
	 * 
	 * @param player that will be left his current room, see {@link Room}
	 * @param force  it's set <b>true</b> if you want to force the player leave.
	 *               Otherwise, it's set <b>false</b>
	 * @return the action' result if it existed in, see {@link CoreMessageCode},
	 *         <b>null</b> otherwise
	 */
	public CoreMessageCode makePlayerLeaveRoom(Player player, boolean force) {
		return __roomManager.makePlayerLeaveRoom(player, force);
	}

	/**
	 * Remove a player from your server.
	 * 
	 * @param name the player with this name that is removed, see {@link Player}
	 */
	public void logOut(String name) {
		logOut(get(name));
	}

	/**
	 * Remove a player from your server.
	 * 
	 * @param player that is removed, see {@link Player}
	 */
	public void logOut(Player player) {
		__playerManager.remove(player);
	}

	/**
	 * @return the number of all current players' instance (include NPC or BOT)
	 */
	public int getCCU() {
		return count();
	}

}
