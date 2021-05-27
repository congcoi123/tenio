package com.tenio.core.exceptions;

import com.tenio.core.entities.defines.results.SwitchedPlayerSpectatorResult;

public final class SwitchedPlayerSpectatorException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8858056991799548907L;

	private SwitchedPlayerSpectatorResult __result;

	public SwitchedPlayerSpectatorException(String message, SwitchedPlayerSpectatorResult result) {
		super(message);
		__result = result;
	}

	public SwitchedPlayerSpectatorResult getResult() {
		return __result;
	}

}
