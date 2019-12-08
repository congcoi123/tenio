package com.tenio.engine.entitas.exceptions;

/**
 * @author Rubentxu
 */
public class SingleEntityException extends EntitasException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -600641229360203231L;

	public SingleEntityException(int count) {
		super("Expected exactly one entity in collection but found " + count + "!",
				"Use collection.SingleEntity() only when you are sure that there " + "is exactly one entity.");
	}

}
