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

import java.awt.Color;

import com.tenio.engine.ecs.bases.implement.ContextInfo;
import com.tenio.engine.heartbeat.ecs.EcsHeartBeat;
import com.tenio.examples.example5.context.GameComponents;
import com.tenio.examples.example5.context.GameContext;
import com.tenio.examples.example5.system.SystemInitialization;
import com.tenio.examples.example5.system.SystemMovement;
import com.tenio.examples.example5.system.SystemRenderer;
import com.tenio.examples.example5.system.SystemTeardown;

public final class ECS extends EcsHeartBeat {

	private GameContext __context;
	private boolean __toggleMotion = true;
	private boolean __toggleAnimation = true;
	private boolean __toggleView = true;

	public ECS(int cx, int cy) {
		super(cx, cy);

		ContextInfo info = new ContextInfo("Game", GameComponents.getComponentNames(),
				GameComponents.getComponentTypes(), GameComponents.getNumberComponents());
		__context = new GameContext(info);

		addSystem(new SystemInitialization(__context));
		addSystem(new SystemMovement(__context));
		addSystem(new SystemRenderer(__context));
		addSystem(new SystemTeardown(__context));
	}

	@Override
	protected void __onAction1() {
		__toggleMotion = !__toggleMotion;
		setTextAction1(__toggleMotion ? "Stop" : "Move", Color.LIGHT_GRAY);
		for (var entity : __context.getEntities().values()) {
			entity.setMotion(__toggleMotion);
		}
	}

	@Override
	protected void __onAction2() {
		__toggleAnimation = !__toggleAnimation;
		setTextAction2(__toggleAnimation ? "Off animation" : "On animation", Color.LIGHT_GRAY);
		for (var entity : __context.getEntities().values()) {
			entity.setAnimation(__toggleAnimation);
		}
	}

	@Override
	protected void __onAction3() {
		__toggleView = !__toggleView;
		setTextAction3(__toggleView ? "Invisible" : "Visible", Color.LIGHT_GRAY);
		for (var entity : __context.getEntities().values()) {
			entity.setView(__toggleView);
		}
	}

}
