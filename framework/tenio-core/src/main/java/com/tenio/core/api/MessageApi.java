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
package com.tenio.core.api;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.element.CommonObject;
import com.tenio.common.element.CommonObjectArray;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.pool.IElementsPool;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.entity.IPlayer;
import com.tenio.core.entity.IRoom;
import com.tenio.core.event.IEventManager;
import com.tenio.core.network.IConnection;

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
@ThreadSafe
public final class MessageApi extends AbstractLogger {

	private final IElementsPool<CommonObject> __msgObjectPool;
	private final IElementsPool<CommonObjectArray> __msgArrayPool;
	private final IEventManager __eventManager;

	public MessageApi(IEventManager eventManager, IElementsPool<CommonObject> msgObjectPool,
			IElementsPool<CommonObjectArray> msgArrayPool) {
		__eventManager = eventManager;
		__msgObjectPool = msgObjectPool;
		__msgArrayPool = msgArrayPool;
	}

	/**
	 * Send a message for a connection
	 * 
	 * @param connection See {@link IConnection}
	 * @param key        the key of message
	 * @param value      the value of message
	 */
	public void sendToConnection(IConnection connection, String key, Object value) {
		var message = __msgObjectPool.get();
		message.put(key, value);
		connection.send(message);
		__msgObjectPool.repay(message);
		if (value instanceof CommonObjectArray) {
			__msgArrayPool.repay((CommonObjectArray) value);
		}
	}

	/**
	 * Send a message to a connection
	 * 
	 * Must use {@link #getMessageObjectArray()} to create data array package for
	 * avoiding memory leak.
	 * 
	 * @param connection See {@link IConnection}
	 * @param key        the key of message
	 * @param value      the value of message
	 * @param keyData    the key of message's data
	 * @param data       the main data of message, see: {@link CommonObjectArray}
	 */
	public void sendToConnection(IConnection connection, String key, Object value,
			String keyData, CommonObjectArray data) {
		var message = __msgObjectPool.get();
		message.put(key, value);
		message.put(keyData, data);
		connection.send(message);
		__msgObjectPool.repay(message);
		__msgArrayPool.repay(data);
	}

	/**
	 * Send a message method to a player
	 * 
	 * @param player          See {@link IPlayer}
	 * @param connectionIndex the index of connection in current player
	 * @param message         the sending message
	 */
	private void __send(IPlayer player, int connectionIndex, CommonObject message) {
		// update time to check TIMEOUT
		player.setCurrentWriterTime();
		// send to CLIENT (connection)
		if (player.hasConnection(connectionIndex)) {
			player.getConnection(connectionIndex).send(message);
		}
		__eventManager.getExtension().emit(ExtEvent.SEND_MESSAGE_TO_PLAYER, player, connectionIndex, message);
	}

	/**
	 * Send a message to player via his connection
	 * 
	 * @param player See {@link IPlayer}
	 * @param connectionIndex  the index of connection in current player
	 * @param key    the key of message
	 * @param value  the value of message
	 */
	public void sendToPlayer(IPlayer player, int connectionIndex, String key, Object value) {
		var message = __msgObjectPool.get();
		message.put(key, value);
		__send(player, connectionIndex, message);
		__msgObjectPool.repay(message);
		if (value instanceof CommonObjectArray) {
			__msgArrayPool.repay((CommonObjectArray) value);
		}
	}

	/**
	 * Send a message to a player
	 * 
	 * Must use {@link #getMessageObjectArray()} to create data array package for
	 * avoiding memory leak.
	 * 
	 * @param player          the desired player
	 * @param connectionIndex the index of connection in current player
	 * @param key             the key of message
	 * @param value           the value of message
	 * @param keyData         the key of message's data
	 * @param data            the message data, see: {@link CommonObjectArray}
	 */
	public void sendToPlayer(IPlayer player, int connectionIndex, String key, Object value,
			String keyData, CommonObjectArray data) {
		var message = __msgObjectPool.get();
		message.put(key, value);
		message.put(keyData, data);
		__send(player, connectionIndex, message);
		__msgObjectPool.repay(message);
		__msgArrayPool.repay(data);
	}

