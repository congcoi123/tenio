package com.tenio.engine.entitas.index;

/**
 * @author kong
 * 
 * Nov 4, 2019
 */
@FunctionalInterface
public interface FuncKey<TEntity, IComponent, TKey> {

	TKey getKey(TEntity entity, IComponent component);

}
