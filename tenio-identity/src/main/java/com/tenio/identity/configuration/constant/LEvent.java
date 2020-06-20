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
package com.tenio.identity.configuration.constant;

import com.tenio.identity.entity.AbstractPlayer;
import com.tenio.identity.entity.element.TObject;
import com.tenio.identity.network.Connection;

/**
 * This Enum defines all logic events in the main thread. All the process should
 * be handled in <b>ServerLogic</b> class.
 * 
 * @author kong
 *
 */
public enum LEvent {

	/**
	 * When a client is disconnected from your server for any reason, you can handle
	 * it in this event. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the connection, see {@link Connection}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	CONNECTION_CLOSE,

	/**
	 * The exceptions occur when the server handles messages from a client. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the connection's id in string</li>
	 * <li><b>parameter[1]</b> the connection, see {@link Connection}</li>
	 * <li><b>parameter[2]</b> the exception will occur, see {@link Throwable}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	CONNECTION_EXCEPTION,

	/**
	 * This event is called when you let the player leave by his desire. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the player's name, see
	 * {@link AbstractPlayer#getName()}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	MANUALY_CLOSE_CONNECTION,

	/**
	 * When a player is forced to leave his current room. It means this player was
	 * kicked by the host or the entire room was removed. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> a player who was forced to leave, see
	 * {@link AbstractPlayer}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	FORCE_PLAYER_LEAVE_ROOM,

	/**
	 * You can handle the message sent from a connection here.
	 * <ul>
	 * <li><b>parameter[0]</b> index: the connection's order in one player (start
	 * from <b>0</b>)</li>
	 * <li><b>parameter[1]</b> the connection (can be <b>null</b>) that is retrieved
	 * from the corresponding channel, see {@link Connection}</li>
	 * <li><b>parameter[2]</b> the message, see {@link TObject} which is sent by its
	 * corresponding connection</li>
	 * <li><b>parameter[3]</b> the connection object which is used to assigned to a
	 * player (can be <b>null</b>, see {@link Connection}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	CHANNEL_HANDLE

}
