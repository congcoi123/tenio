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
package com.tenio.api;

import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.AbstractRoom;
import com.tenio.entities.element.TArray;
import com.tenio.entities.element.TObject;
import com.tenio.event.EventManager;
import com.tenio.logger.AbstractLogger;
import com.tenio.network.Connection;
import com.tenio.pool.ArrayPool;
import com.tenio.pool.ObjectPool;

/**
 * This class provides you a necessary interface for sending messages from the
 * server to clients. It uses a pooling mechanism to increase performance. For
 * creating a message, it's better that decrease this content as less as
 * possible (for fast transferring, save bandwidth, reduce risk, ...). For
 * example, a message should only hold an array, and the client will reference
 * the defined APIs to retrieve his desired values by the array's indexes.
 * 
 * @author kong
 * 
 */
public final class MessageApi extends AbstractLogger {

	/**
	 * @see ArrayPool
	 */
	private ArrayPool __arrayPool = new ArrayPool();
	/**
	 * @see ObjectPool
	 */
	private ObjectPool __objectPool = new ObjectPool();

	/**
	 * Send a message for a connection
	 * 
	 * @param connection @see {@link Connection}
	 * @param key        the key of message
	 * @param value      the value of message
	 */
	public void sendToConnection(Connection connection, String key, Object value) {
		var message = __objectPool.get();
		message.put(key, value);
		connection.send(message);
		__objectPool.repay(message);
		if (value instanceof TArray) {
			__arrayPool.repay((TArray) value);
		}
	}

	/**
	 * Send a message to a connection
	 * 
	 * Must use @see {@link #getArrayPack()} to create data array package for
	 * avoiding memory leak.
	 * 
	 * @param connection @see {@link Connection}
	 * @param key        the key of message
	 * @param value      the value of message
	 * @param keyData    the key of message's data
	 * @param data       the main data of message @see {@link TArray}
	 */
	public void sendToConnection(Connection connection, String key, Object value, String keyData, TArray data) {
		var message = __objectPool.get();
		message.put(key, value);
		message.put(keyData, data);
		connection.send(message);
		__objectPool.repay(message);
		__arrayPool.repay(data);
	}

	/**
	 * Send a message method to a player
	 * 
	 * @param player          @see {@link AbstractPlayer}
	 * @param isSubConnection set <code>true</code> is you want to send to your
	 *                        client a message in sub-connection (UDP)
	 * @param message         the sending message
	 */
	private void __send(AbstractPlayer player, boolean isSubConnection, TObject message) {
		player.currentWriterTime(); // update time to check TIMEOUT
		if (!isSubConnection) {
			if (player.hasConnection()) { // send to CLIENT (connection)
				player.getConnection().send(message);
				debug("SENT", player.getName(), message.toString());
			} else {
				debug("SENT NPC", player.getName(), message.toString());
			}
		} else {
			if (player.hasSubConnection()) { // send to CLIENT (sub-connection)
				player.getSubConnection().send(message);
				debug("SENT SUB", player.getName(), message.toString());
			} else {
				debug("SENT SUB NPC", player.getName(), message.toString());
			}
		}
		EventManager.getEvent().emit(TEvent.SEND_TO_PLAYER, player, isSubConnection, message);
	}

	/**
	 * @see #__send(AbstractPlayer, boolean, TObject)
	 * 
	 * @param key   the key of message
	 * @param value the value of message
	 */
	private void __sendToPlayer(AbstractPlayer player, boolean isSubConnection, String key, Object value) {
		var message = __objectPool.get();
		message.put(key, value);
		__send(player, isSubConnection, message);
		__objectPool.repay(message);
		if (value instanceof TArray) {
			__arrayPool.repay((TArray) value);
		}
	}

	/**
	 * Send a message to player via his main connection
	 * 
	 * @see #__sendToPlayer(AbstractPlayer, boolean, String, Object)
	 * 
	 * @param player the desired player
	 * @param key    the key of message
	 * @param value  the value of message
	 */
	public void sendToPlayer(AbstractPlayer player, String key, Object value) {
		__sendToPlayer(player, false, key, value);
	}

	/**
	 * Send a message to player via his sub-connection
	 * 
	 * @see #__sendToPlayer(AbstractPlayer, boolean, String, Object)
	 * 
	 * @param player the desired player
	 * @param key    the key of message
	 * @param value  the value of message
	 */
	public void sendToPlayerSub(AbstractPlayer player, String key, Object value) {
		__sendToPlayer(player, true, key, value);
	}

