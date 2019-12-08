package com.tenio.engine.entitas.api.events;

import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.api.entitas.IEntity;

/**
 * @author Rubentxu
 */
@FunctionalInterface
public interface EntityComponentChanged<TEntity extends IEntity> {
	
	void changed(final TEntity entity, final int index, final IComponent component);

}
