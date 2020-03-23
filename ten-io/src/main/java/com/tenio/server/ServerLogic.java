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
	private final IPlayerManager __playerManager;
	/**
	 * @see IRoomManager
	 */
	private final IRoomManager __roomManager;

	public ServerLogic(IPlayerManager playerManager, IRoomManager roomManager) {
		__playerManager = playerManager;
		__roomManager = roomManager;
	}

	/**
	 * Start handling
	 */
	public void init() {

		__on(LogicEvent.FORCE_PLAYER_LEAVE_ROOM, args -> {
			var player = __getPlayer(args[0]);

			__roomManager.playerLeaveRoom(player, true);

			return null;
		});

		__on(LogicEvent.CONNECTION_CLOSE, args -> {
			var connection = __getConnection(args[0]);
			boolean keepPlayerOnDisconnect = __getBoolean(args[1]);

			if (connection != null) { // the connection has existed
				String id = connection.getId();
				if (id != null) { // the player maybe exist
					var player = __playerManager.get(id);
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
			String channelId = __getString(args[0]);
			var connection = __getConnection(args[1]);
			var cause = __getThrowable(args[2]);

			if (connection != null) { // the old connection
				String id = connection.getId();
				if (id != null) { // the player maybe exist
					var player = __playerManager.get(id);
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
			String name = __getString(args[0]);

			var player = __playerManager.get(name);
			if (player != null) {
				EventManager.getEvent().emit(TEvent.DISCONNECT_PLAYER, player);
			}

			return null;
		});

		__on(LogicEvent.CREATE_NEW_CONNECTION, args -> {
			int maxPlayer = __getInt(args[0]);
			boolean keepPlayerOnDisconnect = __getBoolean(args[1]);
			var connection = __getConnection(args[2]);
			var message = __getTObject(args[3]);

			// check the reconnection first
			if (keepPlayerOnDisconnect) {
				var player = (AbstractPlayer) EventManager.getEvent().emit(TEvent.PLAYER_RECONNECT_REQUEST, connection,
						message);
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
			var connection = __getConnection(args[0]);
			var message = __getTObject(args[1]);

			String id = connection.getId();
			if (id != null) { // the player's identify
				var player = __playerManager.get(id);
				if (player != null) {
					__handle(player, false, message);
				}
			} else { // a new connection
				EventManager.getEvent().emit(TEvent.CONNECTION_SUCCESS, connection, message);
			}

			return null;
		});

		__on(LogicEvent.DATAGRAM_HANDLE, args -> {
			var player = __getPlayer(args[0]);
			var message = __getTObject(args[1]);

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

	/**
	 * @param object the corresponding object
	 * @return Returns value in @see {@link TObject}
	 */
	private TObject __getTObject(Object object) {
		return (TObject) object;
	}

	/**
	 * @param object the corresponding object
	 * @return Returns value in @see {@link Connection}
	 */
	private Connection __getConnection(Object object) {
		return (Connection) object;
	}

	/**
	 * @param object the corresponding object
	 * @return Returns value in @see {@link AbstractPlayer}
	 */
	private AbstractPlayer __getPlayer(Object object) {
		return (AbstractPlayer) object;
	}

	/**
	 * @param object the corresponding object
	 * @return Returns value in @see {@link Boolean}
	 */
	private boolean __getBoolean(Object object) {
		return (boolean) object;
	}

	/**
	 * @param object the corresponding object
	 * @return Returns value in @see {@link String}
	 */
	private String __getString(Object object) {
		return (String) object;
	}

	/**
	 * @param object the corresponding object
	 * @return Returns value in @see {@link Integer}
	 */
	private int __getInt(Object object) {
		return (int) object;
	}

	/**
	 * @param object the corresponding object
	 * @return Returns value in @see {@link Throwable}
	 */
	private Throwable __getThrowable(Object object) {
		return (Throwable) object;
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
