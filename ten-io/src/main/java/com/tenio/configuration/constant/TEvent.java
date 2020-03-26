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

import com.tenio.configuration.BaseConfiguration;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.AbstractRoom;
import com.tenio.entities.element.TObject;
import com.tenio.extension.AbstractExtensionHandler;
import com.tenio.extension.IExtension;
import com.tenio.network.Connection;

/**
 * This Enum defines all events in your server. You can handle these events by
 * implementing your own logic in the {@link IExtension#init()} method. The
 * logic class must be an inheritance of {@link AbstractExtensionHandler} class.
 * 
 * @author kong
 *
 */
public enum TEvent {

	/**
	 * When the client sends its first request to your server and is made a valid
	 * connection. A connection can be some types of TCP, UDP or WebSocket. One it's
	 * established, you can make a bi-direction communication between the server and
	 * that client.
	 * 
	 * @parameter [0] a valid connection @see {@link Connection}
	 * @parameter [1] a message that sent from the client @see {@link TObject}
	 * 
	 * @return <code>null</code>
	 */
	CONNECTION_SUCCESS,

	/**
	 * When a connection was established, the next requests are handled in this
	 * event.
	 * 
	 * @parameter [0] the current connection @see {@link Connection}
	 * @parameter [1] a message that sent from the client @see {@link TObject}
	 * 
	 * @return <code>null</code>
	 */
	RECEIVED_FROM_CONNECTION,

	/**
	 * When the client sends its first request to your server and because of some
	 * reason it was treated as an invalid connection. The communication with the
	 * current client is closed immediately.
	 * 
	 * @parameter [0] an invalid connection @see {@link Connection}
	 * @parameter [1] a reason why it was refused. It was defined in
	 *            {@link ErrorMsg} class in string type
	 * 
	 * @return <code>null</code>
	 */
	CONNECTION_FAILED,

	/**
	 * This event let you know when one connection has login successful and became a
	 * valid player. You can now inform the client with a message.
	 * 
	 * @parameter [0] a new valid player @see {@link AbstractPlayer}
	 * 
	 * @return <code>null</code>
	 */
	PLAYER_IN_SUCCESS,

	/**
	 * When a connection has failed to login to your server for some reason. At this
	 * time it's treated as an invalid player. You can send an information message
	 * to the current "player" connection.
	 * 
	 * @parameter [0] an invalid player, it will be removed after this event @see
	 *            {@link AbstractPlayer}
	 * @parameter [1] a reason why it was refused. It was defined in
	 *            {@link ErrorMsg} class in string type
	 * 
	 * @return <code>null</code>
	 */
	PLAYER_IN_FAILED,

	/**
	 * When a player is disconnected from your server without its desired, the
	 * player instance is not be removed immediately. In allowed time (which can be
	 * set in the configuration @see {@link BaseConfiguration}) the current client
	 * can request a reconnection. You can handle your own reconnect logic in here.
	 * In this event, you can inform the current client to know that his request was
	 * failed.
	 * 
	 * @parameter [0] a new connection from the current client (the old one was
	 *            removed automatically) @see {@link Connection}
	 * @parameter [1] a message from the current client which needs to hold some
	 *            credentials information @see {@link TObject}
	 * 
	 * @return if you allow the client can be re-connected, returns the
	 *         corresponding player @see {@link AbstractPlayer}. If not, return
	 *         <code>null</code>
	 */
	PLAYER_RECONNECT_REQUEST,

	/**
	 * When a client makes reconnection successful and you can inform him here by a
	 * message.
	 * 
	 * @parameter [0] a corresponding player @see {@link AbstractPlayer}
	 * 
	 * @return <code>null</code>
	 */
	PLAYER_RECONNECT_SUCCESS,

	/**
	 * When a player is in IDLE status in a long time (exceeded the time out, that
	 * can be defined in configuration @see {@link BaseConfiguration}). For more
	 * details: in a long time without sending or receiving message that will be
	 * treated as time out. After this event, the player will be log out of your
	 * server.
	 * 
	 * @parameter [0] a will-be-log-out player, you can inform him by a message @see
	 *            {@link AbstractPlayer}
	 * 
	 * @return <code>null</code>
	 */
	PLAYER_TIMEOUT,

	/**
	 * When you send a message from your server to one client it can be seen here.
	 * This is helpful in case you want your bots to know what's happening.
	 * 
	 * @parameter [0] the player which will be received your message @see
	 *            {@link AbstractPlayer}
	 * @parameter [1] this message was sent by the main connection (TCP) or the sub
	 *            connection (UDP). In the case of WebSocket, it can only send in
	 *            the main connection. It returns <code>true</code> for the sub
	 *            connection and <code>false</code> for the main connection
	 * @parameter [2] the sent message @see {@link TObject}
	 * 
	 * @return <code>null</code>
	 */
	SEND_TO_PLAYER,

