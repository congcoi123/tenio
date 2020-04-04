/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.engine.ecs;

import java.util.ArrayList;
import java.util.List;

import com.tenio.engine.ecs.common.IContext;
import com.tenio.engine.ecs.pool.EntityPool;

/**
 * A context manages the life-cycle of entities and groups. You can create and
 * destroy entities and get groups of entities.
 * 
 * @author Kong
 */
public class Context<TEntity extends Entity> implements IContext<TEntity> {
	
	private final List<TEntity> __entities;
	private final ContextInfo __contextInfo;
	private final EntityPool __entityPool;
	
	public Context(ContextInfo contextInfo, Class<TEntity> clazz) {
		__contextInfo = contextInfo;
		__entities = new ArrayList<TEntity>();
		__entityPool = new EntityPool(clazz, __contextInfo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public TEntity createEntity() {
		var entity = (TEntity) __entityPool.get();
		__entities.add(entity);
		return entity;
	}

	@Override
	public void destroyEntity(TEntity entity) {
		entity.reset();
		__entities.remove(entity);
		__entityPool.repay(entity);
	}

	@Override
	public boolean hasEntity(TEntity entity) {
		return __entities.contains(entity);
	}

	@Override
	public List<TEntity> getEntities() {
		return __entities;
	}
	
	@Override
	public EntityPool getEntityPool() {
		return __entityPool;
	}

	@Override
	public int getTotalComponents() {
		return 0;
	}

	@Override
	public int getEntitesCount() {
		return __entities.size();
	}

	@Override
	public void destroyAllEntities() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return "Context{" + "}";
	}

}
