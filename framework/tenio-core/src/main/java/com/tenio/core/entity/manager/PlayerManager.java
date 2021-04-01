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
package com.tenio.core.entity.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.core.api.PlayerApi;
import com.tenio.core.configuration.SocketConfig;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.configuration.define.InternalEvent;
import com.tenio.core.configuration.define.TransportType;
import com.tenio.core.entity.IPlayer;
import com.tenio.core.event.IEventManager;
import com.tenio.core.exception.DuplicatedPlayerException;
import com.tenio.core.exception.NullPlayerNameException;
import com.tenio.core.network.IConnection;

/**
 * Manage all your players ({@link IPlayer}) on the server. It is a singleton
 * pattern class, which can be called anywhere. But it's better that you use the
 * {@link PlayerApi} interface for easy management.
 * 
 * @see IPlayerManager
 * 
 * @author kong
 * 
 */
public final class PlayerManager implements IPlayerManager {

	/**
	 * A map object to manage your players with the key must be a player's name
	 */
	private final Map<String, IPlayer> __players;
	private final IEventManager __eventManager;
	private IConfiguration __configuration;
	private List<SocketConfig> __socketPorts;
	private List<SocketConfig> __webSocketPorts;
	private int __socketPortsSize;
	private int __webSocketPortsSize;

	public PlayerManager(IEventManager eventManager) {
		__eventManager = eventManager;
		__players = new HashMap<String, IPlayer>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void initialize(IConfiguration configuration) {
		__configuration = configuration;
		__socketPorts = (List<SocketConfig>) (__configuration.get(CoreConfigurationType.SOCKET_PORTS));
		__webSocketPorts = (List<SocketConfig>) (__configuration.get(CoreConfigurationType.WEBSOCKET_PORTS));
		__socketPortsSize = __socketPorts.size();
		__webSocketPortsSize = __webSocketPorts.size();
	}

	@Override
	public int count() {
		synchronized (__players) {
			return __players.size();
		}
	}

	@Override
	public int countPlayers() {
		synchronized (__players) {
			return (int) __players.values().stream().filter(player -> !player.isNPC()).count();
		}
	}

	@Override
	public Map<String, IPlayer> gets() {
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
	public boolean contain(final String name) {
		synchronized (__players) {
			return __players.containsKey(name);
		}
	}

	@Override
	public IPlayer get(final String name) {
		synchronized (__players) {
			return __players.get(name);
		}
	}

	@Override
	public void add(final IPlayer player, final IConnection connection)
			throws DuplicatedPlayerException, NullPlayerNameException {
		if (player.getName() == null) {
			// fire an event
			__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_FAILED, player,
					CoreMessageCode.PLAYER_INFO_IS_INVALID);
			throw new NullPlayerNameException();
		}

		synchronized (__players) {
			if (__players.containsKey(player.getName())) {
				// fire an event
				__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_FAILED, player,
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
			player.setConnection(connection, 0);

			__players.put(player.getName(), player);

			// fire an event
			__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_SUCCESS, player);
		}

	}

	@Override
	public void add(final IPlayer player) throws DuplicatedPlayerException {
		synchronized (__players) {
			if (__players.containsKey(player.getName())) {
				// fire an event
				__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_FAILED, player,
						CoreMessageCode.PLAYER_WAS_EXISTED);
				throw new DuplicatedPlayerException(player.getName());
			}

			__players.put(player.getName(), player);
			// fire an event
			__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_SUCCESS, player);
		}

	}

	@Override
	public void remove(final IPlayer player) {
		if (player == null) {
			return;
		}

		synchronized (__players) {
			if (!__players.containsKey(player.getName())) {
				return;
			}

			// force player leave room, fire a logic event
			__eventManager.getInternal().emit(InternalEvent.PLAYER_WAS_FORCED_TO_LEAVE_ROOM, player);

			// remove all player's connections, player
			removeAllConnections(player);

			__players.remove(player.getName());
		}

	}

	@Override
	public void removeAllConnections(final IPlayer player) {
		player.closeAllConnections();
	}

	@Override
	public void clean(final IPlayer player) {
		if (player == null) {
			return;
		}

		synchronized (__players) {
			if (!__players.containsKey(player.getName())) {
				return;
			}

			__players.remove(player.getName());
		}

	}

}
