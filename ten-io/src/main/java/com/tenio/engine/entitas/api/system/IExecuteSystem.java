package com.tenio.engine.entitas.api.system;

/**
 * <p>
 * Execute systems run once per frame. This is where you put code that needs to
 * run every frame.
 * </p>
 * 
 * @author Rubentxu
 */
public interface IExecuteSystem extends ISystem {

	void execute(float delta);

}
