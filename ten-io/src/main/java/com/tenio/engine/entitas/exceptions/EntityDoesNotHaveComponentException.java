package com.tenio.engine.entitas.exceptions;

/**
 * @author Rubentxu
 */
public class EntityDoesNotHaveComponentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -781764883768737089L;

	public EntityDoesNotHaveComponentException(String message, int index) {
		super(message + "\nEntity does not have a component at index " + index);
	}

}
