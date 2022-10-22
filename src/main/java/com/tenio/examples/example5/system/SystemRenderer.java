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

package com.tenio.examples.example5.system;

import com.tenio.common.utility.MathUtility;
import com.tenio.engine.ecs.basis.Context;
import com.tenio.engine.ecs.system.InitializeSystem;
import com.tenio.engine.ecs.system.RenderSystem;
import com.tenio.engine.ecs.system.implement.AbstractSystem;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.examples.example5.component.Position;
import com.tenio.examples.example5.context.GameComponent;
import com.tenio.examples.example5.context.GameEntity;
import java.awt.Color;

public final class SystemRenderer extends AbstractSystem<GameEntity>
    implements InitializeSystem, RenderSystem {

  public SystemRenderer(Context<GameEntity> context) {
    super(context);
  }

  @Override
  public void initialize() {
  }

  @Override
  public void render(final Paint paint) {
    for (var entity : getContext().getEntities().values()) {
      if (entity.hasComponent(GameComponent.POSITION)) {
        if (entity.hasComponent(GameComponent.VIEW)) {
          if (entity.hasComponent(GameComponent.ANIMATION)) {
            if (MathUtility.randBool()) {
              paint.setPenColor(Color.BLACK);
              paint.setBgColor(Color.BLACK);
            } else {
              paint.setPenColor(Color.RED);
              paint.setBgColor(Color.RED);
            }
          } else {
            paint.setPenColor(Color.RED);
            paint.setBgColor(Color.RED);
          }
          var position = (Position) entity.getComponent(GameComponent.POSITION);
          paint.drawCircle(position.x, position.y, 20);
        }
      }
    }
  }
}
