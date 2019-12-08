package com.tenio.engine.entitas;

import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.tenio.engine.entitas.api.ContextInfo;
import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.api.entitas.IAERC;
import com.tenio.engine.entitas.api.entitas.IEntity;
import com.tenio.engine.entitas.api.events.EntityComponentChanged;
import com.tenio.engine.entitas.api.events.EntityComponentReplaced;
import com.tenio.engine.entitas.api.events.EntityEvent;
import com.tenio.engine.entitas.caching.EntitasCache;
import com.tenio.engine.entitas.exceptions.EntityAlreadyHasComponentException;
import com.tenio.engine.entitas.exceptions.EntityDoesNotHaveComponentException;
import com.tenio.engine.entitas.exceptions.EntityIsNotEnabledException;
import com.tenio.engine.entitas.factories.EntitasCollections;

/**
 * <p>
 * Use <tt>context.CreateEntity()</tt> to create a new entity and
 * <br>
 * <tt>entity.Destroy()</tt> to destroy it. You can add, replace and remove
 * <br>
 * IComponent to an entity.
 * </p>
 * 
 * @author Rubentxu
 **/
public class Entity implements IEntity {

	/**
	 * Occurs when a component gets added. All event handlers will be removed when
	 * the entity gets destroyed by the context.
	 */
	@SuppressWarnings("rawtypes")
	private Set<EntityComponentChanged> __onComponentAddeds = EntitasCollections.createSet();

	/**
	 * Occurs when a component gets removed. All event handlers will be removed when
	 * the entity gets destroyed by the context.
	 */
	@SuppressWarnings("rawtypes")
	private Set<EntityComponentChanged> __onComponentRemoveds = EntitasCollections.createSet();

	/**
	 * Occurs when a component gets replaced. All event handlers will be removed
	 * when the entity gets destroyed by the context.
	 */
	@SuppressWarnings("rawtypes")
	private Set<EntityComponentReplaced> __onComponentReplaceds = EntitasCollections.createSet();

	/**
	 * Occurs when an entity gets released and is not retained anymore. All event
	 * handlers will be removed when the entity gets destroyed by the context.
	 */
	@SuppressWarnings("rawtypes")
	private Set<EntityEvent> __onEntityReleaseds = EntitasCollections.createSet();

	/**
	 * Occurs when calling <tt>entity.Destroy()</tt>. All event handlers will be removed when
	 * the entity gets destroyed by the context.
	 */
	@SuppressWarnings("rawtypes")
	private Set<EntityEvent> __onEntityDestroys = EntitasCollections.createSet();

	private int __creationIndex;
	private boolean __enabled;
	private int __totalComponents;
	private IComponent[] __components;
	private Stack<IComponent>[] __componentPools;
	private ContextInfo __contextInfo;
	private IAERC __aerc;

	private IComponent[] __componentsCache;
	private int[] __componentIndicesCache;

	/**
	 * The total amount of components an entity can possibly have.
	 * 
	 * @return {@link Integer}
	 */
	@Override
	public int getTotalComponents() {
		return __totalComponents;
	}

	/**
	 * Each entity has its own unique creationIndex which will be set by the context
	 * when you create the entity.
	 * 
	 * @return {@link Integer}
	 */
	@Override
	public int getCreationIndex() {
		return __creationIndex;
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
	 * <tt>componentPools</tt> is set by the context which created the entity and is used to
	 * reuse removed components. Removed components will be pushed to the
	 * componentPool. Use <tt>entity.CreateComponent(index, type)</tt> to get a new or
	 * reusable component from the componentPool. Use <tt>entity.GetComponentPool(index)</tt>
	 * to get a componentPool for a specific component index.
	 * 
	 * @return Stack<IComponent>[]
	 */
	@Override
	public Stack<IComponent>[] getComponentPools() {
		return __componentPools;
	}

	/**
	 * The contextInfo is set by the context which created the entity and contains
	 * information about the context. It's used to provide better error messages.
	 * 
	 * @return {@link ContextInfo}
	 */
	@Override
	public ContextInfo getContextInfo() {
		return __contextInfo;
	}

	/**
	 * Automatic Entity Reference Counting (AERC) is used internally to prevent
	 * pooling retained entities. If you use retain manually you also have to
	 * release it manually at some point.
	 * 
	 * @return {@link IAERC}
	 */
	@Override
	public IAERC getAERC() {
		return __aerc;
	}

	/**
	 * Initialize Entity
	 *
	 * @param creationIndex
	 * @param totalComponents
	 * @param componentPools
	 * @param contextInfo
	 * @param aerc
	 */
	@Override
	public void initialize(int creationIndex, int totalComponents, Stack<IComponent>[] componentPools,
			ContextInfo contextInfo, IAERC aerc) {
		reactivate(creationIndex);

		__totalComponents = totalComponents;
		__components = new IComponent[totalComponents];
		__componentPools = componentPools;

		if (contextInfo != null) {
			__contextInfo = contextInfo;
		} else {
			__contextInfo = __createDefaultContextInfo();
		}
		__aerc = (aerc == null) ? new SafeAERC(this) : aerc;
	}

	private ContextInfo __createDefaultContextInfo() {
		String[] componentNames = new String[__totalComponents];
		for (int i = 0; i < componentNames.length; i++) {
			componentNames[i] = String.valueOf(i);
		}
		return new ContextInfo("No Context", componentNames, null);
	}

	@Override
	public void reactivate(int creationIndex) {
		__creationIndex = creationIndex;
		__enabled = true;
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
					"Cannot add component '" + __contextInfo.componentNames[index] + "' to " + this + "!");
		}

