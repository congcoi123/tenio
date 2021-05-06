/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
package com.tenio.core.network.entity.connection.implement;

import com.tenio.core.entity.Player;
import com.tenio.core.event.EventManager;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.connection.Connection;

/**
 * A connection is created when the first request from client reach and pass in
 * your server. The connection has some types based on its client's request type
 * (TCP, UDP, WebSocket). It can work stand-alone for some cases by but most of
 * the time, it is a part of a particular player. You can send a message from
 * the server-side to one client via its corresponding connection/player. Once
 * the connection is established, it should be attached to a player or be
 * refused (be disconnected) as soon as possible.
 * 
 * @author kong
 * 
 */
public abstract class AbstractConnection implements Connection {

	/**
	 * {@link EventManager}
	 */
	private final EventManager __eventManager;
	/**
	 * {@link Player#getName()}
	 */
	private volatile String __playerName;
	/**
	 * Save the client's address
	 */
	private volatile String __address;
	/**
	 * Type of the connection
	 */
	private final TransportType __type;
	/**
	 * The order of connection in one player
	 */
	private final int __index;

	public AbstractConnection(EventManager eventManager, TransportType type, int index) {
		__eventManager = eventManager;
		__type = type;
		__index = index;
	}

	@Override
	public EventManager getEventManager() {
		return __eventManager;
	}

	@Override
	public TransportType getType() {
		return __type;
	}

	@Override
	public boolean isType(TransportType type) {
		return (__type == type);
	}

	@Override
	public int getIndex() {
		return __index;
	}

	@Override
	public String getPlayerName() {
		return __playerName;
	}

	@Override
	public void setPlayerName(String playerName) {
		__playerName = playerName;
	}

	@Override
	public void removePlayerName() {
		__playerName = null;
	}

	@Override
	public void setAddress(String address) {
		__address = address;
	}

	@Override
	public String getAddress() {
		return __address;
	}

}
