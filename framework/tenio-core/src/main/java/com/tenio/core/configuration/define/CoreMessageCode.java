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
package com.tenio.core.configuration.define;

/**
 * All base system messages for the server are defined here. This class should
 * not be modified.
 * 
 * @author kong
 * 
 */
public enum CoreMessageCode {

	/**
	 * When you try to add a player that has the same name with one appeared on the
	 * server.
	 */
	PLAYER_WAS_EXISTED("player_was_existed"),
	/**
	 * When you try to add an invalid player (wrong credentials or your own logic
	 * handle) into the server.
	 */
	PLAYER_INFO_IS_INVALID("player_info_is_invalid"),
	/**
	 * When you try to add a player that duplicates in one room.
	 */
	PLAYER_WAS_IN_ROOM("player_was_in_room"),
	/**
	 * When you try to remove a player from his room, but he has already left.
	 */
	PLAYER_ALREADY_LEFT_ROOM("player_already_left_room"),
	/**
	 * When you try to add a player in one room and that exceeds the room's
	 * capacity.
	 */
	ROOM_IS_FULL("room_is_full"),
	/**
	 * When you try to add a room that has the same id with one appeared on the
	 * server.
	 */
	ROOM_WAS_EXISTED("room_was_existed"),
	/**
	 * When you try to create a new connection and that exceeds the number of
	 * allowed connections.
	 */
	REACHED_MAX_CONNECTION("reached_max_connection"),
	/**
	 * When you try to retrieve a non-existing player.
	 */
	PLAYER_NOT_FOUND("player_not_found"),
	/**
	 * When you try to attach a UDP connection to one player with a non-existing of
	 * TCP connection.
	 */
	MAIN_CONNECTION_NOT_FOUND("main_connection_not_found");
	
	private final String value;
	
	private CoreMessageCode(String value) {
		this.value = value;
	}
	
	public final String getValue() {
		return this.value;
	}
	
	@Override
	public final String toString() {
		return this.name();
	}
	
}
