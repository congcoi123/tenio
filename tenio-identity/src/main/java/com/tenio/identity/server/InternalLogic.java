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
package com.tenio.identity.server;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.identity.common.configuration.BaseConfiguration;
import com.tenio.identity.common.configuration.constant.ErrorMsg;
import com.tenio.identity.common.configuration.constant.LEvent;
import com.tenio.identity.common.configuration.constant.TEvent;
import com.tenio.identity.common.entity.AbstractPlayer;
import com.tenio.identity.common.entity.element.TObject;
import com.tenio.identity.common.entity.manager.IPlayerManager;
import com.tenio.identity.common.entity.manager.IRoomManager;
import com.tenio.identity.common.event.IEventManager;
import com.tenio.identity.common.event.ISubscriber;
import com.tenio.identity.common.network.Connection;

/**
 * Handle the main logic of the server.
 * 
 * @author kong
 *
 */
final class InternalLogic extends AbstractLogger {

	private final IEventManager __eventManager;
	private final IPlayerManager __playerManager;
	private final IRoomManager __roomManager;

	public InternalLogic(IEventManager eventManager, IPlayerManager playerManager, IRoomManager roomManager) {
		__eventManager = eventManager;
		__playerManager = playerManager;
		__roomManager = roomManager;
	}

	/**
	 * Start handling
	 */
	public void init(BaseConfiguration configuration) {

		__on(LEvent.CONNECTION_CLOSE, args -> {
			var connection = __getConnection(args[0]);
			boolean keepPlayerOnDisconnect = configuration.getBoolean(BaseConfiguration.KEEP_PLAYER_ON_DISCONNECT);

			if (connection != null) { // the connection has existed
				String username = connection.getUsername();
				if (username != null) { // the player maybe exist
					var player = __playerManager.get(username);
					if (player != null) { // the player has existed
						__eventManager.getExternal().emit(TEvent.DISCONNECT_PLAYER, player);
						__playerManager.removeAllConnections(player);
						if (!keepPlayerOnDisconnect) {
							__playerManager.clean(player);
						}
					}
				} else { // the free connection (without a corresponding player)
					__eventManager.getExternal().emit(TEvent.DISCONNECT_CONNECTION, connection);
				}
				connection.clean();
			}

			return null;
		});

		__on(LEvent.CONNECTION_EXCEPTION, args -> {
			String channelId = __getString(args[0]);
			var connection = __getConnection(args[1]);
			var cause = __getThrowable(args[2]);

			if (connection != null) { // the old connection
				String username = connection.getUsername();
				if (username != null) { // the player maybe exist
					var player = __playerManager.get(username);
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

		__on(LEvent.FORCE_PLAYER_LEAVE_ROOM, args -> {
			var player = __getPlayer(args[0]);

			__roomManager.playerLeaveRoom(player, true);

			return null;
		});

		// This event will trigger the LEvent.CONNECTION_CLOSE event effect
		__on(LEvent.MANUALY_CLOSE_CONNECTION, args -> {
			String name = __getString(args[0]);

			var player = __playerManager.get(name);
			if (player != null) {
				__eventManager.getExternal().emit(TEvent.DISCONNECT_PLAYER, player);
			}

			return null;
		});

		__on(LEvent.CHANNEL_HANDLE, args -> {
			var index = __getInt(args[0]);
			var connection = __getConnection(args[1]);
			var message = __getTObject(args[2]);
			var tempConnection = __getConnection(args[3]);

			if (connection == null) {
				__createNewConnection(configuration, index, tempConnection, message);
			} else {
				var username = connection.getUsername();
				if (username != null) {
					var player = __playerManager.get(username);
					if (player != null) { // the player has existed
						__handle(player, index, message);
					} else {
						// Can handle free connection here
					}
				} else {
					// Can handle free connection here
				}
			}

			return null;
		});

	}

	private void __createNewConnection(final BaseConfiguration configuration, final int index,
			final Connection connection, final TObject message) {
		if (index == 0) { // is main connection
			// check reconnection request first
			var player = (AbstractPlayer) __eventManager.getExternal().emit(TEvent.PLAYER_RECONNECT_REQUEST, connection,
					message);
			if (player != null) {
				connection.setUsername(player.getName());
				player.setConnection(connection, 0); // main connection
				__eventManager.getExternal().emit(TEvent.PLAYER_RECONNECT_SUCCESS, player);
			} else {
				// check the number of current players
				if (__playerManager.count() > configuration.getInt(BaseConfiguration.MAX_PLAYER)) {
					__eventManager.getExternal().emit(TEvent.CONNECTION_FAILED, connection,
							ErrorMsg.REACH_MAX_CONNECTION);
					connection.close();
				} else {
					__eventManager.getExternal().emit(TEvent.CONNECTION_SUCCESS, connection, message);
				}
			}

		} else {
			// the condition for creating sub-connection
			var player = (AbstractPlayer) __eventManager.getExternal().emit(TEvent.ATTACH_CONNECTION_REQUEST, index,
					message);

			if (player == null) {
				__eventManager.getExternal().emit(TEvent.ATTACH_CONNECTION_FAILED, index, message,
						ErrorMsg.PLAYER_NOT_FOUND);
			} else if (!player.hasConnection(0)) {
				__eventManager.getExternal().emit(TEvent.ATTACH_CONNECTION_FAILED, index, message,
						ErrorMsg.MAIN_CONNECTION_NOT_FOUND);
			} else {
				connection.setUsername(player.getName());
				player.setConnection(connection, index);
				__eventManager.getExternal().emit(TEvent.ATTACH_CONNECTION_SUCCESS, index, player);
			}
		}
	}

	private void __on(final LEvent event, ISubscriber sub) {
		__eventManager.getInternal().on(event, sub);
	}

	/**
	 * @param object the corresponding object
	 * @return a value in, see {@link TObject}
	 */
	private TObject __getTObject(Object object) {
		return (TObject) object;
	}

	/**
	 * @param object the corresponding object
	 * @return a value in ,see {@link Connection}
	 */
	private Connection __getConnection(Object object) {
		return (Connection) object;
	}

	/**
	 * @param object the corresponding object
	 * @return a value in, see {@link AbstractPlayer}
	 */
	private AbstractPlayer __getPlayer(Object object) {
		return (AbstractPlayer) object;
	}

	/**
	 * @param object the corresponding object
	 * @return a value in, see {@link String}
	 */
	private String __getString(Object object) {
		return (String) object;
	}

	/**
	 * @param object the corresponding object
	 * @return a value in, see {@link Integer}
	 */
	private int __getInt(Object object) {
		return (int) object;
	}

	/**
	 * @param object the corresponding object
	 * @return value in, see {@link Throwable}
	 */
	private Throwable __getThrowable(Object object) {
		return (Throwable) object;
	}

	private void __handle(AbstractPlayer player, int index, TObject message) {
		debug("RECV PLAYER", index, player.getName(), message.toString());
		player.setCurrentReaderTime();
		__eventManager.getExternal().emit(TEvent.RECEIVED_FROM_PLAYER, player, index, message);
	}

	private void __exception(AbstractPlayer player, Throwable cause) {
		error(cause, "player name: ", player.getName());
	}

	private void __exception(String identify, Throwable cause) {
		error(cause, "identify: ", identify);
	}

}
