package com.tenio.engine.entitas.exceptions;

/**
 * @author Rubentxu
 */
public class EntityAlreadyHasComponentException extends EntitasException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1161665901455353572L;

	public EntityAlreadyHasComponentException(int index, String message, String hint) {
		super(message + "\nEntity already has a component at index " + index + "!", hint);
	}

}
