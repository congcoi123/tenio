package com.tenio.engine.entitas.api.matcher;

import com.tenio.engine.entitas.api.entitas.IEntity;

/**
 * @author Rubentxu
 */
public interface IMatcher<TEntity extends IEntity> {

	int[] getIndices();

	boolean matches(TEntity entity);

}
