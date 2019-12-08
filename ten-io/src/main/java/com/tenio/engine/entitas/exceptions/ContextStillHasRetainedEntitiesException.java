package com.tenio.engine.entitas.exceptions;

import com.tenio.engine.entitas.Context;

/**
 * @author Rubentxu
 */
public class ContextStillHasRetainedEntitiesException extends EntitasException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2794916214969885390L;

	@SuppressWarnings("rawtypes")
	public ContextStillHasRetainedEntitiesException(Context pool) {
		super("'" + pool + "' detected retained entities " + "although all entities got destroyed!",
				"Did you release all entities? Try calling pool.ClearGroups() "
						+ "and system.ClearReactiveSystems() before calling "
						+ "pool.DestroyAllEntities() to avoid memory leaks.");
	}

}
