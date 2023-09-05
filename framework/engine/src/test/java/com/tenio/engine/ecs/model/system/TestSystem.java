/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.engine.ecs.model.system;

import com.tenio.engine.ecs.basis.Context;
import com.tenio.engine.ecs.model.GameEntity;
import com.tenio.engine.ecs.system.ExecuteSystem;
import com.tenio.engine.ecs.system.InitializeSystem;
import com.tenio.engine.ecs.system.RenderSystem;
import com.tenio.engine.ecs.system.TearDownSystem;
import com.tenio.engine.ecs.system.implement.AbstractSystem;
import com.tenio.engine.physic2d.graphic.Paint;

public final class TestSystem extends AbstractSystem<GameEntity>
    implements InitializeSystem, ExecuteSystem, RenderSystem, TearDownSystem {

  private boolean flagInitialize;
  private boolean flagExecute;
  private boolean flagRender;
  private boolean flagTearDown;

  public TestSystem(Context<GameEntity> context) {
    super(context);

    flagInitialize = false;
    flagExecute = false;
    flagRender = false;
    flagTearDown = false;
  }

  @Override
  public void initialize() {
    flagInitialize = true;
  }

  @Override
  public void execute(float deltaTime) {
    flagExecute = true;
  }

  @Override
  public void render(Paint paint) {
    flagRender = true;
  }

  @Override
  public void tearDown() {
    flagTearDown = true;
  }

  public boolean isInitialized() {
    return flagInitialize;
  }

  public boolean isExecuted() {
    return flagExecute;
  }

  public boolean isRendered() {
    return flagRender;
  }

  public boolean isTearDown() {
    return flagTearDown;
  }
}
