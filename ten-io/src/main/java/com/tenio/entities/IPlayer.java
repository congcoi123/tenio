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
package com.tenio.entities;

import java.util.List;

import com.tenio.entities.element.TObject;
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
public interface IPlayer extends IBackup<IPlayer> {

	int getId();
	
	String getName();

	String getDisplayname();

	void setDisplayname(String displayName);

	boolean isState(int state);

	int getState();

	void setState(int state);

	int getEntityId();

	void setEntityId(int entityId);

	boolean hasConnection();

	Connection getConnection();

	void setConnection(final Connection connection);

	boolean hasSubConnection();

	Connection getSubConnection();

	void setSubConnection(final Connection subConnection);

	IRoom getLastJoinedRoom();

	List<IRoom> getJoinedRooms();

	void addJoinedRoom(final IRoom room);

	void removeJoinedRoom(final IRoom room);

	boolean isJoinedInRoom(final IRoom room);

	void addCreatedRoom(final IRoom room);

	void removeCreatedRoom(final IRoom room);

	List<IRoom> getCreatedRooms();

	long getLastLoginTime();

	void setLastLoginTime();
	
	long getLastLogoutTime();
	
	void setLastLogoutTime();

	long getReaderTime();

	void setCurrentReaderTime();

	long getWriterTime();

	void setCurrentWriterTime();

	boolean isIgnoreTimeout();

	void setIgnoreTimeout(boolean isIgnoreTimeout);

	boolean isSpectator();
	
	void setSpectator(boolean isSpectator);

	Object getVariable(String key);

	TObject getVariables();

	void setVariable(String key, Object value);

	void setVariables(TObject variables);

}
