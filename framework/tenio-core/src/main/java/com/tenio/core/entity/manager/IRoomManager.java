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
package com.tenio.core.entity.manager;

import java.util.Map;

import com.tenio.core.api.RoomApi;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.entity.IPlayer;
import com.tenio.core.entity.IRoom;
import com.tenio.core.exception.DuplicatedRoomIdException;
import com.tenio.core.exception.NullRoomException;

/**
 * Manage all your rooms ({@link IRoom}) on the server. It is a singleton
 * pattern class, which can be called anywhere. But it's better that you use the
 * {@link RoomApi} interface for easier management.
 * 
 * @author kong
 * 
 */
public interface IRoomManager extends IManager {

	/**
	 * @return the number of rooms in your server
	 */
	int count();

	/**
	 * We let all rooms escape from their scope, so the associating process need to
	 * be thread-safe.
	 * 
	 * @return all the current rooms in your server
	 */
	Map<String, IRoom> gets();

	/**
	 * Remove all rooms
	 */
	void clear();

	/**
	 * Retrieve a room by its id. We let a room escape from its scope, so the
	 * associating process need to be thread-safe.
	 * 
	 * @param roomId the unique id
	 * @return a room's instance if it has existed, <b>null</b> otherwise
	 */
	IRoom get(final String roomId);

	/**
	 * Determine if the room has existed or not.
	 * 
	 * @param roomId the unique ID
	 * @return <b>true</b> if the room has existed, <b>null</b> otherwise
	 */
	boolean contain(final String roomId);

	/**
	 * Add a new room to your server. You need create your own room first.
	 * 
	 * @param room that is added, see {@link IRoom}
	 * 
	 * @throws DuplicatedRoomIdException
	 */
	void add(final IRoom room) throws DuplicatedRoomIdException;

	/**
	 * Remove a room from your server.
	 * 
	 * @param room that is removed, see {@link IRoom}
	 * 
	 * @throws NullRoomException
	 */
	void remove(final IRoom room) throws NullRoomException;

	/**
	 * Request one player to join a room. This request can be refused with some
	 * reason. You can handle these results in the corresponding events.
	 * 
	 * @param room   the desired room, see {@link IRoom}
	 * @param player the current player, see {@link IPlayer}
	 * @return the action' result if it existed in, see {@link CoreMessageCode},
	 *         <b>null</b> otherwise
	 */
	CoreMessageCode makePlayerJoinRoom(final IRoom room, final IPlayer player);

	/**
	 * Allow a player to leave his current room. You can handle your own logic in
	 * the corresponding events.
	 * 
	 * @param player that will be left his current room, see {@link IPlayer}
	 * @param force  it's set <b>true</b> if you want to force the player leave.
	 *               Otherwise, it's set <b>false</b>
	 * @return the action' result if it existed in, see {@link CoreMessageCode},
	 *         <b>null</b> otherwise
	 */
	CoreMessageCode makePlayerLeaveRoom(final IPlayer player, final boolean force);

}
