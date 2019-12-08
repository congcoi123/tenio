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
package com.tenio.net.mina;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.tenio.configuration.BaseConfiguration;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.manager.PlayerManager;
import com.tenio.event.EventManager;
import com.tenio.net.Connection;
import com.tenio.server.Server;

/**
 * /** Use <a href="https://mina.apache.org/">Apache Mina</a> to handle message.
 * Base on the messages' content. You can handle your own logic here.
 * 
 * @author kong
 * 
 */
public abstract class BaseMinaHandler extends IoHandlerAdapter {

	/**
	 * @see {@link Server}
	 */
	protected Server _server = Server.getInstance();
	/**
	 * @see {@link PlayerManager}
	 */
	protected PlayerManager _playersManager = PlayerManager.getInstance();
	/**
	 * @see {@link EventManager}
	 */
	protected EventManager _events = EventManager.getInstance();

	/**
	 * Retrieve a connection by its session
	 * 
	 * @param session @see {@link IoSession}
	 * @return Returns a connection
	 */
	protected Connection _getConnection(IoSession session) {
		return (MinaConnection) session.getAttribute(MinaConnection.KEY_THIS);
	}

	/**
	 * When a client is disconnected from your server for any reason, you can handle
	 * it in this event
	 * 
	 * @param session                the session @see {@link IoSession}
	 * @param keepPlayerOnDisconnect this value can be configured in your
	 *                               configurations @see {@link BaseConfiguration}.
	 *                               If the value is set to true, when the client is
	 *                               disconnected, it's player can be held for an
	 *                               interval time (you can configure this interval
	 *                               time in your configurations)
	 */
	protected void _sessionClosed(IoSession session, boolean keepPlayerOnDisconnect) {
		// get connection first
		Connection connection = _getConnection(session);
		if (connection != null) { // old connection
			String id = connection.getId();
			if (id != null) { // Player
				AbstractPlayer player = _playersManager.get(id);
				if (player != null) {
					_events.emit(TEvent.DISCONNECT_PLAYER, player);
					_playersManager.clearConnections(player);
					if (!keepPlayerOnDisconnect) {
						_playersManager.clean(player);
					}
				}
			} else { // Connection
				_events.emit(TEvent.DISCONNECT_CONNECTION, connection);
			}
			connection.clean();
		}
		connection = null;
	}

	/**
	 * Record the exceptions
	 * 
	 * @param session the session @see {@link IoSession}
	 * @param cause   the exception will occur
	 */
	protected void _exceptionCaught(IoSession session, Throwable cause) {
		// get connection first
		Connection connection = _getConnection(session);
		if (connection != null) { // old connection
			String id = connection.getId();
			if (id != null) { // Player
				AbstractPlayer player = _playersManager.get(id);
				if (player != null) {
					_server.exception(player, cause);
					return;
				}
			}
		}
		_server.exception(String.valueOf(session.getId()), cause);
	}

}
