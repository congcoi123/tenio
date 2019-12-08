package com.tenio.engine.entitas.api.system;

/**
 * <p>
 * Initialize systems run once at the start of your program
 * </p>
 * 
 * @author Rubentxu
 */
public interface IInitializeSystem extends ISystem {

	void initialize();

}
