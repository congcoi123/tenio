package com.tenio.engine.entitas.index;

import com.tenio.engine.entitas.Entity;
import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.api.IGroup;
import com.tenio.engine.entitas.api.entitas.IEntityIndex;
import com.tenio.engine.entitas.api.events.GroupChanged;
import com.tenio.engine.entitas.group.Group;

/**
 * <p>
 * When we want to get all entities that have a position component, we create a
 * <br>
 * group and iterate over it. However, what about cases, where we want to get
 * <br>
 * entities on a certain position. We could iterate over all entites which have
 * <br>
 * a position and collect only those which have desired position. Or we could
 * <br>
 * use an Index.
 * </p>
 * 
 * @author Rubentxu
 */
public abstract class AbstractEntityIndex<TEntity extends Entity, TKey> implements IEntityIndex {

	private Group<TEntity> __group;
	private FuncKey<TEntity, IComponent, TKey> __key;
	private FuncKey<TEntity, IComponent, TKey[]> __keys;
	private String __name;
	private boolean __singleKey;
	
	private GroupChanged<TEntity> __onEntityAdded = (IGroup<TEntity> group, TEntity entity, int index,
			IComponent component) -> {
		if (__singleKey) {
			addEntity(__key.getKey(entity, component), entity);
		} else {
			TKey[] keys = __keys.getKey(entity, component);
			for (int i = 0; i < keys.length; i++) {
				addEntity(keys[i], entity);
			}
		}
	};

	private GroupChanged<TEntity> __onEntityRemoved = (IGroup<TEntity> group, TEntity entity, int index,
			IComponent component) -> {
		if (__singleKey) {
			removeEntity(__key.getKey(entity, component), entity);
		} else {
			TKey[] keys = __keys.getKey(entity, component);
			for (int i = 0; i < keys.length; i++) {
				removeEntity(keys[i], entity);
			}
		}
	};

	public AbstractEntityIndex(String name, FuncKey<TEntity, IComponent, TKey> key, IGroup<TEntity> group) {
		__name = name;
		__group = (Group<TEntity>) group;
		__key = key;
		__singleKey = true;

	}

	public AbstractEntityIndex(String name, IGroup<TEntity> group, FuncKey<TEntity, IComponent, TKey[]> keys) {
		__name = name;
		__group = (Group<TEntity>) group;
		__keys = keys;
		__singleKey = false;
	}

	protected void _indexEntities() {
		TEntity[] entities = __group.getEntities();
		for (int i = 0; i < entities.length; i++) {
			TEntity entity = entities[i];
			if (__singleKey) {
				addEntity(__key.getKey(entity, null), entity);
			} else {
				for (TKey k : __keys.getKey(entity, null)) {
					addEntity(k, entity);
				}
			}
		}
	}

	@Override
	public String getName() {
		return __name;
	}

	@Override
	public void activate() {
		__group.onEntityAdded(__onEntityAdded);
		__group.onEntityRemoved(__onEntityRemoved);
	}

	@Override
	public void deactivate() {
		__group.removeEntityAddedEvent(__onEntityAdded);
		__group.removeEntityRemovedEvent(__onEntityRemoved);
		clear();
	}

	@Override
	public String toString() {
		return __name;
	}

	public abstract void addEntity(TKey key, TEntity entity);

	public abstract void removeEntity(TKey key, TEntity entity);

	public abstract void clear();

}
