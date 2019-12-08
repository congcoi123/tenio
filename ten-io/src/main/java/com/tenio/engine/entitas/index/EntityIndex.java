package com.tenio.engine.entitas.index;

import java.util.Map;
import java.util.Set;

import com.tenio.engine.entitas.Entity;
import com.tenio.engine.entitas.SafeAERC;
import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.api.IGroup;
import com.tenio.engine.entitas.api.entitas.IEntity;
import com.tenio.engine.entitas.factories.EntitasCollections;

/**
 * <p>
 * An <tt>EntityIndex</tt> is backed by a HashMap which stores a set of entities
 * <br>
 * as value. Meaning - you could have multiple entities on the same position.
 * </p>
 * 
 * @author Rubentxu
 */
public class EntityIndex<TEntity extends Entity, TKey> extends AbstractEntityIndex<TEntity, TKey> {

	private Map<TKey, Set<TEntity>> __indexes;

	public EntityIndex(String name, FuncKey<TEntity, IComponent, TKey> key, IGroup<TEntity> group) {
		super(name, key, group);
		__indexes = EntitasCollections.createMap();
		activate();
	}

	public EntityIndex(String name, IGroup<TEntity> group, FuncKey<TEntity, IComponent, TKey[]> keys) {
		super(name, group, keys);
		__indexes = EntitasCollections.createMap();
		activate();
	}

	@Override
	public void activate() {
		super.activate();
		_indexEntities();
	}

	public Set<TEntity> getEntities(TKey key) {
		if (!__indexes.containsKey(key)) {
			Set<TEntity> entities = EntitasCollections.createSet();
			__indexes.put(key, entities);
			return entities;
		}
		return __indexes.get(key);

	}

	@Override
	public void clear() {
		for (Set<TEntity> entities : __indexes.values()) {
			for (IEntity entity : entities) {
				SafeAERC safeAerc = (SafeAERC) entity.getAERC();
				if (safeAerc != null) {
					if (safeAerc.getOwners().contains(this)) {
						entity.release(this);
					}
				} else {
					entity.release(this);
				}
			}
		}
		__indexes.clear();

	}

	@Override
	public void addEntity(TKey key, TEntity entity) {
		getEntities(key).add(entity);
		SafeAERC safeAerc = (SafeAERC) entity.getAERC();
		if (safeAerc != null) {
			if (!safeAerc.getOwners().contains(this)) {
				entity.retain(this);
			}
		} else {
			entity.retain(this);
		}
	}

	@Override
	public void removeEntity(TKey key, TEntity entity) {
		getEntities(key).remove(entity);
		SafeAERC safeAerc = (SafeAERC) entity.getAERC();
		if (safeAerc != null) {
			if (safeAerc.getOwners().contains(this)) {
				entity.release(this);
			}
		} else {
			entity.release(this);
		}
	}

}
