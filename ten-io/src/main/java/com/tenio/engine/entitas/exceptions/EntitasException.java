package com.tenio.engine.entitas.exceptions;

/**
 * @author Rubentxu
 */
public class EntitasException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8325773031253316826L;

	public EntitasException(String message, String hint) {
        new Exception(hint != null ? (message + "\n" + hint) : message);
    }
	
}
