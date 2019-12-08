package com.tenio.engine.entitas.exceptions;

import com.tenio.engine.entitas.Context;

/**
 * @author Rubentxu
 */
public class ContextEntityIndexDoesNotExistException extends EntitasException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5402168678857209345L;

	@SuppressWarnings("rawtypes")
	public ContextEntityIndexDoesNotExistException(Context pool, String name) {
		super("Cannot get EntityIndex '" + name + "' from pool '" + pool + "'!",
				"No EntityIndex with this name has been added.");
	}

}
