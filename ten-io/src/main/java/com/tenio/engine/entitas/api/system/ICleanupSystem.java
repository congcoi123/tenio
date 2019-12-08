package com.tenio.engine.entitas.api.system;

/**
 * <p>
 * Cleanup systems run at the end of each frame, after all other systems have
 * <br>
 * completed their work. These are useful if you want to create entities that
 * <br>
 * only exist for one frame
 * </p>
 * 
 * @author Rubentxu
 */
public interface ICleanupSystem extends ISystem {

	public void cleanup();

}
