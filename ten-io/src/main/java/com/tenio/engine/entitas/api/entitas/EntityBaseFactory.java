package com.tenio.engine.entitas.api.entitas;

/**
 * @author Rubentxu
 */
@FunctionalInterface
public interface EntityBaseFactory<E extends IEntity> {
	
	E create();
	
}
