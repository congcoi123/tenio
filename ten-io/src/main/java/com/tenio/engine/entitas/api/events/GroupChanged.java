package com.tenio.engine.entitas.api.events;

import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.api.IGroup;
import com.tenio.engine.entitas.api.entitas.IEntity;

/**
 * @author Rubentxu
 */
@FunctionalInterface
public interface GroupChanged<TEntity extends IEntity> {
	
	void changed(final IGroup<TEntity> group, final TEntity entity, final int index, final IComponent component);
	
}
