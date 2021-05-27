package com.tenio.core.exceptions;

import com.tenio.core.entities.defines.results.PlayerJoinedRoomResult;

public final class PlayerJoinedRoomException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3173662815856707842L;

	private PlayerJoinedRoomResult __result;

	public PlayerJoinedRoomException(String message, PlayerJoinedRoomResult result) {
		super(message);
		__result = result;
	}

	public PlayerJoinedRoomResult getResult() {
		return __result;
	}

}
