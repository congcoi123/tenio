package com.tenio.engine.entitas;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import com.tenio.engine.entitas.api.ContextInfo;
import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.api.IContext;
import com.tenio.engine.entitas.api.IGroup;
import com.tenio.engine.entitas.api.entitas.EntityBaseFactory;
import com.tenio.engine.entitas.api.entitas.IAERC;
import com.tenio.engine.entitas.api.entitas.IEntity;
import com.tenio.engine.entitas.api.entitas.IEntityIndex;
import com.tenio.engine.entitas.api.events.ContextEntityChanged;
import com.tenio.engine.entitas.api.events.ContextGroupChanged;
import com.tenio.engine.entitas.api.events.EntityComponentChanged;
import com.tenio.engine.entitas.api.events.EntityComponentReplaced;
import com.tenio.engine.entitas.api.events.EntityEvent;
import com.tenio.engine.entitas.api.events.GroupChanged;
import com.tenio.engine.entitas.api.matcher.IMatcher;
import com.tenio.engine.entitas.caching.ObjectPool;
import com.tenio.engine.entitas.collector.Collector;
import com.tenio.engine.entitas.collector.TriggerOnEvent;
import com.tenio.engine.entitas.exceptions.ContextDoesNotContainEntityException;
import com.tenio.engine.entitas.exceptions.ContextEntityIndexDoesAlreadyExistException;
import com.tenio.engine.entitas.exceptions.ContextEntityIndexDoesNotExistException;
import com.tenio.engine.entitas.exceptions.ContextInfoException;
import com.tenio.engine.entitas.exceptions.ContextStillHasRetainedEntitiesException;
import com.tenio.engine.entitas.exceptions.EntityIsNotDestroyedException;
import com.tenio.engine.entitas.factories.EntitasCollections;
import com.tenio.engine.entitas.group.Group;
import com.tenio.engine.entitas.group.GroupEvent;

/**
 * <p>
 * A context manages the lifecycle of entities and groups. You can create and
 * <br>
 * destroy entities and get groups of entities. The preferred way to create a
 * <br>
 * context is to use the generated methods from the code generator, e.g.
 * <br>
 * <tt>GameContext context = new GameContext();</tt>
 * </p>
 * 
 * @author Rubentxu
 */
public class Context<TEntity extends Entity> implements IContext<TEntity> {

	/// Occurs when an entity gets created.
	private Set<ContextEntityChanged> __onEntityCreateds = EntitasCollections.createSet();
	/// Occurs when an entity will be destroyed.
	private Set<ContextEntityChanged> __onEntityWillBeDestroyeds = EntitasCollections.createSet();
	/// Occurs when an entity got destroyed.
	private Set<ContextEntityChanged> __onEntityDestroyeds = EntitasCollections.createSet();
	/// Occurs when a group gets created for the first time.
	private Set<ContextGroupChanged> __onGroupCreateds = EntitasCollections.createSet();

	@SuppressWarnings("rawtypes")
	private ObjectPool __groupChangedListPool;
	private int __totalComponents;
	private Class<TEntity> __entityType;
	@SuppressWarnings("rawtypes")
	private Map<IMatcher, Group<TEntity>> __groups;
	private List<Group<TEntity>>[] __groupsForIndex;
	private EntityComponentChanged<TEntity> __cachedComponentChanged;
	private EntityComponentReplaced<TEntity> __cachedComponentReplaced;
	private EntityEvent<TEntity> __cachedEntityReleased;
	private EntityEvent<TEntity> __cachedEntityDestroyed;
	private int __creationIndex;
	private Set<TEntity> __entities;
	private Stack<TEntity> __reusableEntities;
	private Set<TEntity> __retainedEntities;
	private TEntity[] __entitiesCache;
	private Map<String, IEntityIndex> __entityIndices;
	private EntityBaseFactory<TEntity> __factoryEntiy;
	private ContextInfo __contextInfo;
	private Stack<IComponent>[] __componentPools;
	private Function<TEntity, IAERC> __aercFactory;

