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
package com.tenio.example.example5;

import java.awt.Color;

import com.tenio.engine.ecs.base.ContextInfo;
import com.tenio.engine.heartbeat.ecs.ECSHeartBeat;
import com.tenio.example.example5.context.GameComponents;
import com.tenio.example.example5.context.GameContext;
import com.tenio.example.example5.system.InitializeSystem;
import com.tenio.example.example5.system.MoveSystem;
import com.tenio.example.example5.system.RenderSystem;
import com.tenio.example.example5.system.TeardownSystem;

/**
 * @author kong
 */
public final class ECS extends ECSHeartBeat {
	
	private GameContext __context;
	private boolean __toggleMotion = true;
	private boolean __toggleAnimation = true;
	private boolean __toggleView = true;
	
	public ECS(int cx, int cy) {
		super(cx, cy);
		
		ContextInfo info = new ContextInfo("Game", GameComponents.getComponentNames(), GameComponents.getComponentTypes(), GameComponents.getNumberComponents());
		__context = new GameContext(info);
		
		addSystem(new InitializeSystem(__context));
		addSystem(new MoveSystem(__context));
		addSystem(new RenderSystem(__context));
		addSystem(new TeardownSystem(__context));
	}
	
	@Override
	protected void _onAction1() {
		__toggleMotion = !__toggleMotion;
		setTextAction1(__toggleMotion ? "On motion" : "Off motion" , Color.LIGHT_GRAY);
		for (var entity : __context.getEntities().values()) {
			entity.setMotion(__toggleMotion);
		}
	}
	
	@Override
	protected void _onAction2() {
		__toggleAnimation = !__toggleAnimation;
		setTextAction2(__toggleAnimation ? "On animation" : "Off animation" , Color.LIGHT_GRAY);
		for (var entity : __context.getEntities().values()) {
			entity.setAnimation(__toggleAnimation);
		}
	}
	
	@Override
	protected void _onAction3() {
		__toggleView = !__toggleView;
		setTextAction3(__toggleView ? "On view" : "Off view" , Color.LIGHT_GRAY);
		for (var entity : __context.getEntities().values()) {
			entity.setView(__toggleView);
		}
	}
	
}
