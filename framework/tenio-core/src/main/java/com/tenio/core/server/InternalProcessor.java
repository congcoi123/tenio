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
package com.tenio.core.server;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.data.element.CommonObject;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.configuration.define.ExtensionEvent;
import com.tenio.core.configuration.define.InternalEvent;
import com.tenio.core.controller.AbstractController;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.event.EventManager;
import com.tenio.core.event.Subscriber;
import com.tenio.core.exception.ExtensionValueCastException;
import com.tenio.core.network.entity.protocol.Request;
import com.tenio.core.network.entity.session.Connection;

/**
 * Handle the main logic of the server.
 * 
 * @author kong
 */
@ThreadSafe
// TODO: Add description
public final class InternalProcessor extends AbstractController implements Service {

	private final EventManager __eventManager;
	private final PlayerManager __playerManager;
	private final RoomManager __roomManager;

	public InternalProcessor(EventManager eventManager, PlayerManager playerManager, RoomManager roomManager) {
		__eventManager = eventManager;
		__playerManager = playerManager;
		__roomManager = roomManager;
	}

	/**
	 * Start handling
	 */
	public void init(Configuration configuration) {

		boolean keepPlayerOnDisconnect = configuration.getBoolean(CoreConfigurationType.KEEP_PLAYER_ON_DISCONNECT);

		__on(InternalEvent.SESSION_WAS_CLOSED, params -> {
			var session;

			if (session != null) { // the connection has existed
				var playerName = session.getPlayerName();
				if (playerName != null) { // the player maybe exist
					var player = __playerManager.get(playerName);
					if (player != null) { // the player has existed
						__eventManager.getExtension().emit(ExtensionEvent.DISCONNECT_PLAYER, player);
						__playerManager.removeAllConnections(player);
						if (!keepPlayerOnDisconnect) {
							__playerManager.clean(player);
						}
					}
				} else { // the free connection (without a corresponding player)
					__eventManager.getExtension().emit(ExtensionEvent.DISCONNECT_CONNECTION, connection);
				}
				connection.clean();
			}

			return null;
		});

		__on(InternalEvent.CONNECTION_MESSAGE_HANDLED_EXCEPTION, params -> {
			String channelId = __getString(params[0]);
			var connection = __getConnection(params[1]);
			var cause = __getThrowable(params[2]);

			if (connection != null) { // the old connection
				var playerName = connection.getPlayerName();
				if (playerName != null) { // the player maybe exist
					var player = __playerManager.get(playerName);
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

		__on(InternalEvent.PLAYER_WAS_FORCED_TO_LEAVE_ROOM, params -> {
			var player = __getPlayer(params[0]);

			__roomManager.makePlayerLeaveRoom(player, true);

			return null;
		});

		__on(InternalEvent.CONNECTION_WAS_CLOSED_MANUALLY, params -> {
			String name = __getString(params[0]);

			var player = __playerManager.get(name);
			if (player != null) {
				__eventManager.getExtension().emit(ExtensionEvent.DISCONNECT_PLAYER, player);
			}

			return null;
		});

		__on(InternalEvent.MESSAGE_HANDLED_IN_CHANNEL, params -> {
			var connectionIndex = __getInteger(params[0]);
			var connection = params[1] == null ? null : __getConnection(params[1]);
			var message = __getCommonObject(params[2]);
			var tempConnection = __getConnection(params[3]);

			if (connection == null) {
				__createNewConnection(configuration, connectionIndex, tempConnection, message);
			} else {
				var playerName = connection.getPlayerName();
				if (playerName != null) {
					var player = __playerManager.get(playerName);
					if (player != null) { // the player has existed
						__handle(player, connectionIndex, message);
					} else {
						// Can handle free connection here
						connection.close();
					}
				} else {
					// Can handle free connection here
					connection.close();
				}
			}

			return null;
		});

	}

	private void __createNewConnection(Configuration configuration, int connectionIndex, Connection connection,
			CommonObject message) {
		if (connectionIndex == CoreConstant.MAIN_CONNECTION_INDEX) { // is main connection
			// check reconnection request first
			var player = (Player) __eventManager.getExtension().emit(ExtensionEvent.PLAYER_RECONNECT_REQUEST_HANDLE,
					connection, message);
			if (player != null) {
				connection.setPlayerName(player.getName());
				player.setConnection(connection, CoreConstant.MAIN_CONNECTION_INDEX); // main connection
				__eventManager.getExtension().emit(ExtensionEvent.PLAYER_RECONNECT_SUCCESS, player);
			} else {
				// check the number of current players
				if (__playerManager.count() > configuration.getInt(CoreConfigurationType.MAX_NUMBER_PLAYERS)) {
					__eventManager.getExtension().emit(ExtensionEvent.CONNECTION_ESTABLISHED_FAILED, connection,
							CoreMessageCode.REACHED_MAX_CONNECTION);
					connection.close();
				} else {
					__eventManager.getExtension().emit(ExtensionEvent.CONNECTION_ESTABLISHED_SUCCESS, connection, message);
				}
			}

		} else {
			// the condition for creating sub-connection
			var player = (Player) __eventManager.getExtension().emit(ExtensionEvent.ATTACH_CONNECTION_REQUEST_VALIDATE,
					connectionIndex, message);

			if (player == null) {
				__eventManager.getExtension().emit(ExtensionEvent.ATTACH_CONNECTION_FAILED, connectionIndex, message,
						CoreMessageCode.PLAYER_NOT_FOUND);
			} else if (player.getConnection(0) == null) {
				__eventManager.getExtension().emit(ExtensionEvent.ATTACH_CONNECTION_FAILED, connectionIndex, message,
						CoreMessageCode.MAIN_CONNECTION_NOT_FOUND);
			} else {
				connection.setPlayerName(player.getName());
				player.setConnection(connection, connectionIndex);
				__eventManager.getExtension().emit(ExtensionEvent.ATTACH_CONNECTION_SUCCESS, connectionIndex, player);
			}
		}
	}

	private void __on(InternalEvent event, Subscriber sub) {
		__eventManager.getInternal().on(event, sub);
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link CommonObject} type
	 * @throws ExtensionValueCastException
	 */
	private CommonObject __getCommonObject(Object object) throws ExtensionValueCastException {
		if (object instanceof CommonObject) {
			return (CommonObject) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link Connection} type
	 * @throws ExtensionValueCastException
	 */
	private Connection __getConnection(Object object) throws ExtensionValueCastException {
		if (object instanceof Connection) {
			return (Connection) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param <T>    the corresponding return type
	 * @param object the corresponding object
	 * @return a value in {@link Player} type
	 * @throws ExtensionValueCastException
	 */
	private Player __getPlayer(Object object) throws ExtensionValueCastException {
		if (object instanceof Player) {
			return (Player) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link Throwable} type
	 * @throws ExtensionValueCastException
	 */
	private Throwable __getThrowable(Object object) throws ExtensionValueCastException {
		if (object instanceof Throwable) {
			return (Throwable) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return value in {@link String} type
	 * @throws ExtensionValueCastException
	 */
	private String __getString(Object object) throws ExtensionValueCastException {
		if (object instanceof String) {
			return (String) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link Integer} type
	 * @throws ExtensionValueCastException
	 */
	private int __getInteger(Object object) throws ExtensionValueCastException {
		if (object instanceof Integer) {
			return (int) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	private void __handle(Player player, int connectionIndex, CommonObject message) {
		player.setCurrentReaderTime();
		__eventManager.getExtension().emit(ExtensionEvent.RECEIVED_MESSAGE_FROM_PLAYER, player, connectionIndex, message);
	}

	private void __exception(Player player, Throwable cause) {
		_error(cause, "player's name: ", player.getName());
	}

	private void __exception(String identify, Throwable cause) {
		_error(cause, "identify: ", identify);
	}

	@Override
	public void onInitialized() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStarted() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResumed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPaused() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopped() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroyed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isActivated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void processRequest(Request request) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
