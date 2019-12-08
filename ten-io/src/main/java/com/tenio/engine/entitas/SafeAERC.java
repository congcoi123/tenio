package com.tenio.engine.entitas;

import java.util.Set;

import com.tenio.engine.entitas.api.entitas.IAERC;
import com.tenio.engine.entitas.api.entitas.IEntity;
import com.tenio.engine.entitas.exceptions.EntityIsAlreadyRetainedByOwnerException;
import com.tenio.engine.entitas.exceptions.EntityIsNotRetainedByOwnerException;
import com.tenio.engine.entitas.factories.EntitasCollections;

/**
 * <p>
 * Automatic Entity Reference Counting (AERC) is used internally to prevent
 * <br>
 * pooling retained entities. If you use retain manually you also have to
 * <br>
 * release it manually at some point. SafeAERC checks if the entity has already
 * <br>
 * been retained or released. It's slower, but you keep the information about
 * <br>
 * the owners.
 * </p>
 * 
 * @author Rubentxu
 */
public class SafeAERC implements IAERC {
	private IEntity __entity;
	private Set<Object> __owners;

	public SafeAERC(IEntity entity) {
		__entity = entity;
		__owners = EntitasCollections.createSet();
	}

	public Set<Object> getOwners() {
		return __owners;
	}
	
	@Override
	public int retainCount() {
		return __owners.size();
	}

	@Override
	public void retain(Object owner) {
		if (!__owners.add(owner)) {
			throw new EntityIsAlreadyRetainedByOwnerException(__entity, owner);
		}
	}

	@Override
	public void release(Object owner) {
		if (!__owners.remove(owner)) {
			throw new EntityIsNotRetainedByOwnerException(__entity, owner);
		}
	}

}
