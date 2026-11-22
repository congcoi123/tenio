/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.engine.ecs.basis.implement;

import com.tenio.common.pool.ElementPool;
import com.tenio.engine.ecs.basis.Component;
import com.tenio.engine.ecs.basis.Context;
import com.tenio.engine.ecs.basis.Entity;
import com.tenio.engine.ecs.pool.ComponentPool;
import com.tenio.engine.ecs.pool.EntityPool;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A context manages the life-cycle of entities and groups. You can create and
 * destroy entities and get groups of entities.
 *
 * @see Context
 */
public class ContextImpl<T extends EntityImpl> implements Context<T> {

  private final Map<String, T> entities;
  private final ContextInfo contextInfo;
  private final ElementPool<Entity> entityPool;
  private final ElementPool<Component>[] componentPools;

  /**
   * Initialization.
   *
   * @param contextInfo the context information
   * @param clazz       the class of element
   */
  public ContextImpl(ContextInfo contextInfo, Class<T> clazz) {
    this.contextInfo = contextInfo;
    entities = new LinkedHashMap<>();
    entityPool = new EntityPool(clazz, this.contextInfo);
    componentPools = new ComponentPool[getContextInfo().getNumberComponents()];

    for (int i = 0; i < this.contextInfo.getNumberComponents(); i++) {
      if (this.contextInfo.getComponentTypes()[i] != null) {
        componentPools[i] = new ComponentPool(this.contextInfo.getComponentTypes()[i]);
      }
    }
  }

  @Override
  public synchronized T createEntity() {
    @SuppressWarnings("unchecked")
    var entity = (T) entityPool.get();
    entity.setComponentPools(componentPools);
    entities.put(entity.getId(), entity);
    return entity;
  }

  @Override
  public synchronized T getEntity(String entityId) {
    return entities.get(entityId);
  }

  @Override
  public synchronized void destroyEntity(T entity) {
    Objects.requireNonNull(entity, "entity");
    if (entities.get(entity.getId()) != entity) {
      return;
    }
    entities.remove(entity.getId());
    reclaimComponents(entity);
    entityPool.repay(entity);
  }

  @Override
  public synchronized boolean hasEntity(T entity) {
    return entity != null && entities.get(entity.getId()) == entity;
  }

  @Override
  public synchronized Map<String, T> getEntities() {
    return Map.copyOf(entities);
  }

  @Override
  public ContextInfo getContextInfo() {
    return contextInfo;
  }

  @Override
  public synchronized int getEntitiesCount() {
    return entities.size();
  }

  @Override
  public synchronized void destroyAllEntities() {
    for (var entity : java.util.List.copyOf(entities.values())) {
      destroyEntity(entity);
    }
  }

  @Override
  public synchronized void reset() {
    destroyAllEntities();
    entityPool.cleanup();
    for (var componentPool : componentPools) {
      if (componentPool != null) {
        componentPool.cleanup();
      }
    }
  }

  private void reclaimComponents(T entity) {
    var components = entity.getComponents();
    for (int i = 0; i < components.length; i++) {
      if (components[i] != null && componentPools[i] != null) {
        if (!(componentPools[i] instanceof ComponentPool pool)
            || pool.isBorrowed(components[i])) {
          componentPools[i].repay(components[i]);
        }
        entity.replaceComponent(i, null);
      }
    }
  }
}
