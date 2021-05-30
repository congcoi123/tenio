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
package com.tenio.engine.ecs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.engine.ecs.bases.implement.ContextInfo;
import com.tenio.engine.ecs.model.GameComponents;
import com.tenio.engine.ecs.model.GameContext;
import com.tenio.engine.ecs.model.system.TestSystem;
import com.tenio.engine.ecs.systems.implement.Systems;

/**
 * @author kong
 */
public final class EcsSystemTest {

	private Systems __systems;
	private TestSystem __testSystem;

	@BeforeEach
	public void initialize() {
		__systems = new Systems();

		ContextInfo info = new ContextInfo("Game", GameComponents.getComponentNames(),
				GameComponents.getComponentTypes(), GameComponents.getNumberComponents());
		GameContext context = new GameContext(info);

		__testSystem = new TestSystem(context);

		__systems.add(__testSystem);
	}

	@AfterEach
	public void tearDown() {
		__systems.clearSystems();
	}

	@Test
	public void allTestSytemMethodsShouldBeRun() {
		__systems.initialize();
		__systems.execute(1);
		__systems.render(null);
		__systems.tearDown();

		assertAll("runTestSystemMethods", () -> assertTrue(__testSystem.isInitialized()),
				() -> assertTrue(__testSystem.isExecuted()),
				() -> assertTrue(__testSystem.isRendered()),
				() -> assertTrue(__testSystem.isTearDown()));
	}

	@Test
	public void pauseSystemShouldReturnTrueValue() {
		__systems.initialize();

		__systems.paused(true);

		__systems.execute(1);
		__systems.render(null);
		__systems.tearDown();

		assertAll("pauseSystem", () -> assertTrue(__testSystem.isInitialized()),
				() -> assertFalse(__testSystem.isExecuted()),
				() -> assertFalse(__testSystem.isRendered()),
				() -> assertTrue(__testSystem.isTearDown()));

	}

}
