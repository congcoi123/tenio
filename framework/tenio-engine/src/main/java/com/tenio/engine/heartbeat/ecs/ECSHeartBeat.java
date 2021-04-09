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
package com.tenio.engine.heartbeat.ecs;

import com.tenio.engine.ecs.system.ISystem;
import com.tenio.engine.ecs.system.Systems;
import com.tenio.engine.heartbeat.AbstractHeartBeat;
import com.tenio.engine.message.IMessage;
import com.tenio.engine.physic2d.graphic.Paint;

/**
 * The ECS system base on heart-beat
 * 
 * @see AbstractHeartBeat
 * 
 * @author kong
 */
public class ECSHeartBeat extends AbstractHeartBeat {

	private final Systems __systems = new Systems();

	public ECSHeartBeat(int viewWidth, int viewHeight) {
		super(viewWidth, viewHeight);
	}

	public void addSystem(ISystem system) {
		__systems.add(system);
	}

	public void clearSystems() {
		__systems.clearSystems();
	}

	@Override
	protected void _onCreate() {
		__systems.initialize();
	}

	@Override
	protected void _onMessage(IMessage message) {
	}

	@Override
	protected void _onUpdate(float deltaTime) {
		__systems.execute(deltaTime);
	}

	@Override
	protected void _onRender(Paint paint) {
		__systems.render(paint);
	}

	@Override
	protected void _onPause() {
		__systems.paused(true);
	}

	@Override
	protected void _onResume() {
		__systems.paused(false);
	}

	@Override
	protected void _onDispose() {
		__systems.tearDown();
	}

	@Override
	protected void _onAction1() {
	}

	@Override
	protected void _onAction2() {
	}

	@Override
	protected void _onAction3() {
	}

}
