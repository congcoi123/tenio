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
package com.tenio.example.example5.system;

import com.tenio.engine.ecs.base.IContext;
import com.tenio.engine.ecs.system.AbstractSystem;
import com.tenio.engine.ecs.system.IExecuteSystem;
import com.tenio.engine.ecs.system.IInitializeSystem;
import com.tenio.example.example5.component.Position;
import com.tenio.example.example5.constant.Constants;
import com.tenio.example.example5.context.GameComponents;
import com.tenio.example.example5.context.GameEntity;

/**
 * @author kong
 */
public final class MoveSystem extends AbstractSystem<GameEntity> implements IInitializeSystem, IExecuteSystem {

	public MoveSystem(IContext<GameEntity> context) {
		super(context);
	}

	@Override
	public void initialize() {

	}

	@Override
	public void execute(float deltaTime) {
		for (var entity : getContext().getEntities().values()) {
			if (entity.hasComponents(GameComponents.POSITION, GameComponents.MOTION)) {
				var position = (Position) entity.getComponent(GameComponents.POSITION);
				if (position.x < Constants.DESIGN_WIDTH) {
					position.x += deltaTime;
				} else {
					position.x = 0;
				}
			}
		}
	}

}
