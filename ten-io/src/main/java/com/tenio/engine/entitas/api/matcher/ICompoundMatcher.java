package com.tenio.engine.entitas.api.matcher;

import com.tenio.engine.entitas.api.entitas.IEntity;

/**
 * @author Rubentxu
 */
public interface ICompoundMatcher<TEntity extends IEntity> extends IMatcher<TEntity> {

    int[] getAllOfIndices();

    int[] getAnyOfIndices();

    int[] getNoneOfIndices();

}
