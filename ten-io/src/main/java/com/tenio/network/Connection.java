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
package com.tenio.network;

import java.net.InetSocketAddress;

import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.element.TObject;

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

	/**
	 * Save the connection address
	 */
	protected InetSocketAddress _sockAddress;
	/**
	 * Type of the connection
	 */
	private Type __type;

	public enum Type {
		SOCKET, DATAGRAM, WEB_SOCKET
	}

	public Connection(Type type) {
		__type = type;
	}

	public Type getType() {
		return __type;
	}

	public boolean isType(Type type) {
		return (__type == type);
	}

	/**
	 * Send a message to the client
	 * 
	 * @param message the message content @see {@link TObject}
	 */
	public abstract void send(TObject message);

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
	 * @param sockAddress @see {@link InetSocketAddress}
	 */
	public abstract void setSockAddress(InetSocketAddress sockAddress);

	/**
	 * Retrieve the "connection" address in string type
	 * 
	 * @return Returns the address
	 */
	public abstract String getAddress();

	/**
	 * Retrieve the "connection" id, this id is player's name @see
	 * {@link AbstractPlayer#getName()}
	 * 
	 * @return Returns the id
	 */
	public abstract String getId();

	/**
	 * Set id for the "connection", this id is player's name @see
	 * {@link AbstractPlayer#getName()}
	 * 
	 * @param id the identify of this connection
	 */
	public abstract void setId(String id);

	/**
	 * Retrieve value by key
	 * 
	 * @param key the desired key
	 * @return Returns the corresponding object
	 */
	public abstract Object getAttr(String key);

	/**
	 * Set value for one key. You can set your custom data to one connection to
	 * quick access. These keys and values should be saved to channel (which is
	 * defined by NIO mechanism)
	 * 
	 * @param key   the desired key
	 * @param value the desired value
	 */
	public abstract void setAttr(String key, Object value);

}
