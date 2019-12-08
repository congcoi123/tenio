package com.tenio.engine.entitas.api;

import java.util.Set;

import com.tenio.engine.entitas.api.entitas.IEntity;
import com.tenio.engine.entitas.api.events.GroupChanged;
import com.tenio.engine.entitas.api.matcher.IMatcher;

/**
 * @author Rubentxu
 */
public interface IGroup<TEntity extends IEntity> {

	int getCountEntities();

	void removeAllEventHandlers();

	IMatcher<TEntity> getMatcher();

	void handleEntitySilently(TEntity entity);

	void handleEntity(TEntity entity, int index, IComponent component);

	@SuppressWarnings("rawtypes")
	Set<GroupChanged> handleEntity(TEntity entity);

	void updateEntity(TEntity entity, int index, IComponent previousComponent, IComponent newComponent);

	boolean containsEntity(TEntity entity);

	TEntity[] getEntities();

	TEntity getSingleEntity();

}
