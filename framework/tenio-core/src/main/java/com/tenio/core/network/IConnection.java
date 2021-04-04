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

import com.tenio.common.element.CommonObject;
import com.tenio.core.configuration.define.TransportType;
import com.tenio.core.entity.IPlayer;
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
public interface IConnection {

	/**
	 * Retrieve the "connection" type, see {@link TransportType}
	 * 
	 * @return the type of connection
	 */
	TransportType getType();

	/**
	 * Determine if the current type of connection is matched or not?
	 * 
	 * @param type the comparison type
	 * @return <b>true</b> is the current type is matched, <b>false</b> otherwise.
	 */
	boolean isType(final TransportType type);

	/**
	 * @return the current index of connection in one player
	 */
	int getIndex();

	/**
	 * Retrieve the "connection" id, this id is player's name, see
	 * {@link IPlayer#getName()}
	 * 
	 * @return the player's name in {@link String}
	 */
	String getPlayerName();

	/**
	 * Set id for the "connection", this id is player's name, see
	 * {@link IPlayer#getName()}
	 * 
	 * @param playerName the identify of this connection
	 */
	void setPlayerName(final String playerName);

	/**
	 * Remove the player's name value from the channel cache
	 */
	void removePlayerName();

	/**
	 * Set the address
	 * 
	 * @param address the new address value
	 */
	void setAddress(final String address);

	/**
	 * Retrieve the "connection" address in string type
	 * 
	 * @return the address
	 */
	String getAddress();

	/**
	 * @return the event manager, see {@link IEventManager}
	 */
	IEventManager getEventManager();

	/**
	 * Send a message to the client
	 * 
	 * @param message the message content, see {@link CommonObject}
	 */
	void send(final CommonObject msgObject);

	/**
	 * Close a "connection" between a client with the server
	 */
	void close();

	/**
	 * Delete the keys which are used to identify a player in one "connection". This
	 * method need to be implemented when the "connection" type is Socket or
	 * WebSocket
	 */
	void clean();

	/**
	 * Set the current address for your "connection" (only need for Socket type)
	 * 
	 * @param remote, see {@link InetSocketAddress}
	 */
	void setRemote(final InetSocketAddress remoteAdress);

	/**
	 * Retrieve this connection itself by channel
	 * 
	 * @return the current connection
	 */
	IConnection getThis();

	/**
	 * Set this connection into current channel Note: Set value for one key. You can
	 * set your custom data to one connection to quick access. These keys and values
	 * should be saved to channel (which is defined by NIO mechanism)
	 */
	void setThis();

	/**
	 * Remove the connection object from channel cache
	 */
	void removeThis();

}
