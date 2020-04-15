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
package com.tenio.entity;

import com.tenio.annotation.Column;
import com.tenio.annotation.Entity;
import com.tenio.network.Connection;

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
public abstract class AbstractPlayer {

	/**
	 * The main connection (TCP). It will be established first
	 */
	private Connection __connection;
	/**
	 * The UDP sub-connection. It can be set or set to <code>null</code>
	 */
	private Connection __subConnection;
	/**
	 * The unique name in the server
	 */
	@Column(name = "name")
	private String __name;
	/**
	 * This value for make a link between a player with his corresponding entity in
	 * one game
	 */
	@Column(name = "entity_id")
	private String __entityId;
	/**
	 * A reference to its contained room. This value may be set
	 * <code>null</code> @see {@link AbstractRoom}
	 */
	@Column(name = "room")
	private AbstractRoom __room;
	/**
	 * The current system time when a new message from the client comes
	 */
	private long __readerTime;
	/**
	 * The current system time when a new message is sent to the client from your
	 * server
	 */
	private long __writerTime;
	/**
	 * For simple hold some states. Default value is <code>0</code>
	 */
	@Column(name = "state")
	private int __state;
	/**
	 * This flag (enabled state) allows the player not affected by the system
	 * timeouts rule. The default value is <code>false</code>
	 */
	@Column(name = "ignore_timeout")
	private boolean __flagIgnoreTimeout;
	/**
	 * To quickly determine the player contains a connection or not
	 */
	private boolean __flagConnection;
	/**
	 * To quickly determine the player contains a sub-connection or not
	 */
	private boolean __flagSubConnection;

	/**
	 * Create a new player
	 * 
	 * @param name the unique name
	 */
	public AbstractPlayer(final String name) {
		__name = name;
		setCurrentReaderTime();
		setCurrentWriterTime();
	}

	public String getEntityId() {
		return __entityId;
	}

	public void setEntityId(String entityId) {
		__entityId = entityId;
	}

	public String getName() {
		return __name;
	}

	public boolean isState(final int state) {
		return (__state == state);
	}

	public int getState() {
		return __state;
	}

	public void setState(final int state) {
		__state = state;
	}

	/**
	 * Check the player's role
	 * 
	 * @return <b>true</b> if the player is a NPC (non player character), otherwise
	 *         return <b>false</b>. A NPC is a player without a connection
	 */
	public boolean isNPC() {
		return !__flagConnection;
	}

	public boolean hasConnection() {
		return __flagConnection;
	}

	public Connection getConnection() {
		return __connection;
	}

	public void setConnection(final Connection connection) {
		__connection = connection;
		__flagConnection = (__connection != null);
	}

	public boolean hasSubConnection() {
		return __flagSubConnection;
	}

	public Connection getSubConnection() {
		return __subConnection;
	}

	public void setSubConnection(final Connection subConnection) {
		if (__flagSubConnection && subConnection == null) {
			// need to clear key in UDP channel
			__subConnection.setAttr(__subConnection.getAddress(), null);
		}
		__subConnection = subConnection;
		__flagSubConnection = (__subConnection != null);
	}

	public AbstractRoom getRoom() {
		return __room;
	}

	public void setRoom(final AbstractRoom room) {
		__room = room;
	}

	public long getReaderTime() {
		return __readerTime;
	}

	public void setCurrentReaderTime() {
		__readerTime = System.currentTimeMillis();
	}

	public long getWriterTime() {
		return __writerTime;
	}

	public void setCurrentWriterTime() {
		__writerTime = System.currentTimeMillis();
	}

	public boolean isIgnoreTimeout() {
		return __flagIgnoreTimeout;
	}

	public void setIgnoreTimeout(final boolean flagIgnoreTimeout) {
		__flagIgnoreTimeout = flagIgnoreTimeout;
		setCurrentReaderTime();
		setCurrentWriterTime();
	}

}
