package com.tenio.engine.entitas.exceptions;

/**
 * @author Rubentxu
 */
public class ContextDoesNotContainEntityException extends EntitasException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7737599052604547668L;

	public ContextDoesNotContainEntityException(String message, String hint) {
        super(message + "\nSplashPool does not contain entity!", hint);
    }

}
