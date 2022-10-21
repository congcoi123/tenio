/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

import com.tenio.engine.ecs.basis.implement.ContextInfo;
import com.tenio.engine.ecs.model.GameComponent;
import com.tenio.engine.ecs.model.GameContext;
import com.tenio.engine.ecs.model.system.TestSystem;
import com.tenio.engine.ecs.system.implement.Systems;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EcsSystemTest {

  private Systems systems;
  private TestSystem testSystem;

  @BeforeEach
  public void initialize() {
    systems = new Systems();

    var info = new ContextInfo("Game", GameComponent.getComponentNames(),
        GameComponent.getComponentTypes(), GameComponent.getNumberComponents());
    var context = new GameContext(info);

    testSystem = new TestSystem(context);

    systems.add(testSystem);
  }

  @AfterEach
  public void tearDown() {
    systems.clearSystems();
  }

  @Test
  public void allTestSytemMethodsShouldBeRun() {
    systems.initialize();
    systems.execute(1);
    systems.render(null);
    systems.tearDown();

    assertAll("runTestSystemMethods", () -> assertTrue(testSystem.isInitialized()),
        () -> assertTrue(testSystem.isExecuted()),
        () -> assertTrue(testSystem.isRendered()),
        () -> assertTrue(testSystem.isTearDown()));
  }

  @Test
  public void pauseSystemShouldReturnTrueValue() {
    systems.initialize();

    systems.paused(true);

    systems.execute(1);
    systems.render(null);
    systems.tearDown();

    assertAll("pauseSystem", () -> assertTrue(testSystem.isInitialized()),
        () -> assertFalse(testSystem.isExecuted()),
        () -> assertFalse(testSystem.isRendered()),
        () -> assertTrue(testSystem.isTearDown()));
  }
}
