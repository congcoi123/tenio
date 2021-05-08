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
package com.tenio.core.entity;

import java.util.List;

import com.tenio.core.network.entity.session.Connection;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.SessionType;

/**
 * A player is one of the base elements in your server. It is a representation
 * of one client in the server and helps that client and this server
 * communicates with each other. You can handle the message that sent from the
 * client or send a message back via a player's instance. Most important here, a
 * player should not be a part of your logic game. A player is better to work as
 * an inspector with 'control' power. For example, when a player joins one game,
 * you need to create for him a corresponding entity. Now, the play will control
 * that entity in the same way you control one chess in a board game. Something
 * like HP status, the number of manas, etc, should not be an attribute of a
 * player, it is a part of an entity's attributes. So, for some interrupt
 * accidents, your character (entity) is still alive and waiting for player
 * re-connect to control it. Look like a soul (player) with a body (entity).
 * 
 * @author kong
 * 
 */
public interface Player {

	/**
	 * @return the entity who associated with the current player
	 */
	String getEntityId();

	/**
	 * A player should be a inspector with the control capacity, set the entity who
	 * associated with the current player.
	 * 
	 * @param entityId the entity' id
	 */
	void setEntityId(String entityId);

	/**
	 * @return the player's name
	 */
	String getName();

	/**
	 * Check the player's role
	 * 
	 * @return <b>true</b> if the player is an NPC (non player character), otherwise
	 *         return <b>false</b> (An NPC is a player has no connection).
	 */
	boolean isNPC();

	Connection getConnection(int connectionIndex);
	
	Session getSession(SessionType sessionType);

	void initializeConnections(int connectionsSize);

	void setConnection(Connection connection, int connectionIndex);

	void closeConnection(int connectionIndex);

	void closeAllConnections();

	/**
	 * We let the room instance escape from its scope, so the concerning process
	 * with this room need to be thread-safe.
	 * 
	 * @return the synchronized room instance
	 */
	Room getCurrentRoom();

	void setCurrentRoom(Room room);

	/**
	 * We let the list escape from its scope, so the concerning process with this
	 * room need to be thread-safe.
	 * 
	 * @return the list of rooms that the player has been in
	 */
	List<String> getTracedRoomIdsList();

	long getReaderTime();

	void setCurrentReaderTime();

	long getWriterTime();

	void setCurrentWriterTime();

	boolean isIgnoredTimeout();

	void setIgnoreTimeout(boolean flagIgnoreTimeout);

}
