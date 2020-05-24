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
package com.tenio.configuration.constant;

/**
 * All base errors' messages for the server are defined here. This class should
 * not be modified.
 * 
 * @author kong
 * 
 */
public final class ErrorMsg {

	private ErrorMsg() {
	}

	/**
	 * When you try to add a player that has the same name with one appeared on the
	 * server.
	 */
	public static final String PLAYER_IS_EXISTED = "player_is_existed";
	/**
	 * When you try to add an invalid player (wrong credentials or your own logic
	 * handle) into the server.
	 */
	public static final String PLAYER_IS_INVALID = "player_is_invalid";
	/**
	 * When you try to add a player that duplicates in one room.
	 */
	public static final String PLAYER_WAS_IN_ROOM = "player_was_in_room";
	/**
	 * When you try to remove a player from his room, but he has already leave.
	 */
	public static final String PLAYER_ALREADY_LEAVE_ROOM = "player_already_leave_room";
	/**
	 * When you try to add a player in one room and that exceeds the room's
	 * capacity.
	 */
	public static final String ROOM_IS_FULL = "room_is_full";
	/**
	 * When you try to add a room that has the same id with one appeared on the
	 * server.
	 */
	public static final String ROOM_IS_EXISTED = "room_is_existed";
	/**
	 * When you try to create a new connection and that exceeds the number of
	 * allowed connections.
	 */
	public static final String REACH_MAX_CONNECTION = "reach_max_connection";
	/**
	 * When you try to retrieve a non-existing player.
	 */
	public static final String PLAYER_NOT_FOUND = "player_not_found";
	/**
	 * When you try to attach a UDP connection to one player with a non-existing of
	 * TCP connection.
	 */
	public static final String MAIN_CONNECTION_NOT_FOUND = "main_connection_not_found";

	public static final String IO_EXCEPTION = "io_exception";

	public static final String INTERRUPTED_EXCEPTION = "interrupted_exception";

	public static final String START_NO_CONNECTION = "start_no_connection";

	public static final String START_NO_SUBCONNECTION_HANDLER = "start_no_connection_handler";

	public static final String START_NO_RECONNECTION_HANDLER = "start_no_reconnection_handler";

	public static final String START_NO_HTTP_HANDLER = "start_no_http_handler";
	
	public static final String DUPLICATED_URI_AND_METHOD_POST = "duplicated_uri_and_method_post";
	
	public static final String DUPLICATED_URI_AND_METHOD_PUT = "duplicated_uri_and_method_put";
	
	public static final String DUPLICATED_URI_AND_METHOD_GET = "duplicated_uri_and_method_get";
	
	public static final String DUPLICATED_URI_AND_METHOD_DELETE = "duplicated_uri_and_method_delete";

}
