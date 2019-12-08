package com.tenio.engine.entitas.exceptions;

/**
 * @author Rubentxu
 */
public class EntityIsNotDestroyedException extends EntitasException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8198832130853669653L;

	public EntityIsNotDestroyedException(String message) {
		super(message + "\nEntity is not destroyed yet!",
				"Did you manually call entity.Release(pool) yourself? " + "If so, please don't :)");
	}

}
