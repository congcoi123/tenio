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
package com.tenio.examples.example2.lifecycle;

import com.tenio.engine.fsm.EntityManager;
import com.tenio.engine.fsm.MessageListener;
import com.tenio.engine.fsm.MessageDispatcher;
import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.engine.heartbeat.AbstractHeartBeat;
import com.tenio.engine.message.EMessage;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.examples.example2.defines.EntityName;
import com.tenio.examples.example2.entities.BaseEntity;
import com.tenio.examples.example2.entities.Miner;
import com.tenio.examples.example2.entities.Wife;

/**
 * The lifecycle, all actions is performed in this class
 */
public final class LifeCycle extends AbstractHeartBeat implements MessageListener {

	private static final float PERIOD_STEPS_IN_SECONDS = 1.0f;

	private final EntityManager __entities;
	private final MessageDispatcher __dispatcher;

	/**
	 * Making slow steps to inspect what happening
	 */
	private float __tick = 0;

	public static LifeCycle newInstance() {
		return new LifeCycle();
	}

	private LifeCycle() {
		// create a manager
		__entities = new EntityManager();
		__dispatcher = new MessageDispatcher(__entities);

		__dispatcher.listen(this);

		// create a miner
		var Bob = new Miner(__dispatcher, EntityName.MINER);

		// create his wife
		var Elsa = new Wife(__dispatcher, EntityName.WIFE);

		// register them with the entity manager
		__entities.register(Bob);
		__entities.register(Elsa);

	}

	@Override
	protected void __onCreate() {
		System.out.println("HeartBeat.onCreate()");
	}

	@Override
	protected void __onUpdate(float delta) {
		if (__tick >= PERIOD_STEPS_IN_SECONDS) {
			__entities.gets().values().forEach(entity -> {
				var base = (BaseEntity) entity;
				if (base.getMood() != null) {
					// send to all inspectors
					// ...
				}
			});
			// need to update ...
			__entities.update(delta);
			__dispatcher.update(delta);

			__tick = 0;
		}
		__tick += delta;
	}

	@Override
	protected void __onPause() {
		System.out.println("HeartBeat.onPause()");
	}

	@Override
	protected void __onResume() {
		System.out.println("HeartBeat.onResume()");
	}

	@Override
	protected void __onDispose() {
		System.out.println("HeartBeat._onDispose()");
	}

	@Override
	public void onListen(Telegram msg, boolean isHandled) {
		System.out.println("HeartBeat.onListen()");
	}

	@Override
	protected void __onRender(Paint paint) {

	}

	@Override
	protected void __onAction1() {
		System.out.println("HeartBeat._onAction1()");
	}

	@Override
	protected void __onAction2() {
		System.out.println("HeartBeat._onAction2()");
	}

	@Override
	protected void __onAction3() {
		System.out.println("HeartBeat._onAction3()");
	}

	@Override
	protected void __onMessage(EMessage message) {
		System.err.println("LifeCycle._onMessage(): " + message + " at: " + System.currentTimeMillis());
	}

}
