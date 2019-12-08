package com.tenio.engine.entitas.api.events;

import com.tenio.engine.entitas.api.entitas.IEntity;

/**
 * @author Rubentxu
 */
@FunctionalInterface
public interface EntityEvent<TEntity extends IEntity> {
	
	void released(final TEntity entity);
	
}
