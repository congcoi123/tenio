package com.tenio.server;

import com.tenio.configuration.constant.ErrorMsg;
import com.tenio.configuration.constant.LogicEvent;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.element.TObject;
import com.tenio.entities.manager.PlayerManager;
import com.tenio.entities.manager.RoomManager;
import com.tenio.event.EventManager;
import com.tenio.logger.AbstractLogger;
import com.tenio.net.Connection;

final class ServerLogic extends AbstractLogger {

	private PlayerManager __playerManager = new PlayerManager();
	private RoomManager __roomManager = new RoomManager();

	public ServerLogic() {

		EventManager.getLogic().on(LogicEvent.FORCE_PLAYER_LEAVE_ROOM, (source, args) -> {
			AbstractPlayer player = (AbstractPlayer) args[0];
			synchronized (player) {
				__roomManager.playerLeaveRoom(player, true);
			}
			return null;
		});

		EventManager.getLogic().on(LogicEvent.CONNECTION_CLOSE, (source, args) -> {
			Connection connection = (Connection) args[0];
			boolean keepPlayerOnDisconnect = (boolean) args[1];

			if (connection != null) { // old connection
				String id = connection.getId();
				if (id != null) { // Player
					AbstractPlayer player = __playerManager.get(id);
					if (player != null) {
						EventManager.getEvent().emit(TEvent.DISCONNECT_PLAYER, player);
						__playerManager.removeAllConnections(player);
						if (!keepPlayerOnDisconnect) {
							__playerManager.clean(player);
						}
					}
				} else { // Connection
					EventManager.getEvent().emit(TEvent.DISCONNECT_CONNECTION, connection);
				}
				connection.clean();
			}
			return null;
		});

		EventManager.getLogic().on(LogicEvent.CONNECTION_EXCEPTION, (source, args) -> {
			String channelId = (String) args[0];
			Connection connection = (Connection) args[1];
			Throwable cause = (Throwable) args[2];

			if (connection != null) { // old connection
				String id = connection.getId();
				if (id != null) { // Player
					AbstractPlayer player = __playerManager.get(id);
					if (player != null) {
						exception(player, cause);
						return null;
					}
				}
			}
			exception(channelId, cause);

			return null;
		});

		EventManager.getLogic().on(LogicEvent.MANUAL_CLOSE_CONNECTION, (source, args) -> {
			String name = (String) args[0];

			AbstractPlayer player = __playerManager.get(name);
			if (player != null) {
				EventManager.getEvent().emit(TEvent.DISCONNECT_PLAYER, player);
			}
			return null;
		});

		EventManager.getLogic().on(LogicEvent.CREATE_NEW_CONNECTION, (source, args) -> {
			int maxPlayer = (int) args[0];
			boolean keepPlayerOnDisconnect = (boolean) args[1];
			Connection connection = (Connection) args[2];
			TObject message = (TObject) args[3];

			// check reconnection
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

			if (__playerManager.count() > maxPlayer) {
				EventManager.getEvent().emit(TEvent.CONNECTION_FAILED, connection, ErrorMsg.REACH_MAX_CONNECTION);
				connection.close();
			} else {
				EventManager.getEvent().emit(TEvent.CONNECTION_SUCCESS, connection, message);
			}

			return null;
		});

		EventManager.getLogic().on(LogicEvent.SOCKET_HANDLE, (source, args) -> {
			Connection connection = (Connection) args[0];
			TObject message = (TObject) args[1];

			String id = connection.getId();
			if (id != null) { // player's identify
				AbstractPlayer player = __playerManager.get(id);
				if (player != null) {
					handle(player, false, message);
				}
			} else { // connection
				EventManager.getEvent().emit(TEvent.CONNECTION_SUCCESS, connection, message);
			}

			return null;
		});

		EventManager.getLogic().on(LogicEvent.DATAGRAM_HANDLE, (source, args) -> {
			AbstractPlayer player = (AbstractPlayer) args[0];
			TObject message = (TObject) args[1];

			// UDP is only attach connection, so if the main connection not found, the UDP
			// must be stop handled
			if (player.hasConnection()) {
				handle(player, true, message);
			}

			return null;
		});

		EventManager.getLogic().on(LogicEvent.GET_PLAYER, (source, args) -> {
			String name = (String) args[0];
			return __playerManager.get(name);
		});

	}

	public void handle(AbstractPlayer player, boolean isSubConnection, TObject message) {
		if (isSubConnection) {
			debug("RECV PLAYER SUB", player.getName(), message.toString());
		} else {
			debug("RECV PLAYER", player.getName(), message.toString());
		}
		player.currentReaderTime();
		EventManager.getEvent().emit(TEvent.RECEIVED_FROM_PLAYER, player, isSubConnection, message);
	}

	public void exception(AbstractPlayer player, Throwable cause) {
		error("EXCEPTION PLAYER", player.getName(), cause);
	}

	public void exception(String identify, Throwable cause) {
		error("EXCEPTION CONNECTION CHANNEL", identify, cause);
	}

}