	/**
	 * The preferred way to create a context is to use the generated methods from
	 * the code generator, e.g. GameContext context = new GameContext();
	 * 
	 * @param totalComponents
	 * @param startCreationIndex
	 * @param contexInfo
	 * @param factoryMethod
	 * @param aercFactory
	 */
	@SuppressWarnings("unchecked")
	public Context(int totalComponents, int startCreationIndex, ContextInfo contexInfo,
			EntityBaseFactory<TEntity> factoryMethod, Function<TEntity, IAERC> aercFactory) {
		__totalComponents = totalComponents;
		__creationIndex = startCreationIndex;
		__factoryEntiy = factoryMethod;

		if (contexInfo != null) {
			__contextInfo = contexInfo;

			if (contexInfo.componentNames.length != totalComponents) {
				throw new ContextInfoException(this, contexInfo);
			}
		} else {
			__contextInfo = __createDefaultContextInfo();
		}

		__aercFactory = aercFactory == null ? (entity) -> new SafeAERC(entity) : aercFactory;

		__groupsForIndex = new List[__totalComponents];
		__componentPools = new Stack[totalComponents];
		__entityIndices = EntitasCollections.createMap();

		__reusableEntities = EntitasCollections.createStack();
		__retainedEntities = EntitasCollections.createSet();
		__entities = EntitasCollections.createSet();
		__groups = EntitasCollections.createMap();
		__groupChangedListPool = new ObjectPool<>(() -> {
			return EntitasCollections.createList();
		}, list -> list.clear());

		__cachedComponentChanged = (TEntity e, int idx, IComponent c) -> {
			__updateGroupsComponentAddedOrRemoved(e, idx, c, __groupsForIndex);
		};

		__cachedComponentReplaced = (final TEntity e, final int idx, final IComponent pComponent,
				final IComponent nComponent) -> {
			__updateGroupsComponentReplaced(e, idx, pComponent, nComponent, __groupsForIndex);
		};

		__cachedEntityReleased = (final TEntity e) -> {
			__onEntityReleased(e);
		};

		__cachedEntityDestroyed = (final TEntity e) -> {
			__onEntityInDestroy(e);
		};
		__entityType = (Class<TEntity>) __factoryEntiy.create().getClass();

	}

	private ContextInfo __createDefaultContextInfo() {
		String[] componentNames = new String[__totalComponents];
		String prefix = "Index ";
		for (int i = 0; i < componentNames.length; i++) {
			componentNames[i] = prefix + i;
		}

		return new ContextInfo("Unnamed Context", componentNames, null);
	}

	/**
	 * Creates a new entity or gets a reusable entity from the internal ObjectPool
	 * for entities.
	 * 
	 * @return Entity
	 */
	@Override
	public TEntity createEntity() {
		TEntity entity;
		if (__reusableEntities.size() > 0) {
			entity = __reusableEntities.pop();
			entity.reactivate(__creationIndex++);
		} else {
			entity = __factoryEntiy.create();
			entity.initialize(__creationIndex++, __totalComponents, __componentPools, __contextInfo, __aercFactory.apply(entity));
		}

		__entities.add(entity);
		entity.retain(this);
		__entitiesCache = null;
		
		entity.onComponentAdded(__cachedComponentChanged);
		entity.onComponentRemoved(__cachedComponentChanged);
		entity.onComponentReplaced(__cachedComponentReplaced);
		entity.onEntityReleased(__cachedEntityReleased);
		entity.onEntityInDestroy(__cachedEntityDestroyed);

		__notifyEntityCreated(entity);

		return entity;
	}

	/**
	 * Destroys all entities in the context. Throws an exception if there are still
	 * retained entities.
	 */
	@Override
	public void destroyAllEntities() {
		for (TEntity entity : getEntities()) {
			entity.destroy();
		}
		__entities.clear();

		if (!__retainedEntities.isEmpty()) {
			throw new ContextStillHasRetainedEntitiesException(this);
		}
	}

	/**
	 * Determines whether the context has the specified entity.
	 * 
	 * @param entity
	 * @return boolean
	 */
	@Override
	public boolean hasEntity(TEntity entity) {
		return __entities.contains(entity);
	}

	/**
	 *
	 * @return Returns all entities which are currently in the context.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public TEntity[] getEntities() {
		if (__entitiesCache == null) {
			__entitiesCache = (TEntity[]) Array.newInstance(__entityType, __entities.size());
			__entities.toArray(__entitiesCache);
		}
		return __entitiesCache;
	}

	/**
	 * The total amount of components an entity can possibly have. This value is
	 * generated by the code generator, e.g ComponentLookup.TotalComponents.
	 * 
	 * @return {@link Integer}
	 */
	@Override
	public int getTotalComponents() {
		return __totalComponents;
	}

