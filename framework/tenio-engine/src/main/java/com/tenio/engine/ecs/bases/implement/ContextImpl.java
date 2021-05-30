/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
package com.tenio.engine.ecs.bases.implement;

import java.util.HashMap;
import java.util.Map;

import com.tenio.common.pool.ElementsPool;
import com.tenio.engine.ecs.bases.Component;
import com.tenio.engine.ecs.bases.Context;
import com.tenio.engine.ecs.bases.Entity;
import com.tenio.engine.ecs.pool.ComponentPool;
import com.tenio.engine.ecs.pool.EntityPool;

/**
 * A context manages the life-cycle of entities and groups. You can create and
 * destroy entities and get groups of entities.
 * 
 * @see Context
 */
public class ContextImpl<EntityExt extends EntityImpl> implements Context<EntityExt> {

	private final Map<String, EntityExt> __entities;
	private final ContextInfo __contextInfo;
	private final ElementsPool<Entity> __entityPool;
	private final ElementsPool<Component>[] __componentPools;

	public ContextImpl(ContextInfo contextInfo, Class<EntityExt> clazz) {
		__contextInfo = contextInfo;
		__entities = new HashMap<String, EntityExt>();
		__entityPool = new EntityPool(clazz, __contextInfo);
		__componentPools = new ComponentPool[getContextInfo().getNumberComponents()];
		for (int i = 0; i < __contextInfo.getNumberComponents(); i++) {
			if (__contextInfo.getComponentTypes()[i] != null) {
				__componentPools[i] = new ComponentPool(__contextInfo.getComponentTypes()[i]);
			}
		}
	}

	@Override
	public EntityExt createEntity() {
		@SuppressWarnings("unchecked")
		EntityExt entity = (EntityExt) __entityPool.get();
		entity.setComponentPools(__componentPools);
		__entities.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public EntityExt getEntity(String entityId) {
		return __entities.get(entityId);
	}

	@Override
	public void destroyEntity(EntityExt entity) {
		entity.reset();
		__entities.remove(entity.getId());
		__entityPool.repay(entity);
	}

	@Override
	public boolean hasEntity(EntityExt entity) {
		return __entities.containsKey(entity.getId());
	}

	@Override
	public Map<String, EntityExt> getEntities() {
		return __entities;
	}

	@Override
	public ContextInfo getContextInfo() {
		return __contextInfo;
	}

	@Override
	public int getEntitesCount() {
		return __entities.size();
	}

	@Override
	public void destroyAllEntities() {
		__entities.values().forEach(entity -> {
			entity.reset();
		});
		__entities.clear();
	}

	@Override
	public void reset() {
		destroyAllEntities();
		__entityPool.cleanup();
		for (var componentPool : __componentPools) {
			if (componentPool != null) {
				componentPool.cleanup();
				componentPool = null;
			}
		}
	}

}