		if (hasComponent(index)) {
			throw new EntityAlreadyHasComponentException(index,
					"Cannot add component '" + __contextInfo.componentNames[index] + "' to " + this + "!",
					"You should check if an entity already has the component "
							+ "before adding it or use entity.ReplaceComponent().");
		}

		__components[index] = component;
		__componentsCache = null;
		__componentIndicesCache = null;
		__notifyComponentAdded(index, component);
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
					"Cannot remove component!" + __contextInfo.componentNames[index] + "' from " + this + "!");
		}

		if (!hasComponent(index)) {
			String errorMsg = "Cannot remove component " + __contextInfo.componentNames[index] + "' from " + this
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
					"Cannot replace component!" + __contextInfo.componentNames[index] + "' on " + this + "!");
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
			__componentsCache = null;
			if (replacement != null) {
				__notifyComponentReplaced(index, previousComponent, replacement);
			} else {
				__componentIndicesCache = null;
				__notifyComponentRemoved(index, previousComponent);
			}
			getComponentPool(index).push(previousComponent);

		} else {
			__notifyComponentReplaced(index, previousComponent, replacement);
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
			String errorMsg = "Cannot get component " + __contextInfo.componentNames[index] + "' from " + this
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
		if (__componentsCache == null) {
			List<IComponent> componentsCache = EntitasCache.getIComponentList();

			for (int i = 0; i < __components.length; i++) {
				IComponent component = __components[i];
				if (component != null) {
					componentsCache.add(component);
				}
			}
			__componentsCache = new IComponent[componentsCache.size()];
			componentsCache.toArray(__componentsCache);
			EntitasCache.pushIComponentList(componentsCache);
		}
		return __componentsCache;
	}

	/**
	 * @return {@link Integer}[] Returns all indices of added components.
	 */
	@Override
	public int[] getComponentIndices() {
		if (__componentIndicesCache == null) {
			List<Integer> indices = EntitasCache.getIntArray();
			for (int i = 0; i < __components.length; i++) {
				if (__components[i] != null) {
					indices.add(i);
				}
			}
			__componentIndicesCache = new int[indices.size()];
			for (int i = 0; i < indices.size(); i++) {
				__componentIndicesCache[i] = indices.get(i);

			}
			EntitasCache.pushIntArray(indices);
		}
		return __componentIndicesCache;
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

	/**
	 * Returns the componentPool for the specified component index. componentPools
	 * is set by the context which created the entity and is used to reuse removed
	 * components. Removed components will be pushed to the componentPool. Use
	 * entity.CreateComponent(index, type) to get a new or reusable component from
	 * the componentPool.
	 *
	 * @param index
	 * @return Stack<IComponent>
	 */
	@Override
	public Stack<IComponent> getComponentPool(int index) {
		Stack<IComponent> componentPool = __componentPools[index];
		if (componentPool == null) {
			componentPool = EntitasCollections.createStack();
			__componentPools[index] = componentPool;
		}
		return componentPool;
	}

	/**
	 * Returns a new or reusable component from the componentPool for the specified
	 * component index.
	 *
	 * @param index
	 * @param clazz
	 * @return {@link IComponent}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IComponent createComponent(int index, Class clazz) {
		Stack<IComponent> componentContext = getComponentPool(index);
		try {
			if (componentContext.size() > 0) {
				return componentContext.pop();
			} else {
				return (IComponent) clazz.cast(clazz.getConstructor((Class[]) null).newInstance());
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns a new or reusable component from the componentPool for the specified
	 * component index. The component's type is assigned by previous component in
	 * this index
	 *
	 * @param index
	 * @return {@link IComponent}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IComponent createComponent(int index) {
		Stack<IComponent> componentPool = getComponentPool(index);
		try {
			if (componentPool.size() > 0) {
				return componentPool.pop();
			} else {
				Class<?> clazz = __contextInfo.componentTypes[index];
				return (IComponent) clazz.cast(clazz.getConstructor((Class[]) null).newInstance());
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns a component at the specified index or null if it does not exists.
	 *
	 * @param index
	 * @return {@link IComponent}
	 */
	@Override
	public IComponent recoverComponent(int index) {
		Stack<IComponent> componentContext = getComponentPool(index);
		if (componentContext.size() > 0) {
			return componentContext.pop();
		}
		return null;
	}

	/**
	 * @return Returns the number of objects that retain this entity.
	 */
	@Override
	public int retainCount() {
		return __aerc.retainCount();
	}

	/**
	 * Retains the entity. An owner can only retain the same entity once.
	 * <tt>Retain/Release</tt> is part of
	 * <tt>AERC (Automatic Entity Reference Counting)</tt> and is used internally to
	 * prevent pooling retained entities. If you use retain manually you also have
	 * to release it manually at some point.
	 *
	 * @param owner
	 */
	@Override
	public void retain(Object owner) {
		__aerc.retain(owner);
	}

	/**
	 * Releases the entity. An owner can only release an entity if it retains it.
	 * <tt>Retain/Release</tt> is part of
	 * <tt>AERC (Automatic Entity Reference Counting)</tt> and is used internally to
	 * prevent pooling retained entities. If you use retain manually you also have
	 * to release it manually at some point.
	 *
	 * @param owner
	 */
	@Override
	public void release(Object owner) {
		__aerc.release(owner);
		if (__aerc.retainCount() == 0) {
			__notifyEntityReleased();
		}
	}

	/**
	 * Dispatches <tt>OnDestroyEntity</tt> which will start the destroy process.
	 */
	@Override
	public void destroy() {
		if (!__enabled) {
			throw new EntityIsNotEnabledException("Cannot destroy " + this + "!");
		}
		__notifyDestroyEntity();
	}

	/**
	 * This method is used internally. Don't call it yourself. Use
	 * <tt>entity.Destroy();</tt>
	 */
	@Override
	public void internalDestroy() {
		removeAllComponents();
		clearEventsListener();
		__enabled = false;
	}

	@Override
	public void clearEventsListener() {
		__onComponentAddeds.clear();
		__onComponentRemoveds.clear();
		__onComponentReplaceds.clear();
		__onEntityReleaseds.clear();
		__onEntityDestroys.clear();
	}

	/**
	 * Do not call this method manually. This method is called by the context.
	 */
	public void removeAllOnEntityReleasedHandlers() {
		__onEntityReleaseds.clear();
	}

	@SuppressWarnings("rawtypes")
	public void onComponentAdded(EntityComponentChanged listener) {
		__onComponentAddeds.add(listener);
	}

	@SuppressWarnings("rawtypes")
	public void onComponentRemoved(EntityComponentChanged listener) {
		__onComponentRemoveds.add(listener);
	}

	@SuppressWarnings("rawtypes")
	public void onComponentReplaced(EntityComponentReplaced listener) {
		__onComponentReplaceds.add(listener);
	}

	@SuppressWarnings("rawtypes")
	public void onEntityReleased(EntityEvent listener) {
		__onEntityReleaseds.add(listener);
	}

	@SuppressWarnings("rawtypes")
	public void onEntityInDestroy(EntityEvent listener) {
		__onEntityDestroys.add(listener);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void __notifyComponentAdded(int index, IComponent component) {
		for (EntityComponentChanged listener : __onComponentAddeds) {
			listener.changed(this, index, component);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void __notifyComponentRemoved(int index, IComponent component) {
		for (EntityComponentChanged listener : __onComponentRemoveds) {
			listener.changed(this, index, component);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void __notifyComponentReplaced(int index, IComponent previousComponent, IComponent newComponent) {
		for (EntityComponentReplaced listener : __onComponentReplaceds) {
			listener.replaced(this, index, previousComponent, newComponent);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void __notifyEntityReleased() {
		for (EntityEvent listener : __onEntityReleaseds) {
			listener.released(this);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void __notifyDestroyEntity() {
		for (EntityEvent listener : __onEntityDestroys) {
			listener.released(this);
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
		return __creationIndex;
	}

	@Override
	public String toString() {
		return "Entity{" + "creationIndex=" + __creationIndex + ", enabled=" + __enabled + ", contextName="
				+ __contextInfo.contextName + ", components=" + __components + ", componentIndicesCache="
				+ __componentIndicesCache + '}';
	}

}
