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
package com.tenio.ecs.model.system;

import com.tenio.ecs.model.GameEntity;
import com.tenio.engine.ecs.base.IContext;
import com.tenio.engine.ecs.system.AbstractSystem;
import com.tenio.engine.ecs.system.IExecuteSystem;
import com.tenio.engine.ecs.system.IInitializeSystem;
import com.tenio.engine.ecs.system.IRenderSystem;
import com.tenio.engine.ecs.system.ITearDownSystem;
import com.tenio.engine.physic2d.graphic.Paint;

/**
 * @author kong
 */
public final class TestSystem extends AbstractSystem<GameEntity>
		implements IInitializeSystem, IExecuteSystem, IRenderSystem, ITearDownSystem {

	private boolean __flagInitialize;
	private boolean __flagExecute;
	private boolean __flagRender;
	private boolean __flagTearDown;

	public TestSystem(IContext<GameEntity> context) {
		super(context);

		__flagInitialize = false;
		__flagExecute = false;
		__flagRender = false;
		__flagTearDown = false;
	}

	@Override
	public void initialize() {
		__flagInitialize = true;
	}

	@Override
	public void execute(float deltaTime) {
		__flagExecute = true;
	}

	@Override
	public void render(Paint paint) {
		__flagRender = true;
	}

	@Override
	public void tearDown() {
		__flagTearDown = true;
	}

	public boolean isInitialized() {
		return __flagInitialize;
	}

	public boolean isExecuted() {
		return __flagExecute;
	}

	public boolean isRendered() {
		return __flagRender;
	}

	public boolean isTearDown() {
		return __flagTearDown;
	}

}
