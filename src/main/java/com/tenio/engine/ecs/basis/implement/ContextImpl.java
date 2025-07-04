/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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
import java.util.HashMap;
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
    entities = new HashMap<>();
    entityPool = new EntityPool(clazz, this.contextInfo);
    componentPools = new ComponentPool[getContextInfo().getNumberComponents()];

    for (int i = 0; i < this.contextInfo.getNumberComponents(); i++) {
      if (Objects.nonNull(this.contextInfo.getComponentTypes()[i])) {
        componentPools[i] = new ComponentPool(this.contextInfo.getComponentTypes()[i]);
      }
    }
  }

  @Override
  public T createEntity() {
    @SuppressWarnings("unchecked")
    var entity = (T) entityPool.get();
    entity.setComponentPools(componentPools);
    entities.put(entity.getId(), entity);
    return entity;
  }

  @Override
  public T getEntity(String entityId) {
    return entities.get(entityId);
  }

  @Override
  public void destroyEntity(T entity) {
    entity.reset();
    entities.remove(entity.getId());
    entityPool.repay(entity);
  }

  @Override
  public boolean hasEntity(T entity) {
    return entities.containsKey(entity.getId());
  }

  @Override
  public Map<String, T> getEntities() {
    return entities;
  }

  @Override
  public ContextInfo getContextInfo() {
    return contextInfo;
  }

  @Override
  public int getEntitiesCount() {
    return entities.size();
  }

  @Override
  public void destroyAllEntities() {
    entities.values().forEach(EntityImpl::reset);
    entities.clear();
  }

  @Override
  public void reset() {
    destroyAllEntities();
    entityPool.cleanup();
    for (var componentPool : componentPools) {
      if (Objects.nonNull(componentPool)) {
        componentPool.cleanup();
      }
    }
  }
}
