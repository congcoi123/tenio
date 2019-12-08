package com.tenio.engine.entitas.api.events;

import com.tenio.engine.entitas.api.IContext;
import com.tenio.engine.entitas.api.entitas.IEntity;

/**
 * @author Rubentxu
 */
@FunctionalInterface
public interface ContextEntityChanged {
	
	void changed(@SuppressWarnings("rawtypes") final IContext context, final IEntity entity);
	
}
