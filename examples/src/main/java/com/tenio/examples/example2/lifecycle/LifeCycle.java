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

package com.tenio.examples.example2.lifecycle;

import com.tenio.engine.fsm.EntityManager;
import com.tenio.engine.fsm.MessageDispatcher;
import com.tenio.engine.fsm.MessageListener;
import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.engine.heartbeat.AbstractHeartBeat;
import com.tenio.engine.message.ExtraMessage;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.examples.example2.define.EntityName;
import com.tenio.examples.example2.entity.BaseEntity;
import com.tenio.examples.example2.entity.Miner;
import com.tenio.examples.example2.entity.Wife;

/**
 * The lifecycle, all actions is performed in this class.
 */
public final class LifeCycle extends AbstractHeartBeat implements MessageListener {

  private static final float PERIOD_STEPS_IN_SECONDS = 1.0f;

  private final EntityManager entityManager;
  private final MessageDispatcher messageDispatcher;

  /**
   * Making slow steps to inspect what happening.
   */
  private float tick = 0;

  private LifeCycle() {
    // create a manager
    entityManager = new EntityManager();
    messageDispatcher = new MessageDispatcher(entityManager);

    messageDispatcher.listen(this);

    // create a miner
    var Bob = new Miner(messageDispatcher, EntityName.MINER);

    // create his wife
    var Elsa = new Wife(messageDispatcher, EntityName.WIFE);

    // register them with the entity manager
    entityManager.register(Bob);
    entityManager.register(Elsa);
  }

  public static LifeCycle newInstance() {
    return new LifeCycle();
  }

  @Override
  protected void onCreate() {
    System.out.println("HeartBeat.onCreate()");
  }

  @Override
  protected void onUpdate(float delta) {
    if (tick >= PERIOD_STEPS_IN_SECONDS) {
      entityManager.gets().values().forEach(entity -> {
        var base = (BaseEntity) entity;
        if (base.getMood() != null) {
          // send to all inspectors
          // ...
        }
      });
      // need to update ...
      entityManager.update(delta);
      messageDispatcher.update(delta);

      tick = 0;
    }
    tick += delta;
  }

  @Override
  protected void onPause() {
    System.out.println("HeartBeat.onPause()");
  }

  @Override
  protected void onResume() {
    System.out.println("HeartBeat.onResume()");
  }

  @Override
  protected void onDispose() {
    System.out.println("HeartBeat._onDispose()");
  }

  @Override
  public void onListen(Telegram msg, boolean isHandled) {
    System.out.println("HeartBeat.onListen()");
  }

  @Override
  protected void onRender(Paint paint) {

  }

  @Override
  protected void onAction1() {
    System.out.println("HeartBeat._onAction1()");
  }

  @Override
  protected void onAction2() {
    System.out.println("HeartBeat._onAction2()");
  }

  @Override
  protected void onAction3() {
    System.out.println("HeartBeat._onAction3()");
  }

  @Override
  protected void onMessage(ExtraMessage message) {
    System.err.println("LifeCycle._onMessage(): " + message + " at: " + System.currentTimeMillis());
  }
}
