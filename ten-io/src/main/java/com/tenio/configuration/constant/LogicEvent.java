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
package com.tenio.configuration.constant;

import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.element.TObject;
import com.tenio.network.Connection;

/**
 * This Enum defines all logic events in the main thread. All the process should
 * be handled in <code>ServerLogic</code> class.
 * 
 * @author kong
 *
 */
public enum LogicEvent {

	/**
	 * When a player is forced to leave his current room. It means this player was
	 * kicked by the host or the entire room was removed.
	 * 
	 * @parameter [0] a player who was forced to leave @see {@link AbstractPlayer}
	 * 
	 * @return <code>null</code>
	 */
	FORCE_PLAYER_LEAVE_ROOM,

	/**
	 * When a client is disconnected from your server for any reason, you can handle
	 * it in this event.
	 * 
	 * @parameter [0] the connection @see {@link Connection}
	 * @parameter [1] If the value is set to <code>true</code>, when the client is
	 *            disconnected, it's player can be held for an interval time (you
	 *            can configure this interval time in your configurations)
	 * 
	 * @return <code>null</code>
	 */
	CONNECTION_CLOSE,

	/**
	 * The exceptions occur when the server handles messages from a client.
	 * 
	 * @parameter [0] the connection's id in string
	 * @parameter [1] the connection @see {@link Connection}
	 * @parameter [2] the exception will occur @see {@link Throwable}
	 * 
	 * @return <code>null</code>
	 */
	CONNECTION_EXCEPTION,

	/**
	 * This event is called when you let the player leave by his desire.
	 * 
	 * @parameter [0] the player's name @see {@link AbstractPlayer#getName()}
	 * 
	 * @return <code>null</code>
	 */
	MANUALY_CLOSE_CONNECTION,

	/**
	 * When a new connection is created. This connection's type must be TCP or
	 * WebSocket (The main connection).
	 * 
	 * @parameter [0] the maximum of players count that your server can handle
	 * @parameter [1] If the value is set to <code>true</code>, when the client is
	 *            disconnected, it's player can be held for an interval time (you
	 *            can configure this interval time in your configurations)
	 * @parameter [2] the new connection @see {@link Connection}
	 * @parameter [3] the message @see {@link TObject} which is sent by the first
	 *            times from its new connection
	 * 
	 * @return <code>null</code>
	 */
	CREATE_NEW_CONNECTION,

	/**
	 * You can handle the message sent from a connection here. This connection's
	 * type is TCP or WebSocket.
	 * 
	 * @parameter [0] the connection @see {@link Connection}
	 * @parameter [1] the message @see {@link TObject} which is sent by its
	 *            corresponding connection
	 * 
	 * @return <code>null</code>
	 */
	SOCKET_HANDLE,

	/**
	 * You can handle the message sent from a player here. This connection's type is
	 * UDP.
	 * 
	 * @parameter [0] the player @see {@link AbstractPlayer}
	 * @parameter [1] the message @see {@link TObject} which is sent by its
	 *            corresponding player
	 * 
	 * @return <code>null</code>
	 */
	DATAGRAM_HANDLE,

	/**
	 * Retrieve a player by his name.
	 * 
	 * @parameter [0] the player's name (unique)
	 * 
	 * @return Returns a player @see {@link AbstractPlayer} if he has existed,
	 *         <code>null</code> otherwise
	 */
	GET_PLAYER,

}
