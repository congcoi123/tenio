package com.tenio.core.exceptions;

public final class RequestQueueFullException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6981972099759381035L;

	public RequestQueueFullException(int currentSize) {
		super(String.format("Reached max queue size, the request was dropped. The current size: %d", currentSize));
	}

}