	/**
	 * Send a message to all players of one room
	 * 
	 * @param room            the desired room
	 * @param connectionIndex the index of connection in current player
	 * @param key             the key of message
	 * @param value           the value of message
	 */
	public void sendToRoom(IRoom room, int connectionIndex, String key, Object value) {
		var message = __msgObjectPool.get();
		message.put(key, value);
		var players = room.getPlayers().values();
		for (var player : players) {
			__send(player, connectionIndex, message);
		}
		__msgObjectPool.repay(message);
		if (value instanceof CommonObjectArray) {
			__msgArrayPool.repay((CommonObjectArray) value);
		}
	}

	/**
	 * Send a message to all players on one room
	 * 
	 * Must use {@link #getMessageObjectArray()} to create data array package for
	 * avoiding memory leak.
	 * 
	 * @param room            the desired room
	 * @param connectionIndex the index of connection in current player
	 * @param key             the key of message
	 * @param value           the value of message
	 * @param keyData         the key of message's data
	 * @param data            the message's data, see: {@link CommonObjectArray}
	 */
	public void sendToRoom(IRoom room, int connectionIndex, String key, Object value,
			String keyData, CommonObjectArray data) {
		var message = __msgObjectPool.get();
		message.put(key, value);
		message.put(keyData, data);
		var players = room.getPlayers().values();
		for (var player : players) {
			__send(player, connectionIndex, message);
		}
		__msgObjectPool.repay(message);
		__msgArrayPool.repay(data);
	}

	/**
	 * Send a message to all players in one room except the desired player
	 * 
	 * @param player          the desired player
	 * @param connectionIndex the index of connection in current player
	 * @param key             the key of message
	 * @param value           the value of message
	 */
	public void sendToRoomIgnorePlayer(IPlayer player, int connectionIndex, String key,
			Object value) {
		var room = player.getCurrentRoom();
		var message = __msgObjectPool.get();
		message.put(key, value);
		var players = room.getPlayers().values();
		for (var other : players) {
			if (!other.getName().equals(player.getName())) {
				__send(other, connectionIndex, message);
			}
		}
		__msgObjectPool.repay(message);
		if (value instanceof CommonObjectArray) {
			__msgArrayPool.repay((CommonObjectArray) value);
		}
	}

	/**
	 * Send a message to all players in one room except the desired player
	 * 
	 * Must use {@link #getMessageObjectArray()} to create data array package for
	 * avoiding memory leak.
	 * 
	 * @param player          the desired player
	 * @param connectionIndex the index of connection in current player
	 * @param key             the key of message
	 * @param value           the value of message
	 * @param keyData         the key of message's data
	 * @param data            the message's data, see: {@link CommonObjectArray}
	 */
	public void sendToRoomIgnorePlayer(IPlayer player, int connectionIndex, String key,
			Object value, String keyData, CommonObjectArray data) {
		var room = player.getCurrentRoom();
		var message = __msgObjectPool.get();
		message.put(key, value);
		message.put(keyData, data);
		var players = room.getPlayers().values();
		for (var other : players) {
			if (!other.getName().equals(player.getName())) {
				__send(other, connectionIndex, message);
			}
		}
		__msgObjectPool.repay(message);
		__msgArrayPool.repay(data);
	}

	/**
	 * Send a internal server message, the message format need to be recognized by
	 * handler classes
	 * 
	 * @param player          the desired player
	 * @param connectionIndex the index of connection in current player
	 * @param message         the message instance
	 */
	public void sendToInternalServer(IPlayer player, int connectionIndex, CommonObject message) {
		player.setCurrentReaderTime();
		__eventManager.getExtension().emit(ExtEvent.RECEIVED_MESSAGE_FROM_PLAYER, player, connectionIndex, message);
	}

	/**
	 * @return a {@link CommonObjectArray} object from the pooling mechanism
	 */
	public CommonObjectArray getMessageObjectArray() {
		return __msgArrayPool.get();
	}

}
