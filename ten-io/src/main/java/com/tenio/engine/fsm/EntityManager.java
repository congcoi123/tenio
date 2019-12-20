/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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

import java.util.HashMap;
import java.util.Map;

import com.tenio.engine.fsm.entities.AbstractEntity;
import com.tenio.exception.DuplicatedEntityException;
import com.tenio.logger.AbstractLogger;

/**
 * This class for managing entities.
 * 
 * @author kong
 * 
 */
public final class EntityManager extends AbstractLogger {

	/**
	 * The list of entities
	 */
	private Map<Integer, AbstractEntity> __entities = new HashMap<Integer, AbstractEntity>();

	/**
	 * Register an entity to this management
	 * 
	 * @param entity the desired entity @see {@link AbstractEntity}
	 */
	public void register(AbstractEntity entity) {
		try {
			if (contain(entity.getId())) {
				throw new DuplicatedEntityException();
			}
		} catch (DuplicatedEntityException e) {
			// fire an event
			error("REGISTER ENTITY", String.valueOf(entity.getId()), e);
			return;
		}

		__entities.put(entity.getId(), entity);
	}

	public boolean contain(int id) {
		return __entities.containsKey(id);
	}

	public long count() {
		return __entities.size();
	}

	public AbstractEntity get(int id) {
		return __entities.get(id);
	}

	/**
	 * Need to call update every frame
	 * 
	 * @param delta the time between two consecutive frames
	 */
	public void update(float delta) {
		for (AbstractEntity entity : __entities.values()) {
			entity.update(delta);
		}
	}

	/**
	 * @return Returns the list of entities in this manager
	 */
	public Map<Integer, AbstractEntity> gets() {
		return __entities;
	}

	public void remove(int id) {
		__entities.remove(id);
	}

	public void clear() {
		__entities.clear();
	}

}
