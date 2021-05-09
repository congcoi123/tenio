package com.tenio.core.exception;

public final class RequestQueueFullException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6981972099759381035L;

	public RequestQueueFullException(String message) {
		super(message);
	}

}