	/**
	 * Send a message to a player
	 * 
	 * Must use @see {@link #getArrayPack()} to create data array package for
	 * avoiding memory leak.
	 * 
	 * @param player          the desired player
	 * @param isSubConnection set <code>true</code> is you want to send to your
	 *                        client a message in sub-connection (UDP)
	 * @param key             the key of message
	 * @param value           the value of message
	 * @param keyData         the key of message's data
	 * @param data            the message data @see {@link TArray}
	 */
	private void __sendToPlayer(AbstractPlayer player, boolean isSubConnection, String key, Object value,
			String keyData, TArray data) {
		var message = __objectPool.get();
		message.put(key, value);
		message.put(keyData, data);
		__send(player, isSubConnection, message);
		__objectPool.repay(message);
		__arrayPool.repay(data);
	}

	/**
	 * Send a message to a player via his main connection
	 * 
	 * @see #__sendToPlayer(AbstractPlayer, boolean, String, Object, String, TArray)
	 * 
	 * @param player  the desired player
	 * @param key     the key of message
	 * @param value   the value of message
	 * @param keyData the key of message's data
	 * @param data    the message's data @see {@link TArray}
	 */
	public void sendToPlayer(AbstractPlayer player, String key, Object value, String keyData, TArray data) {
		__sendToPlayer(player, false, key, value, keyData, data);
	}

	/**
	 * Send a message to a player via his sub-connection
	 * 
	 * @see #__sendToPlayer(AbstractPlayer, boolean, String, Object, String, TArray)
	 * 
	 * @param player  the desired player
	 * @param key     the key of message
	 * @param value   the value of message
	 * @param keyData the key of message's data
	 * @param data    the message's data @see {@link TArray}
	 */
	public void sendToPlayerSub(AbstractPlayer player, String key, Object value, String keyData, TArray data) {
		__sendToPlayer(player, true, key, value, keyData, data);
	}

	/**
	 * Send a message to all players of one room
	 * 
	 * @param room            the desired room
	 * @param isSubConnection set <code>true</code> is you want to send to your
	 *                        client a message in sub-connection (UDP)
	 * @param key             the key of message
	 * @param value           the value of message
	 */
	private void __sendToRoom(AbstractRoom room, boolean isSubConnection, String key, Object value) {
		var message = __objectPool.get();
		message.put(key, value);
		for (var player : room.getPlayers().values()) {
			__send(player, isSubConnection, message);
		}
		__objectPool.repay(message);
		if (value instanceof TArray) {
			__arrayPool.repay((TArray) value);
		}
	}

	/**
	 * Send a message to all players on one room via their main connection
	 * 
	 * @see #__sendToRoom(AbstractRoom, boolean, String, Object)
	 * 
	 * @param room  the desired room
	 * @param key   the key of message
	 * @param value the value of message
	 */
	public void sendToRoom(AbstractRoom room, String key, Object value) {
		__sendToRoom(room, false, key, value);
	}

	/**
	 * Send a message to all players on one room via their sub-connection
	 * 
	 * @see #__sendToRoom(AbstractRoom, boolean, String, Object)
	 * 
	 * @param room  the desired room
	 * @param key   the key of message
	 * @param value the value of message
	 */
	public void sendToRoomSub(AbstractRoom room, String key, Object value) {
		__sendToRoom(room, true, key, value);
	}

	/**
	 * Send a message to all players on one room
	 * 
	 * Must use @see {@link #getArrayPack()} to create data array package for
	 * avoiding memory leak.
	 * 
	 * @param room            the desired room
	 * @param isSubConnection set <code>true</code> is you want to send to your
	 *                        client a message in sub-connection (UDP)
	 * @param key             the key of message
	 * @param value           the value of message
	 * @param keyData         the key of message's data
	 * @param data            the message's data @see {@link TArray}
	 */
	private void __sendToRoom(AbstractRoom room, boolean isSubConnection, String key, Object value, String keyData,
			TArray data) {
		var message = __objectPool.get();
		message.put(key, value);
		message.put(keyData, data);
		for (var player : room.getPlayers().values()) {
			__send(player, isSubConnection, message);
		}
		__objectPool.repay(message);
		__arrayPool.repay(data);
	}

	/**
	 * Send a message to all players on one room via their main connection
	 * 
	 * @see #__sendToRoom(AbstractRoom, boolean, String, Object, String, TArray)
	 * 
	 * @param room    the desired room
	 * @param key     the key of message
	 * @param value   the value of message
	 * @param keyData the key of message's data
	 * @param data    the messate's data @see {@link TArray}
	 */
	public void sendToRoom(AbstractRoom room, String key, Object value, String keyData, TArray data) {
		__sendToRoom(room, false, key, value, keyData, data);
	}

