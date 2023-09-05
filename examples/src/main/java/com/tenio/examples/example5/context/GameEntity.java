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

package com.tenio.examples.example5.context;

import com.tenio.engine.ecs.basis.implement.EntityImpl;
import com.tenio.examples.example5.component.Position;

public final class GameEntity extends EntityImpl {

  public boolean isAnimation() {
    return hasComponent(GameComponent.ANIMATION);
  }

  public GameEntity setAnimation(boolean value) {
    if (value != hasComponent(GameComponent.ANIMATION)) {
      if (value) {
        setComponent(GameComponent.ANIMATION, getComponentPools()[GameComponent.ANIMATION].get());
      } else {
        getComponentPools()[GameComponent.ANIMATION].repay(getComponent(GameComponent.ANIMATION));
        removeComponent(GameComponent.ANIMATION);
      }
    }
    return this;
  }

  public boolean isMotion() {
    return hasComponent(GameComponent.MOTION);
  }

  public GameEntity setMotion(boolean value) {
    if (value != hasComponent(GameComponent.MOTION)) {
      if (value) {
        setComponent(GameComponent.MOTION, getComponentPools()[GameComponent.MOTION].get());
      } else {
        getComponentPools()[GameComponent.MOTION].repay(getComponent(GameComponent.MOTION));
        removeComponent(GameComponent.MOTION);
      }
    }
    return this;
  }

  public boolean isView() {
    return hasComponent(GameComponent.VIEW);
  }

  public GameEntity setView(boolean value) {
    if (value != hasComponent(GameComponent.VIEW)) {
      if (value) {
        setComponent(GameComponent.VIEW, getComponentPools()[GameComponent.VIEW].get());
      } else {
        getComponentPools()[GameComponent.VIEW].repay(getComponent(GameComponent.VIEW));
        removeComponent(GameComponent.VIEW);
      }
    }
    return this;
  }

  public boolean hasPosition() {
    return hasComponent(GameComponent.POSITION);
  }

  public GameEntity setPosition(float x, float y) {
    var component = (Position) getComponentPools()[GameComponent.POSITION].get();
    component.x = x;
    component.y = y;
    setComponent(GameComponent.POSITION, component);
    return this;
  }

  public GameEntity replacePosition(float x, float y) {
    var component = (Position) getComponentPools()[GameComponent.POSITION].get();
    component.x = x;
    component.y = y;
    replaceComponent(GameComponent.POSITION, component);
    return this;
  }

  public GameEntity removePosition() {
    getComponentPools()[GameComponent.POSITION].repay(getComponent(GameComponent.POSITION));
    removeComponent(GameComponent.POSITION);
    return this;
  }
}
