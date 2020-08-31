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
package com.tenio.engine.ecs.system;

import java.util.ArrayList;
import java.util.List;

import com.tenio.engine.physic2d.graphic.Paint;

/**
 * Systems provide a convenient way to group systems. You can add
 * {@link IInitializeSystem}, {@link IExecuteSystem}, {@link IRenderSystem},
 * {@link ITearDownSystem}, initialized and executed based on the order you
 * added them.
 * 
 * @author kong
 */
public final class Systems implements IInitializeSystem, IExecuteSystem, IRenderSystem, ITearDownSystem {

	private final List<IInitializeSystem> __initializeSystems;
	private final List<IExecuteSystem> __executeSystems;
	private final List<IRenderSystem> __renderSystems;
	private final List<ITearDownSystem> __tearDownSystems;

	/**
	 * Check if systems is running or not
	 */
	private boolean __flagRunning = true;

	/**
	 * Creates a new systems instance.
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
	 * @param system new adding system
	 * @return the current system instance
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
	 * Calls {@code initialize()} on all {@link IInitializeSystem} and other nested
	 * systems instances in the order you added them.
	 * 
	 * @see IInitializeSystem#initialize()
	 */
	public void initialize() {
		for (var system : __initializeSystems) {
			system.initialize();
		}
	}

	/**
	 * Calls {@code execute()} on all {@link IExecuteSystem} and other nested
	 * systems instances in the order you added them.
	 * 
	 * @see IExecuteSystem#execute(float)
	 * 
	 * @param deltaTime the delta time
	 */
	public void execute(float deltaTime) {
		if (__flagRunning) {
			for (var system : __executeSystems) {
				system.execute(deltaTime);
			}
		}
	}

	/**
	 * Calls {@code render()} on all {@link IRenderSystem} and other nested systems
	 * instances in the order you added them.
	 * 
	 * @see IRenderSystem#render(Paint)
	 * 
	 * @param paint the renderer object
	 */
	@Override
	public void render(Paint paint) {
		if (__flagRunning) {
			for (var system : __renderSystems) {
				system.render(paint);
			}
		}
	}

	/**
	 * Calls {@code tearDown()} on all {@link ITearDownSystem} and other nested
	 * Systems instances in the order you added them.
	 * 
	 * @see ITearDownSystem#tearDown()
	 */
	public void tearDown() {
		for (var system : __tearDownSystems) {
			system.tearDown();
		}
	}

	/**
	 * Remove all systems
	 */
	public void clearSystems() {
		__initializeSystems.clear();
		__executeSystems.clear();
		__renderSystems.clear();
		__tearDownSystems.clear();
	}

	/**
	 * Pause the systems running
	 * 
	 * @see #__executeSystems
	 * @see #__renderSystems
	 * 
	 * @param flagPause <b>true</b> for pausing, <b>false</b> otherwise
	 */
	public void paused(boolean flagPause) {
		__flagRunning = !flagPause;
	}

	/**
	 * Retrieves the systems status
	 * 
	 * @return <b>true</b> if systems are running, <b>false</b> otherwise
	 */
	public boolean isRunning() {
		return __flagRunning;
	}

}