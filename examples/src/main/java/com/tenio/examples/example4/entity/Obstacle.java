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

package com.tenio.examples.example4.entity;

import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.graphic.Renderable;
import java.awt.Color;

/**
 * Just a simple obstacle class.
 */
public final class Obstacle extends BaseGameEntity implements Renderable {

  public Obstacle(float x, float y, float radius) {
    super(0, x, y, radius);
  }

  @Override
  public void render(Paint paint) {
    paint.setPenColor(Color.BLACK);
    paint.drawCircle(getPositionX(), getPositionY(), getBoundingRadius());
  }

  @Override
  public void update(float delta) {
  }

  @Override
  public boolean handleMessage(Telegram msg) {
    return false;
  }
}