	/**
	 * Send a message to all players on one room via their sub-connection
	 * 
	 * @see #__sendToRoom(AbstractRoom, boolean, String, Object, String, TArray)
	 * 
	 * @param room    the desired room
	 * @param key     the key of message
	 * @param value   the value of message
	 * @param keyData the key of message's data
	 * @param data    the messate's data @see {@link TArray}
	 */
	public void sendToRoomSub(AbstractRoom room, String key, Object value, String keyData, TArray data) {
		__sendToRoom(room, true, key, value, keyData, data);
	}

	/**
	 * Send a message to all players in one room except the desired player
	 * 
	 * @param player          the desired player
	 * @param isSubConnection set <code>true</code> is you want to send to your
	 *                        client a message in sub-connection (UDP)
	 * @param key             the key of message
	 * @param value           the value of message
	 */
	private void __sendToRoomIgnorePlayer(AbstractPlayer player, boolean isSubConnection, String key, Object value) {
		var room = player.getRoom();
		var message = __objectPool.get();
		message.put(key, value);
		for (var p : room.getPlayers().values()) {
			if (!p.equals(player)) {
				__send(p, isSubConnection, message);
			}
		}
		__objectPool.repay(message);
		if (value instanceof TArray) {
			__arrayPool.repay((TArray) value);
		}
	}

	/**
	 * Send a message to all players in one room except the desired player via their
	 * main connection
	 * 
	 * @see #__sendToRoomIgnorePlayer(AbstractPlayer, boolean, String, Object)
	 * 
	 * @param player the desired player
	 * @param key    the key of message
	 * @param value  the value of message
	 */
	public void sendToRoomIgnorePlayer(AbstractPlayer player, String key, Object value) {
		__sendToRoomIgnorePlayer(player, false, key, value);
	}

	/**
	 * Send a message to all players in one room except the desired player via their
	 * sub-connection
	 * 
	 * @see #__sendToRoomIgnorePlayer(AbstractPlayer, boolean, String, Object)
	 * 
	 * @param player the desired player
	 * @param key    the key of message
	 * @param value  the value of message
	 */
	public void sendToRoomIgnorePlayerSub(AbstractPlayer player, String key, Object value) {
		__sendToRoomIgnorePlayer(player, true, key, value);
	}

	/**
	 * Send a message to all players in one room except the desired player
	 * 
	 * Must use @see {@link #getArrayPack()} to create data array package for
	 * avoiding memory leak.
	 * 
	 * @param player          the desired player
	 * @param isSubConnection set <code>true</code> is you want to send to your
	 *                        client a message in sub-connection (UDP)
	 * @param key             the key of message
	 * @param value           the value of message
	 * @param keyData         the key of message's data
	 * @param data            the message's data @see {@link TArray}
	 */
	private void __sendToRoomIgnorePlayer(AbstractPlayer player, boolean isSubConnection, String key, Object value,
			String keyData, TArray data) {
		var room = player.getRoom();
		var message = __objectPool.get();
		message.put(key, value);
		message.put(keyData, data);
		for (var p : room.getPlayers().values()) {
			if (!p.equals(player)) {
				__send(p, isSubConnection, message);
			}
		}
		__objectPool.repay(message);
		__arrayPool.repay(data);
	}

	/**
	 * Send a message to all players in one room except the desired player via their
	 * main connection
	 * 
	 * @see #__sendToRoomIgnorePlayer(AbstractPlayer, boolean, String, Object,
	 *      String, TArray)
	 * 
	 * @param player  the desired player
	 * @param key     the key of message
	 * @param value   the value of message
	 * @param keyData the key of message's data
	 * @param data    the message's data @see {@link TArray}
	 */
	public void sendToRoomIgnorePlayer(AbstractPlayer player, String key, Object value, String keyData, TArray data) {
		__sendToRoomIgnorePlayer(player, false, key, value, keyData, data);
	}

	/**
	 * Send a message to all players in one room except the desired player via their
	 * sub-connection
	 * 
	 * @see #__sendToRoomIgnorePlayer(AbstractPlayer, boolean, String, Object,
	 *      String, TArray)
	 * 
	 * @param player  the desired player
	 * @param key     the key of message
	 * @param value   the value of message
	 * @param keyData the key of message's data
	 * @param data    the message's data @see {@link TArray}
	 */
	public void sendToRoomIgnorePlayerSub(AbstractPlayer player, String key, Object value, String keyData,
			TArray data) {
		__sendToRoomIgnorePlayer(player, true, key, value, keyData, data);
	}

	/**
	 * @return Returns a @see {@link TArray} object from the pooling mechanism
	 */
	public TArray getArrayPack() {
		return __arrayPool.get();
	}

}