	/**
	 * With a valid player, his message can be seen here. This message is sent from
	 * a client to your server.
	 * 
	 * @parameter [0] the player which sent message @see {@link AbstractPlayer} to
	 *            your server
	 * @parameter [1] this message was sent by the main connection (TCP) or the sub
	 *            connection (UDP). In the case of WebSocket, it can only send in
	 *            the main connection. It returns <code>true</code> for the sub
	 *            connection and <code>false</code> for the main connection
	 * @parameter [2] the received message @see {@link TObject}
	 * 
	 * @return <code>null</code>
	 */
	RECEIVED_FROM_PLAYER,

	/**
	 * Created a new room. A room is a group of some players.
	 * 
	 * @parameter [0] a new created room @see {@link AbstractRoom}
	 * 
	 * @return <code>null</code>
	 */
	CREATED_ROOM,

	/**
	 * The room will be removed, but in this event, all the players and their state
	 * are preserved. You can handle your own logic here. After that, all the data
	 * of this room (include players' data) will be deleted and the room is removed
	 * from the room's list. All players in this room will be forced to leave.
	 * 
	 * @parameter [0] a will-be-deleted room @see {@link AbstractRoom}
	 * 
	 * @return <code>null</code>
	 */
	REMOVE_ROOM,

	/**
	 * When a player wants to join one room, its request can be seen here. This
	 * event occur once with a pair of player and room. These steps will be, first,
	 * the player leaves his current room and joins a new room.
	 * 
	 * @parameter [0] a player that wants to join one room @see
	 *            {@link AbstractPlayer}
	 * @parameter [1] the desired room @see {@link AbstractRoom}
	 * @parameter [2] this event's result. If the player joins his desired room
	 *            successful, it will return <code>true</code>. Otherwise, it will
	 *            return <code>false</code>
	 * 
	 * @parameter [3] If the player can not join his desired room, the reason can be
	 *            found here @see {@link ErrorMsg}. Its value can be
	 *            <code>null</code>
	 * 
	 * @return <code>null</code>
	 */
	PLAYER_JOIN_ROOM,

	/**
	 * This event occurs when a player before leaves his current room. You can
	 * handle your own logic here. After that, the room attribute in the player will
	 * be removed, as well as, this player reference will be removed from a players
	 * list in the current room.
	 * 
	 * @parameter [0] the current player @see {@link AbstractPlayer}
	 * @parameter [1] the player's current room @see {@link AbstractRoom}
	 * 
	 * @return <code>null</code>
	 */
	PLAYER_BEFORE_LEAVE_ROOM,

	/**
	 * The player just finished left his current room. Now all related data between
	 * this player and the room has been deleted.
	 * 
	 * @parameter [0] the current player @see {@link AbstractPlayer}
	 * @parameter [1] the room without the player above @see {@link AbstractRoom}
	 * @parameter [2] force: for some reason, a player will be left his room
	 *            unexpected. it returns <code>true</code> if the player is forced
	 *            to leave. Otherwise, return <code>false</code>
	 * 
	 * @return <code>null</code>
	 */
	PLAYER_LEFT_ROOM,

	/**
	 * When a connection between one player and your service is closed (for any
	 * reason), this event occurs.
	 * 
	 * @parameter [0] the disconnected player @see {@link AbstractPlayer}
	 * 
	 * @return <code>null</code>
	 */
	DISCONNECT_PLAYER,

	/**
	 * When a connection between one non-connection player and your service is
	 * closed (for any reason), this event occurs.
	 * 
	 * @parameter [0] the disconnected connection @see {@link Connection}
	 * 
	 * @return <code>null</code>
	 */
	DISCONNECT_CONNECTION,

	/**
	 * You can see the number of concurrent users (CCU) in period time. This scanned
	 * time can be changed in configuration @see {@link BaseConfiguration}.
	 * 
	 * @parameter [0] the number of current players that have connection
	 * @parameter [1] the number of all players (players and your bots)
	 * 
	 * @return <code>null</code>
	 */
	CCU,

	/**
	 * In this server, a UDP connection is treated as a sub-connection. That means
	 * you need to create one main connection between one client and the server
	 * first (a TCP connection). When it's finished, that client can send a request
	 * for making a link.
	 * 
	 * @parameter [0] the message from one client needs to hold some credentials
	 *            data so that you can return him a corresponding value @see
	 *            {@link TObject}
	 * 
	 * @return if the client is allowed to attach a UDP connection, return the
	 *         corresponding player @see {@link AbstractPlayer}. Otherwise, return
	 *         <code>null</code>
	 */
	ATTACH_UDP_REQUEST,

	/**
	 * When a UDP connection link is established, you can inform its own player
	 * here.
	 * 
	 * @parameter [0] the corresponding player @see {@link AbstractPlayer}
	 * 
	 * @return <code>null</code>
	 */
	ATTACH_UDP_SUCCESS,

	/**
	 * The client failed to attach his desired UDP connection. The reason can be
	 * returned here. This event only for informing you about the current situation.
	 * If you want to let your client know about his unsuccess, you can handle it on
	 * {@link TEvent#ATTACH_UDP_REQUEST}.
	 * 
	 * @parameter [0] the message received from one client @see {@link TObject}
	 * @parameter [1] the reason for failed @see {@link ErrorMsg} in string type
	 * 
	 * @return <code>null</code>
	 */
	ATTACH_UDP_FAILED

}
