package com.tenio.engine.entitas.exceptions;

/**
 * @author Rubentxu
 */
public class EntityIsNotEnabledException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6772209109112632867L;

	public EntityIsNotEnabledException(String message) {
		super(message + "\nEntity is not enabled!");
	}

}
