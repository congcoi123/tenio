package com.tenio.engine.entitas.exceptions;

import com.tenio.engine.entitas.api.entitas.IEntity;

/**
 * @author Rubentxu
 */
public class EntityIsAlreadyRetainedByOwnerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4088911788679274393L;

	public EntityIsAlreadyRetainedByOwnerException(IEntity entity, Object owner) {
		super("'" + owner + "' cannot release " + entity + "!\n" + "Entity is already retained by owner: " + owner
				+ "An entity can only be released from objects that retain it.");
	}

}
