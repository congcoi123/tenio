package com.tenio.engine.entitas.api.matcher;

import com.tenio.engine.entitas.api.entitas.IEntity;

/**
 * @author Rubentxu
 */
public interface IAnyOfMatcher<TEntity extends IEntity> extends INoneOfMatcher<TEntity> {

	INoneOfMatcher<TEntity> noneOf(int... indices);

	INoneOfMatcher<TEntity> noneOf(@SuppressWarnings("unchecked") IMatcher<TEntity>... matchers);

}
