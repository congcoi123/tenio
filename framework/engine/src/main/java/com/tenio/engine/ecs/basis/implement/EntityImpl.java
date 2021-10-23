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

package com.tenio.engine.ecs.basis.implement;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.pool.ElementPool;
import com.tenio.engine.ecs.basis.Component;
import com.tenio.engine.ecs.basis.Entity;
import com.tenio.engine.exception.ComponentIsNotExistedException;
import com.tenio.engine.exception.DuplicatedComponentException;

/**
 * An entity is something that exists in your game world. Again, an entity is
 * little more than a list of components. Because they are so simple, most
 * implementations won't define an entity as a concrete piece of data. Instead,
 * an entity is a unique ID, and all components that make up an entity will be
 * tagged with that ID.
 *
 * @see Entity
 **/
public class EntityImpl extends AbstractLogger implements Entity {

  private ElementPool<Component>[] componentPools = null;
  private Component[] components = null;
  private ContextInfo contextInfo = null;
  private String id = null;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public ElementPool<Component>[] getComponentPools() {
    return componentPools;
  }

  @Override
  public void setComponentPools(ElementPool<Component>[] componentPools) {
    if (this.componentPools == null) {
      this.componentPools = componentPools;
    }
  }

  @Override
  public ContextInfo getContextInfo() {
    return contextInfo;
  }

  @Override
  public void setContextInfo(ContextInfo contextInfo) {
    if (this.contextInfo == null) {
      this.contextInfo = contextInfo;
    }
    if (components == null) {
      components = new Component[contextInfo.getNumberComponents()];
    }
  }

  @Override
  public void setComponent(int index, Component component) {
    if (hasComponent(index)) {
      var e = new DuplicatedComponentException();
      error(e, "index: ", index);
      throw e;
    }

    components[index] = component;
  }

  @Override
  public void removeComponent(int index) {
    if (!hasComponent(index)) {
      var e = new ComponentIsNotExistedException();
      error(e, "index: ", index);
      throw e;
    }

    replaceComponentInternal(index, null);
  }

  @Override
  public void replaceComponent(int index, Component component) {
    if (hasComponent(index)) {
      replaceComponentInternal(index, component);
    } else {
      if (component != null) {
        setComponent(index, component);
      }
    }
  }

  private void replaceComponentInternal(int index, Component replacement) {
    var previousComponent = components[index];

    if (replacement != previousComponent) {
      components[index] = replacement;
    }
  }

  @Override
  public Component getComponent(int index) {
    return components[index];
  }

  @Override
  public Component[] getComponents() {
    return components;
  }

  @Override
  public boolean hasComponent(int index) {
    if (index < components.length) {
      return components[index] != null;
    } else {
      return false;
    }
  }

  @Override
  public boolean hasComponents(int... indices) {
    for (int index : indices) {
      if (components[index] == null) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean hasAnyComponent(int... indices) {
    for (int index : indices) {
      if (components[index] != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void removeAllComponents() {
    for (int i = 0; i < components.length; i++) {
      if (components[i] != null) {
        replaceComponent(i, null);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return this == o;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public void reset() {
    removeAllComponents();
  }
}
