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

package com.tenio.examples.example5.system;

import com.tenio.engine.ecs.basis.Context;
import com.tenio.engine.ecs.system.InitializeSystem;
import com.tenio.engine.ecs.system.implement.AbstractSystem;
import com.tenio.examples.example5.constant.Example5Constant;
import com.tenio.examples.example5.context.GameEntity;

public final class SystemInitialization extends AbstractSystem<GameEntity> implements
    InitializeSystem {

  public SystemInitialization(Context<GameEntity> context) {
    super(context);
  }

  @Override
  public void initialize() {
    var entity = (GameEntity) getContext().createEntity();
    entity.setAnimation(true);
    entity.setMotion(true);
    entity.setView(true);
    entity.setPosition(Example5Constant.DESIGN_WIDTH / 2, Example5Constant.DESIGN_HEIGHT / 2);
  }
}
