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
package com.tenio.core.configuration.define;

import java.util.HashMap;
import java.util.Map;

import com.tenio.common.configuration.ConfigurationType;

/**
 * This server needs some basic configuration to start running. The
 * configuration file can be defined as an XML file. See an example in
 * TenIOConfig.example.xml. You can also extend this file to create your own
 * configuration values.
 * 
 * <h1>Configuration for game server, declared in properties file</h1> <br>
 * 
 * @author kong
 * 
 */
public enum CoreConfigurationType implements ConfigurationType {
	
	/**
	 * The server name
	 */
	SERVER_NAME("name"),
	/**
	 * The server id (module name)
	 */
	SERVER_ID("id"),
	/**
	 * This current version code of your server in integer type (can be compared)
	 */
	SERVER_VERSION_CODE("versionCode"),
	/**
	 * This current version name of your server in string type
	 */
	SERVER_VERSION_NAME("versionName"),

	/**
	 * @see NioEventLoopGroup
	 */
	PRODUCER_THREADS_POOL_SIZE("producerThreadsPoolSize"),
	/**
	 * @see NioEventLoopGroup
	 */
	CONSUMER_THREADS_POOL_SIZE("consumerThreadsPoolSize"),
	/**
	 * When the server get disconnection of one client, can be hold its player
	 * instance until timeout
	 */
	KEEP_PLAYER_ON_DISCONNECT("keepPlayerOnDisconnect"),
	/**
	 * The maximum number of players which game can handle
	 */
	MAX_NUMBER_PLAYERS("maxNumberPlayers"),
	/**
	 * The max IDLE time in seconds which server can wait from the last getting
	 * message from client
	 */
	IDLE_READER_TIME("idleReaderTime"),
	/**
	 * The max IDLE time in seconds which server can wait from the last sending
	 * message to client
	 */
	IDLE_WRITER_TIME("idleWriterTime"),
	/**
	 * Get the period checking in seconds which server can keep the empty room
	 */
	EMPTY_ROOM_SCAN_INTERVAL("emptyRoomScanInterval"),
	/**
	 * The period checking player time out in seconds
	 */
	TIMEOUT_SCAN_INTERVAL("timeoutScanInterval"),
	/**
	 * The period checking CCU in seconds
	 */
	CCU_SCAN_INTERVAL("ccuScanInterval"),
	/**
	 * Schedule detecting deadlocked threads
	 */
	DEADLOCK_SCAN_INTERVAL("deadlockScanInterval"),
	/**
	 * The period monitoring system
	 */
	SYSTEM_MONITORING_INTERVAL("systemMonitoringInterval"),
	/**
	 * The delay between two computations of performances for channels or 0 if no
	 * statics are to be computed
	 */
	TRAFFIC_COUNTER_CHECK_INTERVAL("trafficCounterCheckInterval"),

	//====== UNDER CONTRUCTION ======//
	NUMBER_ACCEPTOR_WORKER("numberAcceptorWorker"),
	NUMBER_READER_WORKER("numberReaderWorker"),
	NUMBER_WRITER_WORKER("numberWriterWorker"),
	READ_MAX_BUFFER_SIZE("readMaxBufferSize"),
	WRITE_MAX_BUFFER_SIZE("writeMaxBufferSize"),
	CHANNEL_PACKET_QUEUE_SIZE("channelPacketQueueSize"),
	SERVER_ADDRESS("serverAddress"),
	//====== UNDER CONTRUCTION ======//
	
	/**
	 * The list of socket ports in configuration
	 */
	SOCKET_PORTS("socketPorts"),
	/**
	 * The list of web socket ports in configuration
	 */
	WEBSOCKET_PORTS("webSocketPorts"),
	/**
	 * The list of HTTP ports in configuration
	 */
	HTTP_PORTS("httpPorts");

	// Reverse-lookup map for getting a type from a value
	private static final Map<String, CoreConfigurationType> lookup = new HashMap<String, CoreConfigurationType>();

	static {
		for (var configurationType : CoreConfigurationType.values()) {
			lookup.put(configurationType.getValue(), configurationType);
		}
	}

	private final String value;

	private CoreConfigurationType(final String value) {
		this.value = value;
	}

	public final String getValue() {
		return this.value;
	}

	@Override
	public final String toString() {
		return this.name();
	}

	public static CoreConfigurationType getByValue(String value) {
		return lookup.get(value);
	}

}
