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
package com.tenio.core.network;

import java.net.InetSocketAddress;

import com.tenio.core.configuration.define.ConnectionType;
import com.tenio.core.entity.AbstractPlayer;
import com.tenio.core.entity.element.MessageObject;
import com.tenio.core.event.IEventManager;

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
public abstract class Connection {

	public static final String KEY_STR_CONNECTION = "c";

	protected final IEventManager _eventManager;

	/**
	 * @see AbstractPlayer#getName()
	 */
	private String __username;
	/**
	 * Save the client's address
	 */
	private String __address;
	/**
	 * Type of the connection
	 */
	private ConnectionType __type;
	/**
	 * The order of connection in one player
	 */
	private int __index;

	public Connection(IEventManager eventManager, ConnectionType type, int index) {
		_eventManager = eventManager;
		__type = type;
		__index = index;
	}

	/**
	 * Retrieve the "connection" type, see {@link ConnectionType}
	 * 
	 * @return the type of connection
	 */
	public ConnectionType getType() {
		return __type;
	}

	/**
	 * Determine if the current type of connection is matched or not?
	 * 
	 * @param type the comparison type
	 * @return <b>true</b> is the current type is matched, <b>false</b> otherwise.
	 */
	public boolean isType(ConnectionType type) {
		return (__type == type);
	}

	/**
	 * @return the current index of connection in one player
	 */
	public int getIndex() {
		return __index;
	}

	/**
	 * Retrieve the "connection" id, this id is player's name, see
	 * {@link AbstractPlayer#getName()}
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return __username;
	}

	/**
	 * Set id for the "connection", this id is player's name, see
	 * {@link AbstractPlayer#getName()}
	 * 
	 * @param username the identify of this connection
	 */
	public void setUsername(String username) {
		__username = username;
	}

	/**
	 * Remove the username value from channel cache
	 */
	public void removeUsername() {
		__username = null;
	}

	/**
	 * Set the address
	 * 
	 * @param address the new address value
	 */
	public void setAddress(String address) {
		__address = address;
	}

	/**
	 * Retrieve the "connection" address in string type
	 * 
	 * @return the address
	 */
	public String getAddress() {
		return __address;
	}
	
	@Override
	public String toString() {
		return getAddress();
	}

	/**
	 * Send a message to the client
	 * 
	 * @param message the message content, see {@link MessageObject}
	 */
	public abstract void send(MessageObject message);

	/**
	 * Close a "connection" between a client with the server
	 */
	public abstract void close();

	/**
	 * Delete the keys which are used to identify a player in one "connection". This
	 * method need to be implemented when the "connection" type is Socket or
	 * WebSocket
	 */
	public abstract void clean();

	/**
	 * Set the current address for your "connection" (only need for Socket type)
	 * 
	 * @param remote, see {@link InetSocketAddress}
	 */
	public abstract void setRemote(InetSocketAddress remote);

	/**
	 * Retrieve this connection itself by channel
	 * 
	 * @return the current connection
	 */
	public abstract Connection getThis();

	/**
	 * Set this connection into current channel Note: Set value for one key. You can
	 * set your custom data to one connection to quick access. These keys and values
	 * should be saved to channel (which is defined by NIO mechanism)
	 */
	public abstract void setThis();

	/**
	 * Remove the connection object from channel cache
	 */
	public abstract void removeThis();

}
