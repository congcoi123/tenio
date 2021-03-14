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
	VERSION_CODE("versionCode"),
	/**
	 * This current version name of your server in string type
	 */
	VERSION_NAME("versionName"),

	/**
	 * When the server get disconnection of one client, can be hold its player
	 * instance until timeout
	 */
	KEEP_PLAYER_ON_DISCONNECT("keepPlayerOnDisconnect"),
	/**
	 * The maximum number of players which game can handle
	 */
	MAX_PLAYER("maxPlayer"),
	/**
	 * The max IDLE time in seconds which server can wait from the last getting
	 * message from client
	 */
	IDLE_READER("idleReader"),
	/**
	 * The max IDLE time in seconds which server can wait from the last sending
	 * message to client
	 */
	IDLE_WRITER("idleWriter"),
	/**
	 * Get the period checking in seconds which server can keep the empty room
	 */
	EMPTY_ROOM_SCAN("emptyRoomScan"),
	/**
	 * The period checking player time out in seconds
	 */
	TIMEOUT_SCAN("timeoutScan"),
	/**
	 * The period checking CCU in seconds
	 */
	CCU_SCAN("ccuScan"),
	/**
	 * The list of socket ports in configuration
	 */
	SOCKET_PORTS("socketPorts"),
	/**
	 * The list of web socket ports in configuration
	 */
	WEBSOCKET_PORTS("webSocketPorts"),
	/**
	 * The list of http ports in configuration
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
