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
package com.tenio.core.entity;

import java.util.LinkedList;
import java.util.List;

import com.tenio.core.entity.annotation.Column;
import com.tenio.core.entity.annotation.Entity;
import com.tenio.core.network.IConnection;

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
@Entity
public abstract class AbstractPlayer implements IPlayer {

	/**
	 * The list of socket/web socket connections
	 */
	private IConnection[] __connections;
	/**
	 * Tracing the room which they player has been in
	 */
	private final LinkedList<String> __tracedPassedRoom;
	/**
	 * The unique name in the server
	 */
	@Column(name = "name")
	private final String __name;
	/**
	 * This value for make a link between a player with his corresponding entity in
	 * one game
	 */
	@Column(name = "entity_id")
	private String __entityId;
	/**
	 * A reference to its contained room. This value may be set <b>null</b> @see
	 * {@link IRoom}
	 */
	@Column(name = "room")
	private volatile IRoom __room;
	/**
	 * The current system time when a new message from the client comes
	 */
	private volatile long __readerTime;
	/**
	 * The current system time when a new message is sent to the client from your
	 * server
	 */
	private volatile long __writerTime;
	/**
	 * This flag (enabled state) allows the player not affected by the system
	 * timeouts rule. The default value is <b>false</b>
	 */
	@Column(name = "ignore_timeout")
	private volatile boolean __flagIgnoreTimeout;

	/**
	 * This flag for quick checking if a player is a NPC or not
	 */
	@Column(name = "flag_npc")
	private volatile boolean __flagNPC;

	/**
	 * Create a new player
	 * 
	 * @param name the unique name
	 */
	public AbstractPlayer(final String name) {
		__name = name;
		__flagNPC = true;
		__tracedPassedRoom = new LinkedList<String>();
		setCurrentReaderTime();
		setCurrentWriterTime();
	}

	@Override
	public String getEntityId() {
		return __entityId;
	}

	@Override
	public void setEntityId(String entityId) {
		__entityId = entityId;
	}

	@Override
	public String getName() {
		return __name;
	}

	@Override
	public boolean isNPC() {
		return __flagNPC;
	}

	@Override
	public boolean hasConnection(int index) {
		if (__flagNPC) {
			return false;
		}
		synchronized (__connections) {
			return (__connections[index] != null);
		}
	}

	@Override
	public IConnection getConnection(int index) {
		if (__flagNPC) {
			return null;
		}
		synchronized (__connections) {
			return __connections[index];
		}
	}

	@Override
	public synchronized void initializeConnections(int size) {
		__connections = new IConnection[size];
		__flagNPC = false;
	}

	@Override
	public void setConnection(final IConnection connection, int index) {
		if (__flagNPC) {
			return;
		}
		synchronized (connection) {
			__connections[index] = connection;
		}
	}

	@Override
	public void closeConnection(int index) {
		if (__flagNPC) {
			return;
		}
		synchronized (__connections) {
			__closeConnection(index);
		}
	}

	@Override
	public void closeAllConnections() {
		if (__flagNPC) {
			return;
		}
		synchronized (__connections) {
			for (int i = 0; i < __connections.length; i++) {
				__closeConnection(i);
			}
		}
	}

	/**
	 * Non thread-safe method
	 * 
	 * @param index the connection's index
	 */
	private void __closeConnection(int index) {
		if (index < 0 || index > __connections.length) {
			return;
		}
		if (__connections[index] != null) {
			__connections[index].close();
			__connections[index] = null;
		}
	}

	@Override
	public IRoom getCurrentRoom() {
		return __room;
	}

	@Override
	public void setCurrentRoom(final IRoom room) {
		__room = room;
		if (__room != null) {
			synchronized (__tracedPassedRoom) {
				__tracedPassedRoom.addLast(room.getId());
			}
		}
	}

	@Override
	public List<String> getTracedRoomsList() {
		synchronized (__tracedPassedRoom) {
			return __tracedPassedRoom;
		}
	}

	@Override
	public long getReaderTime() {
		return __readerTime;
	}

	@Override
	public void setCurrentReaderTime() {
		__readerTime = System.currentTimeMillis();
	}

	@Override
	public long getWriterTime() {
		return __writerTime;
	}

	@Override
	public void setCurrentWriterTime() {
		__writerTime = System.currentTimeMillis();
	}

	@Override
	public boolean isIgnoreTimeout() {
		return __flagIgnoreTimeout;
	}

	@Override
	public void setIgnoreTimeout(final boolean flagIgnoreTimeout) {
		__flagIgnoreTimeout = flagIgnoreTimeout;
		setCurrentReaderTime();
		setCurrentWriterTime();
	}

}
