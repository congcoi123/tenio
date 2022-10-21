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

package com.tenio.engine.heartbeat.ecs;

import com.tenio.engine.ecs.system.System;
import com.tenio.engine.ecs.system.implement.Systems;
import com.tenio.engine.heartbeat.AbstractHeartBeat;
import com.tenio.engine.message.ExtraMessage;
import com.tenio.engine.physic2d.graphic.Paint;

/**
 * The ECS system base on heart-beat.
 *
 * @see AbstractHeartBeat
 */
public class EcsHeartBeat extends AbstractHeartBeat {

  private final Systems systems = new Systems();

  public EcsHeartBeat(int viewWidth, int viewHeight) {
    super(viewWidth, viewHeight);
  }

  public void addSystem(System system) {
    systems.add(system);
  }

  public void clearSystems() {
    systems.clearSystems();
  }

  @Override
  protected void onCreate() {
    systems.initialize();
  }

  @Override
  protected void onMessage(ExtraMessage message) {
  }

  @Override
  protected void onUpdate(float deltaTime) {
    systems.execute(deltaTime);
  }

  @Override
  protected void onRender(Paint paint) {
    systems.render(paint);
  }

  @Override
  protected void onPause() {
    systems.paused(true);
  }

  @Override
  protected void onResume() {
    systems.paused(false);
  }

  @Override
  protected void onDispose() {
    systems.tearDown();
  }

  @Override
  protected void onAction1() {
  }

  @Override
  protected void onAction2() {
  }

  @Override
  protected void onAction3() {
  }
}
