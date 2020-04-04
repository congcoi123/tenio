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

import java.util.UUID;

import com.tenio.engine.ecs.common.IComponent;
import com.tenio.engine.ecs.common.IEntity;
import com.tenio.engine.ecs.pool.ComponentPool;
import com.tenio.logger.AbstractLogger;

/**
 * @author kong
 **/
public class Entity extends AbstractLogger implements IEntity {

	private ComponentPool[] __componentPools = null;
	private IComponent[] __components = null;
	private ContextInfo __contextInfo = null;
	private UUID __id = null;
	private boolean __enabled = false;

	@Override
	public void setId(UUID id) {
		__id = id;
	}

	@Override
	public UUID getId() {
		return __id;
	}

	@Override
	public void setContextInfo(ContextInfo contextInfo) {
		if (__contextInfo == null) {
			__contextInfo = contextInfo;
		}
		if (__components == null) {
			__components = new IComponent[contextInfo.getNumberComponents()];
		}
	}
	
	@Override
	public void setComponentPools(ComponentPool[] componentPools) {
		if (__componentPools == null) {
			__componentPools = componentPools;
		}
	}
	
	@Override
	public ComponentPool[] getComponentPools() {
		return __componentPools;
	}

	@Override
	public ContextInfo getContextInfo() {
		return __contextInfo;
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
			info("Entity", "entity is not enabled",
					strgen("Cannot add component", __contextInfo.getComponentNames()[index], " to ", this, " !"));
			return;
		}

		if (hasComponent(index)) {
			info("Entity", "entity had a same component",
					strgen("Cannot add component", __contextInfo.getComponentNames()[index], " to ", this, " !"));
			return;
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
			info("Entity", "entity is not enabled",
					strgen("Cannot remove component", __contextInfo.getComponentNames()[index], " to ", this, " !"));
			return;
		}

		if (!hasComponent(index)) {
			info("Entity", "entity does not has the component",
					strgen("Cannot remove component", __contextInfo.getComponentNames()[index], " to ", this, " !"));
			return;
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
			info("Entity", "entity is not enabled",
					strgen("Cannot replace component", __contextInfo.getComponentNames()[index], " to ", this, " !"));
			return;
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
			info("Entity", "entity does not have the component",
					strgen("Cannot get component", __contextInfo.getComponentNames()[index], " to ", this, " !"));
			return null;
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
		return __id.hashCode();
	}

	@Override
	public void setEnabled(boolean enabled) {
		__enabled = enabled;
	}

	@Override
	public void reset() {
		removeAllComponents();
	}

}
