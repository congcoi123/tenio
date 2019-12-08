package com.tenio.engine.entitas.collector;

import java.util.Set;

import com.tenio.engine.entitas.api.entitas.IEntity;

/**
 * @author Rubentxu
 */
public interface ICollector<TEntity extends IEntity> {

	int getCount();

	void activate();

	void deactivate();

	void clearCollectedEntities();

	Set<TEntity> getCollectedEntities();
	
}
