/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.engine.ecs.basis;

import com.tenio.common.pool.ElementPool;
import com.tenio.engine.ecs.basis.implement.ContextInfo;
import com.tenio.engine.ecs.pool.ComponentPool;

/**
 * An entity is something that exists in your game world. Again, an entity is
 * little more than a list of components. Because they are so simple, most
 * implementations won't define an entity as a concrete piece of data. Instead,
 * an entity is a unique ID, and all components that make up an entity will be
 * tagged with that ID.
 */
public interface Entity {

  /**
   * Retrieves the entity's id.
   *
   * @return entity's id
   */
  String getId();

  /**
   * Set new id.
   *
   * @param id the desired id
   */
  void setId(String id);

  /**
   * Retrieves the list of component pools.
   *
   * @return an array of {@link ComponentPool}
   */
  ElementPool<Component>[] getComponentPools();

  /**
   * Set list of component pools, each component pool manages specific component
   * type.
   *
   * @param componentPools an array of {@link ComponentPool}
   */
  void setComponentPools(ElementPool<Component>[] componentPools);

  /**
   * Retrieves the context information.
   *
   * @return see {@link ContextInfo}
   */
  ContextInfo getContextInfo();

  /**
   * Set context information.
   *
   * @param contextInfo see {@link ContextInfo}
   */
  void setContextInfo(ContextInfo contextInfo);

  /**
   * Set new component.
   *
   * @param index     the component index
   * @param component the component object
   */
  void setComponent(int index, Component component);

  /**
   * Remove component by index.
   *
   * @param index the component index
   */
  void removeComponent(int index);

  /**
   * Replace old component by new component by index.
   *
   * @param index     the component index
   * @param component the component object
   */
  void replaceComponent(int index, Component component);

  /**
   * Retrieves the component by index.
   *
   * @param index the component index
   * @return the corresponding component
   */
  Component getComponent(int index);

  /**
   * Retrieves a list of the current entity.
   *
   * @return list of components
   */
  Component[] getComponents();

  /**
   * Check if the component is existed or not.
   *
   * @param index component index
   * @return <b>true</b> if the component is existed, <b>false</b> otherwise
   */
  boolean hasComponent(int index);

  /**
   * Check if all the components in list are existed or not.
   *
   * @param indices list of component indices
   * @return <b>true</b> if the all components are existed, <b>false</b> otherwise
   */
  boolean hasComponents(int... indices);

  /**
   * Check if one of components in list are existed or not.
   *
   * @param indices list of component indices
   * @return <b>true</b> if the one of components in list is existed, <b>false</b> otherwise
   */
  boolean hasAnyComponent(int... indices);

  /**
   * Remove all components.
   */
  void removeAllComponents();

  /**
   * Reset entity.
   */
  void reset();
}
