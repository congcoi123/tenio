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
package com.tenio.server;

import com.tenio.configuration.constant.ErrorMsg;
import com.tenio.configuration.constant.LogicEvent;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.element.TObject;
import com.tenio.entities.manager.IPlayerManager;
import com.tenio.entities.manager.IRoomManager;
import com.tenio.event.EventManager;
import com.tenio.event.ISubscriber;
import com.tenio.logger.AbstractLogger;
import com.tenio.network.Connection;

/**
 * Handle the main logic of the server.
 * 
 * @author kong
 *
 */
final class ServerLogic extends AbstractLogger {

	/**
	 * @see IPlayerManager
	 */
	private IPlayerManager __playerManager;
	/**
	 * @see IRoomManager
	 */
	private IRoomManager __roomManager;

	public ServerLogic(IPlayerManager playerManager, IRoomManager roomManager) {
		__playerManager = playerManager;
		__roomManager = roomManager;
	}

	/**
	 * Start handling
	 */
	public void init() {
		
		__on(LogicEvent.FORCE_PLAYER_LEAVE_ROOM, args -> {
			AbstractPlayer player = (AbstractPlayer) args[0];
			
			__roomManager.playerLeaveRoom(player, true);
			
			return null;
		});

		__on(LogicEvent.CONNECTION_CLOSE, args -> {
			Connection connection = (Connection) args[0];
			boolean keepPlayerOnDisconnect = (boolean) args[1];

			if (connection != null) { // the connection has existed
				String id = connection.getId();
				if (id != null) { // the player maybe exist
					AbstractPlayer player = __playerManager.get(id);
					if (player != null) { // the player has existed
						EventManager.getEvent().emit(TEvent.DISCONNECT_PLAYER, player);
						__playerManager.removeAllConnections(player);
						if (!keepPlayerOnDisconnect) {
							__playerManager.clean(player);
						}
					}
				} else { // the free connection (without a corresponding player)
					EventManager.getEvent().emit(TEvent.DISCONNECT_CONNECTION, connection);
				}
				connection.clean();
			}
			
			return null;
		});

		__on(LogicEvent.CONNECTION_EXCEPTION, args -> {
			String channelId = (String) args[0];
			Connection connection = (Connection) args[1];
			Throwable cause = (Throwable) args[2];

			if (connection != null) { // the old connection
				String id = connection.getId();
				if (id != null) { // the player maybe exist
					AbstractPlayer player = __playerManager.get(id);
					if (player != null) { // the player has existed
						__exception(player, cause);
						return null;
					}
				}
			}
			// also catch the exception for "channel"
			__exception(channelId, cause);

			return null;
		});

		__on(LogicEvent.MANUALY_CLOSE_CONNECTION, args -> {
			String name = (String) args[0];

			AbstractPlayer player = __playerManager.get(name);
			if (player != null) {
				EventManager.getEvent().emit(TEvent.DISCONNECT_PLAYER, player);
			}
			
			return null;
		});

		__on(LogicEvent.CREATE_NEW_CONNECTION, args -> {
			int maxPlayer = (int) args[0];
			boolean keepPlayerOnDisconnect = (boolean) args[1];
			Connection connection = (Connection) args[2];
			TObject message = (TObject) args[3];

			// check the reconnection first
			if (keepPlayerOnDisconnect) {
				AbstractPlayer player = (AbstractPlayer) EventManager.getEvent().emit(TEvent.PLAYER_RECONNECT_REQUEST,
						connection, message);
				if (player != null) {
					player.currentReaderTime();
					connection.setId(player.getName());
					player.setConnection(connection);

					EventManager.getEvent().emit(TEvent.PLAYER_RECONNECT_SUCCESS, player);
					return null;
				}
			}
			// check the number of current players
			if (__playerManager.count() > maxPlayer) {
				EventManager.getEvent().emit(TEvent.CONNECTION_FAILED, connection, ErrorMsg.REACH_MAX_CONNECTION);
				connection.close();
			} else {
				EventManager.getEvent().emit(TEvent.CONNECTION_SUCCESS, connection, message);
			}

			return null;
		});

		__on(LogicEvent.SOCKET_HANDLE, args -> {
			Connection connection = (Connection) args[0];
			TObject message = (TObject) args[1];

			String id = connection.getId();
			if (id != null) { // the player's identify
				AbstractPlayer player = __playerManager.get(id);
				if (player != null) {
					__handle(player, false, message);
				}
			} else { // a new connection
				EventManager.getEvent().emit(TEvent.CONNECTION_SUCCESS, connection, message);
			}

			return null;
		});

		__on(LogicEvent.DATAGRAM_HANDLE, args -> {
			AbstractPlayer player = (AbstractPlayer) args[0];
			TObject message = (TObject) args[1];

			// UDP is only attach connection, so if the main connection not found, the UDP
			// must be stop handled
			if (player.hasConnection()) {
				__handle(player, true, message);
			}

			return null;
		});

		__on(LogicEvent.GET_PLAYER, args -> {
			String name = (String) args[0];
			return __playerManager.get(name);
		});
		
	}
	
	private void __on(final LogicEvent event, ISubscriber sub) {
		EventManager.getLogic().on(event, sub);
	}

	private void __handle(AbstractPlayer player, boolean isSubConnection, TObject message) {
		if (isSubConnection) {
			debug("RECV PLAYER SUB", player.getName(), message.toString());
		} else {
			debug("RECV PLAYER", player.getName(), message.toString());
		}
		player.currentReaderTime();
		EventManager.getEvent().emit(TEvent.RECEIVED_FROM_PLAYER, player, isSubConnection, message);
	}

	private void __exception(AbstractPlayer player, Throwable cause) {
		error("EXCEPTION PLAYER", player.getName(), cause);
	}

	private void __exception(String identify, Throwable cause) {
		error("EXCEPTION CONNECTION CHANNEL", identify, cause);
	}

}
