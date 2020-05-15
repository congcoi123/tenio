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
import com.tenio.entity.AbstractPlayer;
import com.tenio.entity.AbstractRoom;
import com.tenio.entity.element.TObject;
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
	 * that client. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> a valid connection, see {@link Connection}</li>
	 * <li><b>parameter[1]</b> a message that sent from the client, see
	 * {@link TObject}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	CONNECTION_SUCCESS,

	/**
	 * When a connection was established, the next requests are handled in this
	 * event. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the current connection, see {@link Connection}</li>
	 * <li><b>parameter[1]</b> a message that sent from the client, see
	 * {@link TObject}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	RECEIVED_FROM_CONNECTION,

	/**
	 * When the client sends its first request to your server and because of some
	 * reason it was treated as an invalid connection. The communication with the
	 * current client is closed immediately. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> an invalid connection, see {@link Connection}</li>
	 * <li><b>parameter[1]</b> a reason why it was refused. It was defined in
	 * {@link ErrorMsg} class in string type</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	CONNECTION_FAILED,

	/**
	 * This event let you know when one connection has login successful and became a
	 * valid player. You can now inform the client with a message. <br>
	 * <ul>
	 * <li><b>@parameter[0]</b> a new valid player, see {@link AbstractPlayer}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	PLAYER_IN_SUCCESS,

	/**
	 * When a connection has failed to login to your server for some reason. At this
	 * time it's treated as an invalid player. You can send an information message
	 * to the current "player" connection. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> an invalid player, it will be removed after this
	 * event, see {@link AbstractPlayer}</li>
	 * <li><b>parameter[1]</b> a reason why it was refused. It was defined in
	 * {@link ErrorMsg} class in string type</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	PLAYER_IN_FAILED,

	/**
	 * When a player is disconnected from your server without its desired, the
	 * player instance is not be removed immediately. In allowed time (which can be
	 * set in the configuration: {@link BaseConfiguration}) the current client can
	 * request a reconnection. You can handle your own reconnect logic in here. In
	 * this event, you can inform the current client to know that his request was
	 * failed. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> a new connection from the current client (the old one
	 * was removed automatically), see {@link Connection}</li>
	 * <li><b>parameter[1]</b> a message from the current client which needs to hold
	 * some credentials information, see {@link TObject}</li>
	 * </ul>
	 * 
	 * Return if you allow the client can be re-connected, return the corresponding
	 * player: {@link AbstractPlayer}, return <b>null</b> otherwise
	 */
	PLAYER_RECONNECT_REQUEST,

	/**
	 * When a client makes reconnection successful and you can inform him here by a
	 * message. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> a corresponding player, see
	 * {@link AbstractPlayer}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	PLAYER_RECONNECT_SUCCESS,

	/**
	 * When a player is in IDLE status in a long time (exceeded the time out, that
	 * can be defined in configuration: {@link BaseConfiguration}). For more
	 * details: in a long time without sending or receiving message that will be
	 * treated as time out. After this event, the player will be log out of your
	 * server. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> a will-be-log-out player, you can inform him by a
	 * message, see {@link AbstractPlayer}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	PLAYER_TIMEOUT,

	/**
	 * When you send a message from your server to one client it can be seen here.
	 * This is helpful in case you want your BOTs to know what's happening. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the player which will be received your message, see
	 * {@link AbstractPlayer}</li>
	 * <li><b>parameter[1]</b> this message was sent by the main connection (TCP) or
	 * the sub connection (UDP). In the case of WebSocket, it can only send in the
	 * main connection. It returns <b>true</b> for the sub connection and
	 * <b>false</b> for the main connection</li>
	 * <li><b>parameter[2]</b> the sent message, see {@link TObject}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	SEND_TO_PLAYER,

	/**
	 * With a valid player, his message can be seen here. This message is sent from
	 * a client to your server. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the player which sent message, see
	 * {@link AbstractPlayer} to your server</li>
	 * <li><b>parameter[1]</b> this message was sent by the main connection (TCP) or
	 * the sub connection (UDP). In the case of WebSocket, it can only send in the
	 * main connection. It returns <b>true</b> for the sub connection and
	 * <b>false</b> for the main connection</li>
	 * <li><b>parameter[2]</b> the received message, see {@link TObject}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	RECEIVED_FROM_PLAYER,

	/**
	 * Created a new room. A room ({@link AbstractRoom}) is a group of some players.
	 * <br>
	 * <ul>
	 * <li><b>parameter[0]</b> a new created room, see {@link AbstractRoom}</li>
	 * <li><b>parameter[1]</b> a reason why it was refused. It was defined in
	 * {@link ErrorMsg} class in string type</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	CREATED_ROOM,

	/**
	 * The room will be removed, but in this event, all the players and their state
	 * are preserved. You can handle your own logic here. After that, all the data
	 * of this room (include players' data) will be deleted and the room is removed
	 * from the room's list. All players in this room will be forced to leave. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> a will-be-deleted room, see {@link AbstractRoom}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	REMOVE_ROOM,

	/**
	 * When a player wants to join one room, its request can be seen here. This
	 * event occur once with a pair of player and room. These steps will be, first,
	 * the player leaves his current room and joins a new room. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> a player that wants to join one room, see
	 * {@link AbstractPlayer}</li>
	 * <li><b>parameter[1]</b> the desired room, see {@link AbstractRoom}</li>
	 * <li><b>parameter[2]</b> this event's result. If the player joins his desired
	 * room successful, it will return <b>true</b>. Otherwise, it will return
	 * <b>false</b></li>
	 * 
	 * <li><b>parameter[3]</b> if the player can not join his desired room, the
	 * reason can be found here, see {@link ErrorMsg}. Its value can be
	 * <b>null</b></li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	PLAYER_JOIN_ROOM,

	/**
	 * This event occurs when a player before leaves his current room. You can
	 * handle your own logic here. After that, the room attribute in the player will
	 * be removed, as well as, this player reference will be removed from a players
	 * list in the current room. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the current player, see {@link AbstractPlayer}</li>
	 * <li><b>parameter[1]</b> the player's current room, see
	 * {@link AbstractRoom}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	PLAYER_BEFORE_LEAVE_ROOM,

	/**
	 * The player just finished left his current room. Now all related data between
	 * this player and the room has been deleted. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the current player, see {@link AbstractPlayer}</li>
	 * <li><b>parameter[1]</b> the room without the player above, see
	 * {@link AbstractRoom}</li>
	 * <li><b>parameter[2]</b> <i>force flag</i> for some reason, a player will be
	 * left his room unexpected. it returns <b>true</b> if the player is forced to
	 * leave. Otherwise, return <b>false</b></li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	PLAYER_LEFT_ROOM,

	/**
	 * When a connection between one player and your service is closed (for any
	 * reason), this event occurs. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the disconnected player, see
	 * {@link AbstractPlayer}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	DISCONNECT_PLAYER,

	/**
	 * When a connection between one non-connection player and your service is
	 * closed (for any reason), this event occurs. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the disconnected connection, see
	 * {@link Connection}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	DISCONNECT_CONNECTION,

	/**
	 * You can see the number of concurrent users (CCU) in period time. This scanned
	 * time can be changed in configuration, see {@link BaseConfiguration}. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the number of current players that have
	 * connection</li>
	 * <li><b>parameter[1]</b> the number of all players (players and your
	 * BOTs)</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	CCU,

	/**
	 * In this server, you can create other sub connections. That means you need to
	 * create one main connection between one client and the server first (a TCP
	 * connection). When it's finished, that client can send a request for making a
	 * link. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the message from one client needs to hold some
	 * credentials data so that you can return him a corresponding value, see
	 * {@link TObject}</li>
	 * </ul>
	 * 
	 * Return if the client is allowed to attach a UDP connection, return the
	 * corresponding player, see {@link AbstractPlayer}. Otherwise, return
	 * <b>null</b>
	 */
	ATTACH_CONNECTION_REQUEST,

	/**
	 * When a a sub connection link is established, you can inform its own player
	 * here. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the corresponding player, see
	 * {@link AbstractPlayer}</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	ATTACH_CONNECTION_SUCCESS,

	/**
	 * The client failed to attach his desired sub connection. The reason can be
	 * returned here. This event only for informing you about the current situation.
	 * If you want to let your client know about his un-success, you can handle it
	 * on {@link TEvent#ATTACH_UDP_REQUEST}. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> the message received from one client, see
	 * {@link TObject}</li>
	 * <li><b>parameter[1]</b> the reason for failed, see {@link ErrorMsg} in string
	 * type</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	ATTACH_CONNECTION_FAILED,

	/**
	 * The amount of data that can be transmitted in a fixed amount of time. <br>
	 * <ul>
	 * <li><b>parameter[0]</b> Last reading bandwidth (KB/s)</li>
	 * <li><b>parameter[1]</b> Last writing bandwidth (KB/s)</li>
	 * <li><b>parameter[2]</b> Real writing bandwidth (KB/s)</li>
	 * <li><b>parameter[3]</b> Current read bytes (KB)</li>
	 * <li><b>parameter[4]</b> Current written bytes (KB)</li>
	 * <li><b>parameter[5]</b> Real written bytes (KB)</li>
	 * </ul>
	 * 
	 * Return <b>null</b>
	 */
	BANDWIDTH

}
