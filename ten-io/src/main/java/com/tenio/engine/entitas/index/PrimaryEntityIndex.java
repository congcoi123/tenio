package com.tenio.engine.entitas.index;

import java.util.Map;

import com.tenio.engine.entitas.Entity;
import com.tenio.engine.entitas.SafeAERC;
import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.api.IGroup;
import com.tenio.engine.entitas.exceptions.EntityIndexException;
import com.tenio.engine.entitas.factories.EntitasCollections;

/**
 * <p>
 * <tt>PrimaryEntityIndex</tt> makes sure that every key is associated with only one
 * <br>
 * entity. This is very good if you have an <tt>Id</tt> component and you want to look
 * <br>
 * up entities by this <tt>Id</tt>. This is also what we recommend, when you need to
 * <br>
 * store a reference from one entity to another (more on it in ingredience
 * <br>
 * chapter).
 * </p>
 * 
 * @author Rubentxu
 */
public class PrimaryEntityIndex<TEntity extends Entity, TKey> extends AbstractEntityIndex<TEntity, TKey> {

	private Map<TKey, TEntity> __indexes;

	public PrimaryEntityIndex(String name, FuncKey<TEntity, IComponent, TKey> key, IGroup<TEntity> group) {
		super(name, key, group);
		__indexes = EntitasCollections.createMap();
		activate();
	}

	public PrimaryEntityIndex(String name, IGroup<TEntity> group, FuncKey<TEntity, IComponent, TKey[]> keys) {
		super(name, group, keys);
		__indexes = EntitasCollections.createMap();
		activate();
	}

	public TEntity getEntity(TKey key) {
		TEntity entity = __indexes.get(key);
		if (entity == null) {
			throw new EntityIndexException("Entity for key '" + key + "' doesn't exist!",
					"You should check if an entity with that key exists before getting it.");
		}
		return entity;

	}

	@Override
	public void activate() {
		super.activate();
		_indexEntities();
	}

	@Override
	public void clear() {
		for (TEntity entity : __indexes.values()) {
			SafeAERC safeAerc = (SafeAERC) entity.getAERC();
			if (safeAerc != null) {
				if (safeAerc.getOwners().contains(this)) {
					entity.release(this);
				}
			} else {
				entity.release(this);
			}
		}
		__indexes.clear();
	}

	@Override
	public void addEntity(TKey key, TEntity entity) {
		if (__indexes.containsKey(key)) {
			throw new EntityIndexException("Entity for key '" + key + "' already exists!",
					"Only one entity for a primary key is allowed.");
		}
		__indexes.put(key, entity);
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
		__indexes.remove(key);
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