	/**
	 * Returns a group for the specified matcher. Calling context.GetGroup(matcher)
	 * with the same matcher will always
	 * 
	 * @param matcher
	 * @return Group return the same instance of the group.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Group<TEntity> getGroup(IMatcher matcher) {
		Group<TEntity> group = __groups.get(matcher);
		if (group == null) {
			group = new Group(matcher, __entityType);
			for (TEntity entity : getEntities()) {
				group.handleEntitySilently(entity);
			}
			__groups.put(matcher, group);

			for (int index : matcher.getIndices()) {
				if (__groupsForIndex[index] == null) {
					__groupsForIndex[index] = EntitasCollections.createList();
				}
				__groupsForIndex[index].add(group);
			}
			__notifyGroupCreated(group);
			return group;
		}
		return group;
	}

	/**
	 * Adds the IEntityIndex for the specified name. There can only be one
	 * IEntityIndex per name.
	 * 
	 * @param entityIndex
	 */
	@Override
	public void addEntityIndex(IEntityIndex entityIndex) {
		if (__entityIndices.containsKey(entityIndex.getName())) {
			throw new ContextEntityIndexDoesAlreadyExistException(this, entityIndex.getName());
		}
		__entityIndices.put(entityIndex.getName(), entityIndex);
	}

	/**
	 * @param name
	 * @return IEntityIndex Gets the IEntityIndex for the specified name.
	 */
	@Override
	public IEntityIndex getEntityIndex(String name) {
		if (!__entityIndices.containsKey(name)) {
			throw new ContextEntityIndexDoesNotExistException(this, name);
		}
		return __entityIndices.get(name);
	}

	/**
	 * Resets the creationIndex back to 0.
	 */
	@Override
	public void resetCreationIndex() {
		__creationIndex = 0;
	}

	/**
	 * Clears the componentPool at the specified index.
	 * 
	 * @param index
	 */
	@Override
	public void clearComponentPool(int index) {
		Stack<IComponent> componentPool = __componentPools[index];
		if (componentPool != null) {
			componentPool.clear();
		}
	}

	/**
	 * Clears all componentPools.
	 */
	@Override
	public void clearComponentPools() {
		for (int i = 0; i < __componentPools.length; i++) {
			clearComponentPool(i);
		}
	}

