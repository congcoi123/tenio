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
package com.tenio.engine.ecs;

import java.util.concurrent.atomic.AtomicInteger;

import com.tenio.engine.ecs.common.IComponent;
import com.tenio.engine.ecs.common.IEntity;
import com.tenio.engine.ecs.exceptions.EntityAlreadyHasComponentException;
import com.tenio.engine.ecs.exceptions.EntityDoesNotHaveComponentException;
import com.tenio.engine.ecs.exceptions.EntityIsNotEnabledException;

/**
 * @author Kong
 **/
public class Entity implements IEntity {

	/**
	 * Every entity must have a unique identifying number
	 */
	private final int __id;

	/**
	 * This is the next valid ID. Each time a BaseGameEntity is instantiated this
	 * value is updated
	 */
	private static AtomicInteger __nextId = new AtomicInteger();

	private final IComponent[] __components;
	private final ContextInfo __contextInfo;
	private boolean __enabled;

	private Entity(ContextInfo contextInfo) {
		__id = __nextId.getAndIncrement();
		__contextInfo = contextInfo;
		__components = new IComponent[__contextInfo.getComponentNames().length];
	}

	public static IEntity newInstance(ContextInfo contextInfo) {
		return new Entity(contextInfo);
	}
	
	/**
	 * Use with recreating id counter
	 */
	public static void resetValidID() {
		__nextId = new AtomicInteger();
	}

	/**
	 * The context manages the state of an entity. Active entities are enabled,
	 * destroyed entities are not.
	 * 
	 * @return {@link Boolean}
	 */
	@Override
	public boolean isEnabled() {
		return __enabled;
	}

	/**
	 * Adds a component at the specified index. You can only have one component at
	 * an index. Each component type must have its own constant index. The preferred
	 * way is to use the generated methods from the code generator.
	 *
	 * @param index
	 * @param component
	 */
	@Override
	public void addComponent(int index, IComponent component) {
		if (!__enabled) {
			throw new EntityIsNotEnabledException(
					"Cannot add component '" + __contextInfo.getComponentNames()[index] + "' to " + this + "!");
		}

		if (hasComponent(index)) {
			throw new EntityAlreadyHasComponentException(index,
					"Cannot add component '" + __contextInfo.getComponentNames()[index] + "' to " + this + "!",
					"You should check if an entity already has the component "
							+ "before adding it or use entity.ReplaceComponent().");
		}

		__components[index] = component;
	}

	/**
	 * Removes a component at the specified index. You can only remove a component
	 * at an index if it exists. The preferred way is to use the generated methods
	 * from the code generator.
	 *
	 * @param index
	 */
	@Override
	public void removeComponent(int index) {
		if (!__enabled) {
			throw new EntityIsNotEnabledException(
					"Cannot remove component!" + __contextInfo.getComponentNames()[index] + "' from " + this + "!");
		}

		if (!hasComponent(index)) {
			String errorMsg = "Cannot remove component " + __contextInfo.getComponentNames()[index] + "' from " + this
					+ "You should check if an entity has the component before removing it.";
			throw new EntityDoesNotHaveComponentException(errorMsg, index);
		}
		__replaceComponentInternal(index, null);
	}

	/**
	 * Replaces an existing component at the specified index or adds it if it
	 * doesn't exist yet. The preferred way is to use the generated methods from the
	 * code generator.
	 *
	 * @param index
	 * @param component
	 */
	@Override
	public void replaceComponent(int index, IComponent component) {
		if (!__enabled) {
			throw new EntityIsNotEnabledException(
					"Cannot replace component!" + __contextInfo.getComponentNames()[index] + "' on " + this + "!");
		}

		if (hasComponent(index)) {
			__replaceComponentInternal(index, component);
		} else {
			if (component != null) {
				addComponent(index, component);
			}
		}
	}

	private void __replaceComponentInternal(int index, IComponent replacement) {
		IComponent previousComponent = __components[index];

		if (replacement != previousComponent) {
			__components[index] = replacement;
		}
	}

	/**
	 * Returns a component at the specified index. You can only get a component at
	 * an index if it exists. The preferred way is to use the generated methods from
	 * the code generator.
	 *
	 * @param index
	 * @return {@link IComponent}
	 */
	@Override
	public IComponent getComponent(int index) {
		if (!hasComponent(index)) {
			String errorMsg = "Cannot get component " + __contextInfo.getComponentNames()[index] + "' from " + this
					+ "!  You should check if an entity has the component before getting it.";
			throw new EntityDoesNotHaveComponentException(errorMsg, index);
		}
		return __components[index];
	}

	/**
	 * @return {@link IComponent}[] Returns all added components.
	 */
	@Override
	public IComponent[] getComponents() {
		return __components;
	}

	/**
	 * Determines whether this entity has a component at the specified index.
	 *
	 * @param index
	 * @return {@link Boolean}
	 */
	@Override
	public boolean hasComponent(int index) {
		if (index < __components.length) {
			return __components[index] != null;
		} else {
			return false;
		}
	}

	/**
	 * Determines whether this entity has components at all the specified indices.
	 *
	 * @param indices
	 * @return {@link Boolean}
	 */
	@Override
	public boolean hasComponents(int... indices) {
		for (int index : indices) {
			if (__components[index] == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether this entity has a component at any of the specified
	 * indices.
	 *
	 * @param indices
	 * @return {@link Boolean}
	 */
	@Override
	public boolean hasAnyComponent(int... indices) {
		for (int index : indices) {
			if (__components[index] != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes all components.
	 */
	@Override
	public void removeAllComponents() {
		for (int i = 0; i < __components.length; i++) {
			if (__components[i] != null) {
				replaceComponent(i, null);
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		return this == (Entity) o;

	}

	@Override
	public int hashCode() {
		return __id;
	}

	@Override
	public String toString() {
		return "Entity{" + "Index=" + __id + ", enabled=" + __enabled + ", contextName=" + __contextInfo.getName()
				+ ", components=" + __components + '}';
	}

}
