package com.tenio.engine.entitas.api.matcher;

import com.tenio.engine.entitas.api.entitas.IEntity;

/**
 * @author Rubentxu
 */
public interface IAllOfMatcher<TEntity extends IEntity> extends IAnyOfMatcher<TEntity> {

	IAnyOfMatcher<TEntity> anyOf(int... indices);

	IAnyOfMatcher<TEntity> anyOf(@SuppressWarnings("unchecked") IMatcher<TEntity>... matchers);

}
