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

import com.tenio.common.logger.ZeroLogger;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.entity.ZeroPlayer;
import com.tenio.core.entity.ZeroRoom;
import com.tenio.core.entity.manager.IPlayerManager;
import com.tenio.core.entity.manager.IRoomManager;
import com.tenio.core.exception.DuplicatedPlayerException;
import com.tenio.core.exception.NullPlayerNameException;
import com.tenio.core.network.entity.connection.Connection;

/**
 * This class provides you a necessary interface for managing players.
 * 
 * @see IPlayerManager
 * 
 * @author kong
 * 
 */
@ThreadSafe
public final class PlayerApi extends ZeroLogger {

	private final IPlayerManager __playerManager;
	private final IRoomManager __roomManager;

	public PlayerApi(IPlayerManager playerManager, IRoomManager roomManager) {
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
	public ZeroPlayer get(String name) {
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
	public Map<String, ZeroPlayer> gets() {
		return __playerManager.gets();
	}

	/**
	 * Add a new player to your server (this player was upgraded from one
	 * connection).
	 * 
	 * @param player     that is created from your server, see: {@link ZeroPlayer}
	 * @param connection the corresponding connection, see: {@link Connection}
	 */
	public void login(ZeroPlayer player, Connection connection) {
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
	 * @param player that is created from your server, see: {@link ZeroPlayer}
	 */
	public void login(ZeroPlayer player) {
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
	 * @param room   the desired room, see: {@link ZeroRoom}
	 * @param player the current player, see: {@link ZeroPlayer}
	 * @return the action' result if it existed in, see {@link CoreMessageCode},
	 *         <b>null</b> otherwise
	 */
	public CoreMessageCode makePlayerJoinRoom(ZeroRoom room, ZeroPlayer player) {
		return __roomManager.makePlayerJoinRoom(room, player);
	}

	/**
	 * Allow a player to leave his current room. You can handle your own logic in
	 * the corresponding events.
	 * 
	 * @param player that will be left his current room, see {@link ZeroRoom}
	 * @param force  it's set <b>true</b> if you want to force the player leave.
	 *               Otherwise, it's set <b>false</b>
	 * @return the action' result if it existed in, see {@link CoreMessageCode},
	 *         <b>null</b> otherwise
	 */
	public CoreMessageCode makePlayerLeaveRoom(ZeroPlayer player, boolean force) {
		return __roomManager.makePlayerLeaveRoom(player, force);
	}

	/**
	 * Remove a player from your server.
	 * 
	 * @param name the player with this name that is removed, see {@link ZeroPlayer}
	 */
	public void logOut(String name) {
		logOut(get(name));
	}

	/**
	 * Remove a player from your server.
	 * 
	 * @param player that is removed, see {@link ZeroPlayer}
	 */
	public void logOut(ZeroPlayer player) {
		__playerManager.remove(player);
	}

	/**
	 * @return the number of all current players' instance (include NPC or BOT)
	 */
	public int getCCU() {
		return count();
	}

}
