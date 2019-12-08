package com.tenio.engine.entitas.group;

import java.util.Iterator;
import java.util.Set;

import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.api.IGroup;
import com.tenio.engine.entitas.api.entitas.IEntity;
import com.tenio.engine.entitas.api.events.GroupChanged;
import com.tenio.engine.entitas.api.events.GroupUpdated;
import com.tenio.engine.entitas.api.matcher.IMatcher;
import com.tenio.engine.entitas.collector.Collector;
import com.tenio.engine.entitas.collector.ICollector;
import com.tenio.engine.entitas.exceptions.GroupSingleEntityException;
import com.tenio.engine.entitas.factories.EntitasCollections;

/**
 * @author Rubentxu
 */
public class Group<TEntity extends IEntity> implements IGroup<TEntity> {

	@SuppressWarnings("rawtypes")
	private Set<GroupChanged> __onEntityAddeds = EntitasCollections.createSet();
	@SuppressWarnings("rawtypes")
	private Set<GroupUpdated> __onEntityUpdateds = EntitasCollections.createSet();
	@SuppressWarnings("rawtypes")
	private Set<GroupChanged> __onEntityRemoveds = EntitasCollections.createSet();
	
	private Class<TEntity> __clazz;
	private IMatcher<TEntity> __matcher;
	private Set<TEntity> __entities;
	private TEntity[] __entitiesCache;
	private TEntity __singleEntityCache;

	public Group(IMatcher<TEntity> matcher, Class<TEntity> clazz) {
		__entities = EntitasCollections.createSet();
		__matcher = matcher;
		__clazz = clazz;
	}

	@SuppressWarnings("unchecked")
	public ICollector<TEntity> createCollector(GroupEvent groupEvent) {
		return new Collector<TEntity>(this, groupEvent);
	}

	@Override
	public int getCountEntities() {
		return __entities.size();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IMatcher getMatcher() {
		return __matcher;
	}
	
	@Override
	public void handleEntitySilently(TEntity entity) {
		if (__matcher.matches(entity)) {
			__addEntitySilently(entity);
		} else {
			__removeEntitySilently(entity);
		}
	}

	@Override
	public void handleEntity(TEntity entity, int index, IComponent component) {
		if (__matcher.matches(entity)) {
			__addEntity(entity, index, component);
		} else {
			__removeEntity(entity, index, component);
		}

	}

	@Override
	public void updateEntity(TEntity entity, int index, IComponent previousComponent, IComponent newComponent) {
		if (__entities.contains(entity)) {
			__notifyOnEntityRemoved(entity, index, previousComponent);
			__notifyOnEntityAdded(entity, index, previousComponent);
			__notifyOnEntityUpdated(entity, index, previousComponent, newComponent);
		}
	}

	@Override
	public void removeAllEventHandlers() {
		__onEntityAddeds.clear();
		__onEntityRemoveds.clear();
		__onEntityUpdateds.clear();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Set<GroupChanged> handleEntity(TEntity entity) {
		return (__matcher.matches(entity)) ? (__addEntitySilently(entity)) ? __onEntityAddeds : null
				: (__removeEntitySilently(entity)) ? __onEntityRemoveds : null;
	}

	private boolean __addEntitySilently(TEntity entity) {
		if (entity.isEnabled()) {
			boolean added = __entities.add(entity);
			if (added) {
				__entitiesCache = null;
				__singleEntityCache = null;
				entity.retain(this);
			}
			return added;
		}
		return false;
	}

	private void __addEntity(TEntity entity, int index, IComponent component) {
		if (__addEntitySilently(entity)) {
			__notifyOnEntityAdded(entity, index, component);
		}
	}

	private boolean __removeEntitySilently(TEntity entity) {
		boolean removed = __entities.remove(entity);
		if (removed) {
			__entitiesCache = null;
			__singleEntityCache = null;
			entity.release(this);
		}
		return removed;
	}

	private void __removeEntity(TEntity entity, int index, IComponent component) {
		boolean removed = __entities.remove(entity);
		if (removed) {
			__entitiesCache = null;
			__singleEntityCache = null;
			__notifyOnEntityRemoved(entity, index, component);
			entity.release(this);
		}
	}

	@Override
	public boolean containsEntity(TEntity entity) {
		return __entities.contains(entity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public TEntity[] getEntities() {
		if (__entitiesCache == null) {
			__entitiesCache = (TEntity[]) java.lang.reflect.Array.newInstance(__clazz, __entities.size());
			int i = 0;
			for (TEntity entity : __entities) {
				__entitiesCache[i] = entity;
				i++;
			}
		}
		return __entitiesCache;
	}

	@Override
	public TEntity getSingleEntity() {
		if (__singleEntityCache == null) {
			int c = __entities.size();
			if (c == 1) {
				Iterator<TEntity> enumerator = __entities.iterator();
				__singleEntityCache = enumerator.next();
			} else if (c == 0) {
				return null;
			} else {
				throw new GroupSingleEntityException(this);
			}
		}
		return __singleEntityCache;
	}

	public void onEntityAdded(GroupChanged<TEntity> listener) {
		__onEntityAddeds.add(listener);
	}

	public void onEntityUpdated(GroupUpdated<TEntity> listener) {
		__onEntityUpdateds.add(listener);
	}

	public void onEntityRemoved(GroupChanged<TEntity> listener) {
		__onEntityRemoveds.add(listener);
	}
	
	public void removeEntityAddedEvent(GroupChanged<TEntity> listener) {
		__onEntityAddeds.remove(listener);
	}
	
	public void removeEntityRemovedEvent(GroupChanged<TEntity> listener) {
		__onEntityRemoveds.remove(listener);
	}

	@SuppressWarnings("unchecked")
	private void __notifyOnEntityAdded(TEntity entity, int index, IComponent component) {
		for (GroupChanged<TEntity> listener : __onEntityAddeds) {
			listener.changed(this, entity, index, component);
		}
	}

	@SuppressWarnings("unchecked")
	private void __notifyOnEntityUpdated(TEntity entity, int index, IComponent component, IComponent newComponent) {
		for (GroupUpdated<TEntity> listener : __onEntityUpdateds) {
			listener.updated(this, entity, index, component, newComponent);
		}
	}

	@SuppressWarnings("unchecked")
	private void __notifyOnEntityRemoved(TEntity entity, int index, IComponent component) {
		for (GroupChanged<TEntity> listener : __onEntityRemoveds) {
			listener.changed(this, entity, index, component);
		}
	}
	
	@Override
	public String toString() {
		return "Group{" + "matcher=" + __matcher + ", entities=" + __entities + ", singleEntityCache="
				+ __singleEntityCache + ", clazz=" + __clazz + '}';
	}

}
