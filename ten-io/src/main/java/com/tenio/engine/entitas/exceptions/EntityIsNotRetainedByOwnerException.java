package com.tenio.engine.entitas.exceptions;

import com.tenio.engine.entitas.api.entitas.IEntity;

/**
 * @author Rubentxu
 */
public class EntityIsNotRetainedByOwnerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3550533665102998329L;

	public EntityIsNotRetainedByOwnerException(IEntity entity, Object owner) {
		super("'" + owner + "' cannot retain " + entity + "!\n" + "Entity is already retained by this object!"
				+ "The entity must be released by this object first.");
	}

}
