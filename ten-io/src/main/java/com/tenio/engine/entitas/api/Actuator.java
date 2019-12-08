package com.tenio.engine.entitas.api;

/**
 * @author Rubentxu
 */
@FunctionalInterface
public interface Actuator<C extends IComponent> {
	
	void modify(C component);
	
}
