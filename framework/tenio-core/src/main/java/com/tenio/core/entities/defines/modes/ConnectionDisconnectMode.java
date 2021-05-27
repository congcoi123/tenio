package com.tenio.core.entities.defines.modes;

public enum ConnectionDisconnectMode {

	/**
	 * When the player manually disconnect the connection
	 */
	DEFAULT,

	/**
	 * When the connection is lost and causing by the client side
	 */
	LOST,

	REACHED_MAX_CONNECTION,

	IDLE,

	KICK,

	BAN,

	UNKNOWN;

}
