package com.tenio.engine.entitas.api.events;

import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.api.entitas.IEntity;

/**
 * @author Rubentxu
 */
@FunctionalInterface
public interface EntityComponentReplaced<TEntity extends IEntity> {
	
	void replaced(final TEntity entity, final int index, final IComponent previousComponent,
			final IComponent newComponent);
	
}
