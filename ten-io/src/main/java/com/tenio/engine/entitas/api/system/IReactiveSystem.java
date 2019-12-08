package com.tenio.engine.entitas.api.system;

/**
 * <p>
 * Entitas also provides a special system called ReactiveSystem, which is using
 * <br>
 * a Group Observer under the hood. It holds changed entities of interest at
 * <br>
 * your fingertips. Imagine you have 100 fighting units on the battlefield but
 * <br>
 * only 10 of them changed their position. Instead of using a normal
 * <br>
 * IExecuteSystem and updating all 100 views depending on the position you can
 * <br>
 * use a IReactiveSystem which will only update the views of the 10 changed
 * <br>
 * units.
 * </p>
 * 
 * @author Rubentxu
 */
public interface IReactiveSystem extends IExecuteSystem {

	void activate();

	void deactivate();

	void clear();

}
