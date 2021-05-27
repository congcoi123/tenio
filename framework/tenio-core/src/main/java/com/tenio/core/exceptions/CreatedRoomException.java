package com.tenio.core.exceptions;

import com.tenio.core.entities.defines.results.RoomCreatedResult;

public final class CreatedRoomException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1641350947646134751L;

	private RoomCreatedResult __result;

	public CreatedRoomException(String message, RoomCreatedResult result) {
		super(message);
		__result = result;
	}

	public RoomCreatedResult getResult() {
		return __result;
	}

}
