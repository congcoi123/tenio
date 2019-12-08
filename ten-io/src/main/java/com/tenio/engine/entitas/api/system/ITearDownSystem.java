package com.tenio.engine.entitas.api.system;

/**
 * <p>
 * Teardown systems run once at the end of your program. This is where you can
 * <br>
 * clean up all resources acquired throughout the lifetime of your game.
 * </p>
 * 
 * @author Rubentxu
 */
public interface ITearDownSystem extends ISystem {

	public void tearDown();

}
