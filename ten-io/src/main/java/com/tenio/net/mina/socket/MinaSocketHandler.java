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
package com.tenio.net.mina.socket;

import org.apache.mina.core.session.IoSession;

import com.tenio.configuration.BaseConfiguration;
import com.tenio.configuration.constant.ErrorMsg;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.element.TObject;
import com.tenio.message.codec.MsgPackConverter;
import com.tenio.net.Connection;
import com.tenio.net.mina.BaseMinaHandler;
import com.tenio.net.mina.MinaConnection;

import io.netty.channel.ChannelHandlerContext;

/**
 * Receive all messages sent from clients. It converts serialize data to a
 * system's object for convenience and easy to use. It also handles the logic
 * for the processing of players and connections.
 * 
 * @see {@link BaseMinaHandler}
 * 
 * @author kong
 * 
 */
public class MinaSocketHandler extends BaseMinaHandler {

	/**
	 * The maximum number of players that the server can handle
	 */
	private int __maxPlayer;
	/**
	 * Allow a client can be re-connected or not @see
	 * {@link #_channelInactive(ChannelHandlerContext, boolean)}
	 */
	private boolean __keepPlayerOnDisconnect;

	public MinaSocketHandler(BaseConfiguration configuration) {
		__maxPlayer = (int) configuration.get(BaseConfiguration.MAX_PLAYER) - 1;
		__keepPlayerOnDisconnect = (boolean) configuration.get(BaseConfiguration.KEEP_PLAYER_ON_DISCONNECT);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		_sessionClosed(session, __keepPlayerOnDisconnect);
	}

	@Override
	public void messageReceived(IoSession session, Object msg) throws Exception {
		// convert to game message
		TObject message = MsgPackConverter.unserialize((byte[]) msg);
		if (message == null) {
			return;
		}

		Connection connection = _getConnection(session);
		if (connection == null) { // new connection

			connection = MinaConnection.create(Connection.Type.SOCKET, session);

			// check reconnection
			if (__keepPlayerOnDisconnect) {
				AbstractPlayer player = (AbstractPlayer) _events.emit(TEvent.PLAYER_RECONNECT_REQUEST, connection,
						message);
				if (player != null) {
					player.currentReaderTime();
					connection.setId(player.getName());
					player.setConnection(connection);

					_events.emit(TEvent.PLAYER_RECONNECT_SUCCESS, player);
					return;
				}
			}

			if (_playersManager.count() > __maxPlayer) {
				_events.emit(TEvent.CONNECTION_FAILED, connection, ErrorMsg.REACH_MAX_CONNECTION);
				connection.close();
			} else {
				_events.emit(TEvent.CONNECTION_SUCCESS, connection, message);
			}
			return;

		}

		String id = connection.getId();
		if (id != null) { // player's identify
			AbstractPlayer player = _playersManager.get(id);
			if (player != null) {
				_server.handle(player, false, message);
			}
		} else { // connection
			_events.emit(TEvent.CONNECTION_SUCCESS, connection, message);
		}

	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		_exceptionCaught(session, cause);
	}

}
