package com.tenio.engine.entitas.exceptions;

import com.tenio.engine.entitas.Context;

/**
 * @author Rubentxu
 */
public class ContextEntityIndexDoesAlreadyExistException extends EntitasException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -322224614683161509L;

	@SuppressWarnings("rawtypes")
	public ContextEntityIndexDoesAlreadyExistException(Context pool, String name) {
		super("Cannot add EntityIndex '" + name + "' to pool '" + pool + "'!",
				"An EntityIndex with this name has already been added.");
	}

}
