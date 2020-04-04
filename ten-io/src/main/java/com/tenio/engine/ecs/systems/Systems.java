/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.engine.ecs.systems;

import java.util.ArrayList;
import java.util.List;

import com.tenio.engine.physic.graphic.Paint;

/**
 * Systems provide a convenient way to group systems. You can add
 * {@link IInitializeSystem}, {@link IExecuteSystem}, {@link IRenderSystem},
 * {@link ITearDownSystem}, initialized and executed based on the order you
 * added them.
 * 
 * @author Kong
 */
public final class Systems implements IInitializeSystem, IExecuteSystem, IRenderSystem, ITearDownSystem {

	private final List<IInitializeSystem> __initializeSystems;
	private final List<IExecuteSystem> __executeSystems;
	private final List<IRenderSystem> __renderSystems;
	private final List<ITearDownSystem> __tearDownSystems;

	private boolean __running = true;

	/**
	 * Creates a new Systems instance.
	 */
	public Systems() {
		__initializeSystems = new ArrayList<IInitializeSystem>();
		__executeSystems = new ArrayList<IExecuteSystem>();
		__renderSystems = new ArrayList<IRenderSystem>();
		__tearDownSystems = new ArrayList<ITearDownSystem>();
	}

	/**
	 * Adds the system instance to the systems list.
	 * 
	 * @param new adding system
	 * @return Returns the current instance
	 */
	public Systems add(ISystem system) {
		if (system != null) {
			if (system instanceof IInitializeSystem) {
				__initializeSystems.add((IInitializeSystem) system);
			}
			if (system instanceof IExecuteSystem) {
				__executeSystems.add((IExecuteSystem) system);
			}
			if (system instanceof IRenderSystem) {
				__renderSystems.add((IRenderSystem) system);
			}
			if (system instanceof ITearDownSystem) {
				__tearDownSystems.add((ITearDownSystem) system);
			}
		}
		return this;
	}

	/**
	 * Calls initialize() on all IInitializeSystem and other nested systems
	 * instances in the order you added them.
	 */
	public void initialize() {
		for (IInitializeSystem system : __initializeSystems) {
			system.initialize();
		}
	}

	/**
	 * Calls execute() on all IExecuteSystem and other nested systems instances in
	 * the order you added them.
	 * 
	 * @param deltaTime
	 */
	public void execute(float deltaTime) {
		if (__running) {
			for (IExecuteSystem system : __executeSystems) {
				system.execute(deltaTime);
			}
		}
	}

	/**
	 * Calls render() on all IRenderSystem and other nested systems instances in the
	 * order you added them.
	 */
	@Override
	public void render(Paint paint) {
		if (__running) {
			for (IRenderSystem system : __renderSystems) {
				system.render(paint);
			}
		}
	}

	/**
	 * Calls tearDown() on all ITearDownSystem and other nested Systems instances in
	 * the order you added them.
	 */
	public void tearDown() {
		for (ITearDownSystem system : __tearDownSystems) {
			system.tearDown();
		}
	}

	public void clearSystems() {
		__initializeSystems.clear();
		__executeSystems.clear();
		__renderSystems.clear();
		__tearDownSystems.clear();
	}

	public void paused(boolean flag) {
		__running = !flag;
	}

	public boolean isRunning() {
		return __running;
	}

}