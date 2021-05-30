/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
package com.tenio.engine.ecs.systems.implement;

import java.util.ArrayList;
import java.util.List;

import com.tenio.engine.ecs.systems.ExecuteSystem;
import com.tenio.engine.ecs.systems.InitializeSystem;
import com.tenio.engine.ecs.systems.RenderSystem;
import com.tenio.engine.ecs.systems.System;
import com.tenio.engine.ecs.systems.TearDownSystem;
import com.tenio.engine.physic2d.graphic.Paint;

/**
 * Systems provide a convenient way to group systems. You can add
 * {@link InitializeSystem}, {@link ExecuteSystem}, {@link RenderSystem},
 * {@link TearDownSystem}, initialized and executed based on the order you added
 * them.
 */
public final class Systems implements InitializeSystem, ExecuteSystem, RenderSystem, TearDownSystem {

	private final List<InitializeSystem> __initializeSystems;
	private final List<ExecuteSystem> __executeSystems;
	private final List<RenderSystem> __renderSystems;
	private final List<TearDownSystem> __tearDownSystems;

	/**
	 * Check if systems is running or not
	 */
	private boolean __flagRunning = true;

	/**
	 * Creates a new systems instance.
	 */
	public Systems() {
		__initializeSystems = new ArrayList<InitializeSystem>();
		__executeSystems = new ArrayList<ExecuteSystem>();
		__renderSystems = new ArrayList<RenderSystem>();
		__tearDownSystems = new ArrayList<TearDownSystem>();
	}

	/**
	 * Adds the system instance to the systems list.
	 * 
	 * @param system new adding system
	 * @return the current system instance
	 */
	public Systems add(System system) {
		if (system != null) {
			if (system instanceof InitializeSystem) {
				__initializeSystems.add((InitializeSystem) system);
			}
			if (system instanceof ExecuteSystem) {
				__executeSystems.add((ExecuteSystem) system);
			}
			if (system instanceof RenderSystem) {
				__renderSystems.add((RenderSystem) system);
			}
			if (system instanceof TearDownSystem) {
				__tearDownSystems.add((TearDownSystem) system);
			}
		}
		return this;
	}

	/**
	 * Calls {@code initialize()} on all {@link InitializeSystem} and other nested
	 * systems instances in the order you added them.
	 * 
	 * @see InitializeSystem#initialize()
	 */
	public void initialize() {
		for (var system : __initializeSystems) {
			system.initialize();
		}
	}

	/**
	 * Calls {@code execute()} on all {@link ExecuteSystem} and other nested systems
	 * instances in the order you added them.
	 * 
	 * @see ExecuteSystem#execute(float)
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
	 * Calls {@code render()} on all {@link RenderSystem} and other nested systems
	 * instances in the order you added them.
	 * 
	 * @see RenderSystem#render(Paint)
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
	 * Calls {@code tearDown()} on all {@link TearDownSystem} and other nested
	 * Systems instances in the order you added them.
	 * 
	 * @see TearDownSystem#tearDown()
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