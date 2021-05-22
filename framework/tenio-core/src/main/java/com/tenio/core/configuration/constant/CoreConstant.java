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
package com.tenio.core.configuration.constant;

import io.netty.channel.ChannelOption;

/**
 * All base constants' values for the server are defined here. This class should
 * not be modified.
 * 
 * @author kong
 */
// FIXME: Fix me
public final class CoreConstant {

	private CoreConstant() {

	}

	/**
	 * In TCP, because of the stream transmission, it's necessary to know a data
	 * package's length for extracting the number of bytes of its content (divide
	 * stream data into smaller package data). Therefore, we need a data length
	 * value, which should be attached in the header of each package. All TCP
	 * connections which connect to our server must follow this rule.
	 */
	public static final int HEADER_BYTES = 2;

	/**
	 * A player can hold a list of connections, and it is the first connection
	 * order.
	 */
	public static final int MAIN_CONNECTION_INDEX = 0;

	/**
	 * A unique key for the CCU scan schedule.
	 */
	public static final String KEY_SCHEDULE_CCU_SCAN = "t.schedule.ccu.scan";

	/**
	 * A unique key for the system monitoring.
	 */
	public static final String KEY_SCHEDULE_SYSTEM_MONITORING = "t.schedule.system.monitoring";

	/**
	 * A unique key for the Empty Room scan schedule.
	 */
	public static final String KEY_SCHEDULE_EMPTY_ROOM_SCAN = "t.schedule.empty.room.scan";

	/**
	 * A unique key for the Time Out scan schedule.
	 */
	public static final String KEY_SCHEDULE_TIME_OUT_SCAN = "t.schedule.time.out.scan";

	/**
	 * A unique key for the HTTP manager schedule.
	 */
	public static final String KEY_SCHEDULE_HTTP_MANAGER = "t.schedule.http.manager";

	/**
	 * A unique key for the deadlocked thread detector schedule.
	 */
	public static final String KEY_SCHEDULE_DEADLOCK_SCAN = "t.schedule.deadlock.scan";

	/**
	 * 0 or a limit in bytes/s
	 */
	public static final long TRAFFIC_COUNTER_WRITE_LIMIT = 0L;

	/**
	 * 0 or a limit in bytes/s
	 */
	public static final long TRAFFIC_COUNTER_READ_LIMIT = 0L;

	/**
	 * The HTTP response with UTF-8 encoding
	 */
	public static final String UTF_8 = "UTF-8";

	/**
	 * The HTTP response with content type in JSON
	 */
	public static final String CONTENT_TYPE_JSON = "application/json";

	/**
	 * The HTTP response with content type in text
	 */
	public static final String CONTENT_TYPE_TEXT = "text/html";

	/**
	 * The default URI path when a HTTP server was started (To confirm if the server
	 * was started or not)
	 */
	public static final String PING_PATH = "/ping";

	/**
	 * Public Domain for broadcasting
	 */
	public static final String BROADCAST_ADDRESS = "255.255.255.255";

	/**
	 * The size of buffer in bytes {@link Byte}, references to
	 * {@link ChannelOption#SO_RCVBUF}
	 */
	public static final int DATAGRAM_RECEIVE_BUFFER = 768;

	/**
	 * The size of buffer in bytes {@link Byte}, references to
	 * {@link ChannelOption#SO_SNDBUF}
	 */
	public static final int DATAGRAM_SEND_BUFFER = 1024;

	/**
	 * The size of buffer in bytes {@link Byte}, references to
	 * {@link ChannelOption#SO_RCVBUF}
	 */
	public static final int SOCKET_RECEIVE_BUFFER = 10240;

	/**
	 * The size of buffer in bytes {@link Byte}, references to
	 * {@link ChannelOption#SO_SNDBUF}
	 */
	public static final int SOCKET_SEND_BUFFER = 10240;

	/**
	 * The size of buffer in bytes {@link Byte}, references to
	 * {@link ChannelOption#SO_RCVBUF}
	 */
	public static final int WEBSOCKET_RECEIVE_BUFFER = 10240;

	/**
	 * The size of buffer in bytes {@link Byte}, references to
	 * {@link ChannelOption#SO_SNDBUF}
	 */
	public static final int WEBSOCKET_SEND_BUFFER = 10240;

	/**
	 * The size of buffer in bytes {@link Byte}, references to
	 * {@link ChannelOption#SO_RCVBUF}
	 */
	public static final int BROADCAST_RECEIVE_BUFFER = 768;

	/**
	 * The size of buffer in bytes {@link Byte}, references to
	 * {@link ChannelOption#SO_SNDBUF}
	 */
	public static final int BROADCAST_SEND_BUFFER = 1024;

	public static final int WRITE_MESSAGE_QUEUE_SIZE_WARNING = 100;

	public static final String LOCAL_HOST = "localhost";

}
