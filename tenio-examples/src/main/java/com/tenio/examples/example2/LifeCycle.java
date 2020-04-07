/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.examples.example2;

import java.util.Collection;

import com.tenio.api.MessageApi;
import com.tenio.api.PlayerApi;
import com.tenio.engine.fsm.EntityManager;
import com.tenio.engine.fsm.IMessageListener;
import com.tenio.engine.fsm.MessageDispatcher;
import com.tenio.engine.fsm.entities.Telegram;
import com.tenio.engine.heartbeat.AbstractHeartBeat;
import com.tenio.engine.physic.graphic.Paint;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.element.TObject;
import com.tenio.examples.example2.constants.EntityName;
import com.tenio.examples.example2.entities.BaseEntity;
import com.tenio.examples.example2.entities.Miner;
import com.tenio.examples.example2.entities.Wife;
import com.tenio.server.Server;

/**
 * The lifecycle, all actions is performed in this class
 * 
 * @author kong
 *
 */
public final class LifeCycle extends AbstractHeartBeat implements IMessageListener {

	private static final float PERIOD_STEPS_IN_SECONDS = 1.0f;
	
	/**
	 * @see EntityManager
	 */
	private final EntityManager __entities;
	/**
	 * @see MessageDispatcher
	 */
	private final MessageDispatcher __dispatcher;

	/**
	 * @see PlayerApi#gets()
	 */
	private final Collection<AbstractPlayer> __inspectors = Server.getInstance().getPlayerApi().gets().values();
	
	/**
	 * @see MessageApi
	 */
	private final MessageApi __messageApi = Server.getInstance().getMessageApi();

	/**
	 * Making slow steps to inspect what happening
	 */
	private float __tick = 0;

	public LifeCycle() {
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
	protected void _onCreate() {
		System.out.println("HeartBeat.onCreate()");
	}

	@Override
	protected void _onUpdate(float delta) {
		if (__tick >= PERIOD_STEPS_IN_SECONDS) {
			__entities.gets().values().forEach(entity -> {
				var base = (BaseEntity) entity;
				if (base.getMood() != null) {
					// send to all inspectors
					for (var inspector : __inspectors) {
						__messageApi.sendToPlayer(inspector, "m", base.getMood());
					}
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
	protected void _onPause() {
		System.out.println("HeartBeat.onPause()");
	}

	@Override
	protected void _onResume() {
		System.out.println("HeartBeat.onResume()");
	}

	@Override
	protected void _onDispose() {
		System.out.println("HeartBeat._onDispose()");
	}

	@Override
	public void onListen(Telegram msg, boolean isHandled) {
		System.out.println("HeartBeat.onListen()");
	}

	@Override
	protected void _onRender(Paint paint) {

	}

	@Override
	protected void _onAction1() {
		System.out.println("HeartBeat._onAction1()");
	}

	@Override
	protected void _onAction2() {
		System.out.println("HeartBeat._onAction2()");
	}

	@Override
	protected void _onAction3() {
		System.out.println("HeartBeat._onAction3()");
	}

	@Override
	protected void _onMessage(TObject message) {
		System.err.println("LifeCycle._onMessage(): " + message + " at: " + System.currentTimeMillis());
	}

}