	/**
	 * Resets the context (destroys all entities and resets creationIndex back to
	 * 0).
	 */
	@Override
	public void reset() {
		destroyAllEntities();
		resetCreationIndex();
		clearEventsListener();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void __updateGroupsComponentAddedOrRemoved(TEntity entity, int index, IComponent component,
			List<Group<TEntity>>[] groupsForIndex) {
		List<Group<TEntity>> groups = groupsForIndex[index];
		if (groups != null) {
			List<Set<GroupChanged>> events = (List<Set<GroupChanged>>) __groupChangedListPool.get();

			for (int i = 0; i < groups.size(); i++) {
				events.add(groups.get(i).handleEntity(entity));
			}

			for (int i = 0; i < events.size(); i++) {
				Set<GroupChanged> groupChangedEvent = events.get(i);
				if (groupChangedEvent != null) {
					for (GroupChanged listener : groupChangedEvent) {
						listener.changed(groups.get(i), entity, index, component);
					}

				}
			}
			__groupChangedListPool.push(events);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void __updateGroupsComponentReplaced(TEntity entity, int index, IComponent previousComponent,
			IComponent newComponent, List<Group<TEntity>>[] groupsForIndex) {
		List<Group<TEntity>> groups = groupsForIndex[index];
		if (groups != null) {
			for (Group g : groups) {
				g.updateEntity(entity, index, previousComponent, newComponent);
			}
		}
	}

	private void __onEntityReleased(TEntity entity) {
		if (entity.isEnabled()) {
			throw new EntityIsNotDestroyedException("Cannot release " + entity);
		}
		entity.removeAllOnEntityReleasedHandlers();
		__retainedEntities.remove(entity);
		__reusableEntities.push(entity);
	}

	private void __onEntityInDestroy(TEntity entity) {
		if (!__entities.remove(entity)) {
			throw new ContextDoesNotContainEntityException("'" + this + "' cannot destroy " + entity + "!",
					"Did you call context.DestroyEntity() on a wrong context?");
		}
		__entitiesCache = null;
		__notifyEntityWillBeDestroyed(entity);

		entity.internalDestroy();

		__notifyEntityDestroyed(entity);

		if (entity.retainCount() == 1) {
			entity.onEntityReleased(__cachedEntityReleased);
			__reusableEntities.push(entity);
			entity.release(this);
			entity.removeAllOnEntityReleasedHandlers();

		} else {
			__retainedEntities.add(entity);
			entity.release(this);
		}
	}

	public List<Group<TEntity>>[] getGroupsForIndex() {
		return __groupsForIndex;
	}

	/**
	 * Returns all componentPools. componentPools is used to reuse removed
	 * components. Removed components will be pushed to the componentPool. Use
	 * entity.CreateComponent(index, type) to get a new or reusable component from
	 * the componentPool.
	 * 
	 * @return Stack<IComponent>[]
	 */
	@Override
	public Stack<IComponent>[] getComponentPools() {
		return __componentPools;
	}

	/**
	 * The contextInfo contains information about the context. It's used to provide
	 * better error messages.
	 * 
	 * @return {@link ContextInfo}
	 */
	@Override
	public ContextInfo getContextInfo() {
		return __contextInfo;
	}

	/**
	 * @return {@link Integer} Returns the number of entities in the context.
	 */
	@Override
	public int getEntitesCount() {
		return __entities.size();
	}

	/**
	 * Returns the number of entities in the internal ObjectPool for entities which
	 * can be reused.
	 * 
	 * @return {@link Integer}
	 */
	@Override
	public int getReusableEntitiesCount() {
		return __reusableEntities.size();
	}

	/**
	 * @return {@link Integer} Returns the number of entities that are currently
	 *         retained by other objects (e.g. <tt>Group, Collector, ReactiveSystem</tt>).
	 */
	@Override
	public int getRetainedEntitiesCount() {
		return __retainedEntities.size();
	}

	@SuppressWarnings("rawtypes")
	public IEntity[] getEntities(IMatcher matcher) {
		return getGroup(matcher).getEntities();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Collector createCollector(IMatcher matcher) {
		return new Collector(getGroup(matcher), GroupEvent.ADDED);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Collector createCollector(IMatcher matcher, GroupEvent groupEvent) {
		return new Collector(getGroup(matcher), groupEvent);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collector<TEntity> createCollector(Context context, TriggerOnEvent<TEntity>[] triggers) {
		Group[] groups = new Group[triggers.length];
		GroupEvent[] groupEvents = new GroupEvent[triggers.length];

		for (int i = 0; i < triggers.length; i++) {
			groups[i] = context.getGroup(triggers[i].matcher);
			groupEvents[i] = triggers[i].groupEvent;
		}
		return new Collector(groups, groupEvents);
	}

	public void clearEventsListener() {
		__onEntityCreateds.clear();
		__onEntityWillBeDestroyeds.clear();
		__onEntityDestroyeds.clear();
		__onGroupCreateds.clear();
	}

	public void onEntityCreated(ContextEntityChanged listener) {
		__onEntityCreateds.add(listener);
	}

	public void onEntityWillBeDestroyed(ContextEntityChanged listener) {
		__onEntityWillBeDestroyeds.add(listener);
	}

	public void onEntityDestroyed(ContextEntityChanged listener) {
		__onEntityDestroyeds.add(listener);
	}

	public void onGroupCreated(ContextGroupChanged listener) {
		__onGroupCreateds.add(listener);
	}

	private void __notifyEntityCreated(IEntity entity) {
		for (ContextEntityChanged listener : __onEntityCreateds) {
			listener.changed(this, entity);
		}
	}

	private void __notifyEntityWillBeDestroyed(IEntity entity) {
		for (ContextEntityChanged listener : __onEntityWillBeDestroyeds) {
			listener.changed(this, entity);
		}
	}

	private void __notifyEntityDestroyed(IEntity entity) {
		for (ContextEntityChanged listener : __onEntityDestroyeds) {
			listener.changed(this, entity);
		}
	}

	@SuppressWarnings("rawtypes")
	private void __notifyGroupCreated(IGroup group) {
		for (ContextGroupChanged listener : __onGroupCreateds) {
			listener.changed(this, group);
		}
	}
	
	public Set<ContextEntityChanged> getOnEntityCreateds() {
		return __onEntityCreateds;
	}
	
	@Override
	public String toString() {
		return "Context{" + "totalComponents=" + __totalComponents + ", entityType=" + __entityType + ", groups="
				+ __groups + ", groupsForIndex=" + Arrays.toString(__groupsForIndex) + ", creationIndex="
				+ __creationIndex + ", entities=" + __entities + ", reusableEntities=" + __reusableEntities
				+ ", retainedEntities=" + __retainedEntities + ", entitiesCache=" + Arrays.toString(__entitiesCache)
				+ ", entityIndices=" + __entityIndices + ", factoryEntiy=" + __factoryEntiy + ", contextInfo="
				+ __contextInfo + ", componentPools=" + Arrays.toString(__componentPools) + ", cachedComponentChanged="
				+ __cachedComponentChanged + '}';
	}

}
