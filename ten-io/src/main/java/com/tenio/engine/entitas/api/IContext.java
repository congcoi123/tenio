package com.tenio.engine.entitas.api;

import java.util.Stack;

import com.tenio.engine.entitas.api.entitas.IEntity;
import com.tenio.engine.entitas.api.entitas.IEntityIndex;
import com.tenio.engine.entitas.api.matcher.IMatcher;
import com.tenio.engine.entitas.collector.Collector;
import com.tenio.engine.entitas.group.GroupEvent;

/**
 * @author Rubentxu
 */
public interface IContext<TEntity extends IEntity> {

	TEntity createEntity();

	boolean hasEntity(TEntity entity);

	TEntity[] getEntities();

	IGroup<TEntity> getGroup(IMatcher<TEntity> matcher);

	int getTotalComponents();

	Stack<IComponent>[] getComponentPools();

	ContextInfo getContextInfo();

	int getEntitesCount();

	int getReusableEntitiesCount();

	int getRetainedEntitiesCount();

	void destroyAllEntities();

	void addEntityIndex(IEntityIndex entityIndex);

	IEntityIndex getEntityIndex(String name);

	void resetCreationIndex();

	void clearComponentPool(int index);

	void clearComponentPools();

	void reset();

	@SuppressWarnings("rawtypes")
	Collector createCollector(IMatcher matcher);

	@SuppressWarnings("rawtypes")
	Collector createCollector(IMatcher matcher, GroupEvent groupEvent);

}
