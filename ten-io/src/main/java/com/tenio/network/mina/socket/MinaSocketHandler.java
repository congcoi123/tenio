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
package com.tenio.network.mina.socket;

import org.apache.mina.core.session.IoSession;

import com.tenio.configuration.BaseConfiguration;
import com.tenio.configuration.constant.LogicEvent;
import com.tenio.event.EventManager;
import com.tenio.message.codec.MsgPackConverter;
import com.tenio.network.Connection;
import com.tenio.network.mina.BaseMinaHandler;
import com.tenio.network.mina.MinaConnection;

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
		// convert to the game message
		var message = MsgPackConverter.unserialize((byte[]) msg);
		if (message == null) {
			return;
		}

		var connection = _getConnection(session);
		if (connection == null) { // the new connection
			connection = MinaConnection.newInstance(Connection.Type.SOCKET, session);
			EventManager.getLogic().emit(LogicEvent.CREATE_NEW_CONNECTION, __maxPlayer, __keepPlayerOnDisconnect,
					connection, message);
		} else {
			EventManager.getLogic().emit(LogicEvent.SOCKET_HANDLE, connection, message);
		}

	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		_exceptionCaught(session, cause);
	}

}
