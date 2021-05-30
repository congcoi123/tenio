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
package com.tenio.engine.ecs.bases;

import java.util.Map;

import com.tenio.engine.ecs.bases.implement.ContextInfo;

/**
 * A context is used to manage all relative entities and components.
 * 
 * @param <TEntity> the entity template
 */
public interface Context<TEntity extends Entity> {

	/**
	 * Create new entity
	 * 
	 * @return the entity by the corresponding entity template
	 */
	TEntity createEntity();

	/**
	 * Retrieves an entity by entity id
	 * 
	 * @param entityId the entity id
	 * @return the corresponding entity
	 */
	TEntity getEntity(String entityId);

	/**
	 * Check if the entity is existed by entity id
	 * 
	 * @param entity the entity
	 * @return <b>true</b> if this entity is existed in the current context,
	 *         <b>false</b> otherwise
	 */
	boolean hasEntity(TEntity entity);

	/**
	 * Remove this entity from the current context
	 * 
	 * @param entity the corresponding entity
	 */
	void destroyEntity(TEntity entity);

	/**
	 * Retrieves all entities of the current context
	 * 
	 * @return the map of entities
	 */
	Map<String, TEntity> getEntities();

	/**
	 * Retrieves the context information
	 * 
	 * @return see {@link ContextInfo}
	 */
	ContextInfo getContextInfo();

	/**
	 * Retrieves the number of entities
	 * 
	 * @return the entities count
	 */
	int getEntitesCount();

	/**
	 * Remove all context's entities
	 */
	void destroyAllEntities();

	/**
	 * Reset the current context
	 */
	void reset();

}
