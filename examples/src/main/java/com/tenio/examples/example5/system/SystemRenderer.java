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
package com.tenio.examples.example5.system;

import java.awt.Color;

import com.tenio.common.utilities.MathUtility;
import com.tenio.engine.ecs.bases.Context;
import com.tenio.engine.ecs.systems.InitializeSystem;
import com.tenio.engine.ecs.systems.RenderSystem;
import com.tenio.engine.ecs.systems.implement.AbstractSystem;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.examples.example5.component.Position;
import com.tenio.examples.example5.context.GameComponents;
import com.tenio.examples.example5.context.GameEntity;

public final class SystemRenderer extends AbstractSystem<GameEntity> implements InitializeSystem, RenderSystem {

	public SystemRenderer(Context<GameEntity> context) {
		super(context);
	}

	@Override
	public void initialize() {

	}

	@Override
	public void render(final Paint paint) {
		for (var entity : getContext().getEntities().values()) {
			if (entity.hasComponent(GameComponents.POSITION)) {
				if (entity.hasComponent(GameComponents.VIEW)) {
					if (entity.hasComponent(GameComponents.ANIMATION)) {
						if (MathUtility.randBool()) {
							paint.setPenColor(Color.BLACK);
						} else {
							paint.setPenColor(Color.RED);
						}
					} else {
						paint.setPenColor(Color.RED);
					}
					var position = (Position) entity.getComponent(GameComponents.POSITION);
					paint.drawCircle(position.x, position.y, 20);
				}
			}
		}
	}

}
