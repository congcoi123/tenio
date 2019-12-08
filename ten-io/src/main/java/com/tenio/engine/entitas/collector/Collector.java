package com.tenio.engine.entitas.collector;

import java.util.Arrays;
import java.util.Set;

import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.api.IGroup;
import com.tenio.engine.entitas.api.entitas.IEntity;
import com.tenio.engine.entitas.api.events.GroupChanged;
import com.tenio.engine.entitas.exceptions.CollectorException;
import com.tenio.engine.entitas.factories.EntitasCollections;
import com.tenio.engine.entitas.group.Group;
import com.tenio.engine.entitas.group.GroupEvent;

/**
 * <p>
 * A Collector can observe one or more groups from the same context and collects
 * <br>
 * changed entities based on the specified groupEvent.
 * </p>
 * 
 * @author Rubentxu
 */
@SuppressWarnings("rawtypes")
public class Collector<TEntity extends IEntity> implements ICollector {

	private Set<TEntity> __collectedEntities;
	private IGroup<TEntity>[] __groups;
	private GroupEvent[] __groupEvents;
	private GroupChanged<TEntity> __addEntityCache;

	/**
	 * Creates a Collector and will collect changed entities based on the specified
	 * groupEvents.
	 * 
	 * @param groups
	 * @param groupEvents
	 */
	public Collector(IGroup<TEntity>[] groups, GroupEvent[] groupEvents) {
		__groups = groups;
		__groupEvents = groupEvents;
		__collectedEntities = EntitasCollections.createSet();

		if (groups.length != groupEvents.length) {
			throw new CollectorException("Unbalanced count with groups (" + groups.length + ") and group events ("
					+ groupEvents.length + ").", "Group and group event count must be equal.");
		}

		__addEntityCache = (IGroup<TEntity> group, TEntity entity, int index, IComponent component) -> {
			__addEntity(group, entity, index, component);
		};
		activate();
	}

	/**
	 * Creates a Collector and will collect changed entities based on the specified
	 * groupEvent.
	 * 
	 * @param group
	 * @param eventType
	 */
	@SuppressWarnings("unchecked")
	public Collector(IGroup<TEntity> group, GroupEvent eventType) {
		this(new IGroup[] { group }, new GroupEvent[] { eventType });
	}

	/**
	 * Returns all collected entities. Call collector.clearCollectedEntities() once
	 * you processed all entities.
	 *
	 * @returns Set<TEntity>
	 */
	@Override
	public Set getCollectedEntities() {
		return __collectedEntities;
	}

	/**
	 * @return Returns the number of all collected entities.
	 */
	@Override
	public int getCount() {
		return __collectedEntities.size();
	}

	/**
	 * Activates the Collector and will start collecting changed entities.
	 * Collectors are activated by default.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void activate() {
		for (int i = 0; i < __groups.length; i++) {
			Group group = (Group) __groups[i];
			GroupEvent groupEvent = __groupEvents[i];
			switch (groupEvent) {
			case ADDED:
				group.onEntityAdded(__addEntityCache);
				break;
			case REMOVED:
				group.onEntityRemoved(__addEntityCache);
				break;
			case ADDED_OR_REMOVED:
				group.onEntityAdded(__addEntityCache);
				group.onEntityRemoved(__addEntityCache);
				break;
			}
		}
	}

	/**
	 * Deactivates the Collector. This will also clear all collected entities.
	 * Collectors are activated by default.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void deactivate() {
		for (int i = 0; i < __groups.length; i++) {
			Group group = (Group) __groups[i];
			group.removeEntityAddedEvent(__addEntityCache);
			group.removeEntityRemovedEvent(__addEntityCache);
		}
		clearCollectedEntities();
	}

	/**
	 * Clears all collected entities.
	 */
	@Override
	public void clearCollectedEntities() {
		for (IEntity entity : __collectedEntities) {
			entity.release(this);
		}
		__collectedEntities.clear();
	}

	private void __addEntity(IGroup<TEntity> group, TEntity entity, int index, IComponent component) {
		boolean added = __collectedEntities.add(entity);
		if (added) {
			entity.retain(this);
		}
	}

	@Override
	public String toString() {
		return "Collector{" + "collectedEntities=" + __collectedEntities + ", groups=" + Arrays.toString(__groups)
				+ ", groupEvents=" + Arrays.toString(__groupEvents) + ", addEntityCache=" + __addEntityCache + '}';
	}

}
