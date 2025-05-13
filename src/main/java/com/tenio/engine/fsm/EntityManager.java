/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.engine.fsm;

import com.tenio.common.logger.SystemLogger;
import com.tenio.engine.exception.DuplicatedEntityException;
import com.tenio.engine.fsm.entity.AbstractEntity;
import java.util.HashMap;
import java.util.Map;

/**
 * This class for managing entities.
 */
public final class EntityManager extends SystemLogger {

  /**
   * The list of entities.
   */
  private final Map<String, AbstractEntity> entities = new HashMap<>();

  /**
   * Register an entity to this management.
   *
   * @param entity the desired entity, see {@link AbstractEntity}
   */
  public void register(AbstractEntity entity) {
    try {
      if (contain(entity.getId())) {
        throw new DuplicatedEntityException();
      }
    } catch (DuplicatedEntityException exception) {
      // fire an event
      error(exception, "entity id: ", entity.getId());
      return;
    }

    entities.put(entity.getId(), entity);
  }

  public boolean contain(String id) {
    return entities.containsKey(id);
  }

  public long count() {
    return entities.size();
  }

  public AbstractEntity get(String id) {
    return entities.get(id);
  }

  /**
   * Need to call update every frame.
   *
   * @param deltaTime the time between two consecutive frames
   */
  public void update(float deltaTime) {
    for (var entity : entities.values()) {
      entity.update(deltaTime);
    }
  }

  /**
   * Retrieves the list of entities.
   *
   * @return a map of entities in this manager
   */
  public Map<String, AbstractEntity> gets() {
    return entities;
  }

  public void remove(String id) {
    entities.remove(id);
  }

  public void clear() {
    entities.clear();
  }
}
