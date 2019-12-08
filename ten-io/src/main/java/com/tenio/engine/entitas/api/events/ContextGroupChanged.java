package com.tenio.engine.entitas.api.events;

import com.tenio.engine.entitas.api.IContext;
import com.tenio.engine.entitas.api.IGroup;

/**
 * @author Rubentxu
 */
@FunctionalInterface
public interface ContextGroupChanged {
	
	void changed(@SuppressWarnings("rawtypes") final IContext context, @SuppressWarnings("rawtypes") final IGroup group);
	
}
