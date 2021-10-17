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

package com.tenio.examples.example5.ecs;

import com.tenio.engine.ecs.basis.implement.ContextInfo;
import com.tenio.engine.heartbeat.ecs.EcsHeartBeat;
import com.tenio.examples.example5.context.GameComponent;
import com.tenio.examples.example5.context.GameContext;
import com.tenio.examples.example5.system.SystemInitialization;
import com.tenio.examples.example5.system.SystemMovement;
import com.tenio.examples.example5.system.SystemRenderer;
import com.tenio.examples.example5.system.SystemTeardown;
import java.awt.Color;

public final class ECS extends EcsHeartBeat {

  private final GameContext gameContext;
  private boolean toggleMotion = true;
  private boolean toggleAnimation = true;
  private boolean toggleView = true;

  public ECS(int cx, int cy) {
    super(cx, cy);

    ContextInfo info = new ContextInfo("Game", GameComponent.getComponentNames(),
        GameComponent.getComponentTypes(), GameComponent.getNumberComponents());
    gameContext = new GameContext(info);

    addSystem(new SystemInitialization(gameContext));
    addSystem(new SystemMovement(gameContext));
    addSystem(new SystemRenderer(gameContext));
    addSystem(new SystemTeardown(gameContext));
  }

  @Override
  protected void onAction1() {
    toggleMotion = !toggleMotion;
    setTextAction1(toggleMotion ? "Stop" : "Move", Color.LIGHT_GRAY);
    for (var entity : gameContext.getEntities().values()) {
      entity.setMotion(toggleMotion);
    }
  }

  @Override
  protected void onAction2() {
    toggleAnimation = !toggleAnimation;
    setTextAction2(toggleAnimation ? "Off animation" : "On animation", Color.LIGHT_GRAY);
    for (var entity : gameContext.getEntities().values()) {
      entity.setAnimation(toggleAnimation);
    }
  }

  @Override
  protected void onAction3() {
    toggleView = !toggleView;
    setTextAction3(toggleView ? "Invisible" : "Visible", Color.LIGHT_GRAY);
    for (var entity : gameContext.getEntities().values()) {
      entity.setView(toggleView);
    }
  }
}
