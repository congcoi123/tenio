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
package com.tenio.core.entity.manager.implement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.api.PlayerApi;
import com.tenio.core.configuration.constant.CoreConstants;
import com.tenio.core.configuration.data.SocketConfig;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.configuration.define.ZeroEvent;
import com.tenio.core.configuration.define.InternalEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.EventManager;
import com.tenio.core.exception.DuplicatedPlayerException;
import com.tenio.core.exception.NullPlayerNameException;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.connection.Connection;

/**
 * Manage all your players ({@link Player}) on the server. It is a singleton
 * pattern class, which can be called anywhere. But it's better that you use the
 * {@link PlayerApi} interface for easy management.
 * 
 * @see PlayerManager
 * 
 * @author kong
 * 
 */
@ThreadSafe
public final class PlayerManagerImpl implements PlayerManager {

	/**
	 * A map object to manage your players with the key must be a player's name
	 */
	private final Map<String, Player> __players;
	private final EventManager __eventManager;
	private Configuration __configuration;
	private int __socketPortsSize;
	private int __webSocketPortsSize;
	private volatile int __count;
	private volatile int __countPlayers;

	public PlayerManagerImpl(EventManager eventManager) {
		__eventManager = eventManager;
		__players = new HashMap<String, Player>();
		__count = 0;
		__countPlayers = 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void initialize(Configuration configuration) {
		__configuration = configuration;
		var socketPorts = (List<SocketConfig>) (__configuration.get(CoreConfigurationType.SOCKET_PORTS));
		var webSocketPorts = (List<SocketConfig>) (__configuration.get(CoreConfigurationType.WEBSOCKET_PORTS));
		__socketPortsSize = socketPorts.size();
		__webSocketPortsSize = webSocketPorts.size();
	}

	@Override
	public int count() {
		return __count;
	}

	@Override
	public int countPlayers() {
		return __countPlayers;
	}

	@Override
	public Map<String, Player> gets() {
		synchronized (__players) {
			return __players;
		}
	}

	@Override
	public void clear() {
		synchronized (__players) {
			__players.clear();
		}
	}

	@Override
	public boolean contain(String name) {
		synchronized (__players) {
			return __players.containsKey(name);
		}
	}

	@Override
	public Player get(String name) {
		synchronized (__players) {
			return __players.get(name);
		}
	}

	@Override
	public void add(Player player, Connection connection) throws DuplicatedPlayerException, NullPlayerNameException {
		if (player.getName() == null) {
			// fire an event
			__eventManager.getExtension().emit(ZeroEvent.PLAYER_LOGINED_FAILED, player,
					CoreMessageCode.PLAYER_INFO_IS_INVALID);
			throw new NullPlayerNameException();
		}

		synchronized (__players) {
			if (__players.containsKey(player.getName())) {
				// fire an event
				__eventManager.getExtension().emit(ZeroEvent.PLAYER_LOGINED_FAILED, player,
						CoreMessageCode.PLAYER_WAS_EXISTED);
				throw new DuplicatedPlayerException(player.getName());
			}

			// add the main connection
			connection.setPlayerName(player.getName());
			int size = 0;
			if (connection.isType(TransportType.WEB_SOCKET)) {
				size = __webSocketPortsSize;
			} else {
				size = __socketPortsSize;
			}
			player.initializeConnections(size);
			player.setConnection(connection, CoreConstants.MAIN_CONNECTION_INDEX);

			__players.put(player.getName(), player);
			__count = __players.size();
			__countPlayers = (int) __players.values().stream().filter(p -> !p.isNPC()).count();

			// fire an event
			__eventManager.getExtension().emit(ZeroEvent.PLAYER_LOGINED_SUCCESS, player);
		}

	}

	@Override
	public void add(Player player) throws DuplicatedPlayerException {
		synchronized (__players) {
			if (__players.containsKey(player.getName())) {
				// fire an event
				__eventManager.getExtension().emit(ZeroEvent.PLAYER_LOGINED_FAILED, player,
						CoreMessageCode.PLAYER_WAS_EXISTED);
				throw new DuplicatedPlayerException(player.getName());
			}

			__players.put(player.getName(), player);
			__count = __players.size();
			__countPlayers = (int) __players.values().stream().filter(p -> !p.isNPC()).count();
			// fire an event
			__eventManager.getExtension().emit(ZeroEvent.PLAYER_LOGINED_SUCCESS, player);
		}

	}

	@Override
	public void remove(Player player) {
		if (player == null) {
			return;
		}

		synchronized (__players) {
			if (!__players.containsKey(player.getName())) {
				return;
			}

			// force player to leave its current room, fire a logic event
			__eventManager.getInternal().emit(InternalEvent.PLAYER_WAS_FORCED_TO_LEAVE_ROOM, player);

			// remove all player's connections from the player
			removeAllConnections(player);

			__players.remove(player.getName());
			__count = __players.size();
			__countPlayers = (int) __players.values().stream().filter(p -> !p.isNPC()).count();
		}

	}

	@Override
	public void removeAllConnections(Player player) {
		player.closeAllConnections();
	}

	@Override
	public void clean(Player player) {
		if (player == null) {
			return;
		}

		synchronized (__players) {
			if (!__players.containsKey(player.getName())) {
				return;
			}

			__players.remove(player.getName());
			__count = __players.size();
			__countPlayers = (int) __players.values().stream().filter(p -> !p.isNPC()).count();
		}

	}

	@Override
	public boolean isEmpty() {
		return __count == 0;
	}

}