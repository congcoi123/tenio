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

/**
 * A context manages the life-cycle of entities and groups. You can create and
 * destroy entities and get groups of entities.
 * 
 * @author Kong
 */
public class Context<TEntity extends Entity> implements IContext<TEntity> {
	
	private final List<TEntity> __entities;
	private final ContextInfo __contextInfo;
	
	public Context(ContextInfo contextInfo) {
		__contextInfo = contextInfo;
		__entities = new ArrayList<TEntity>();
	}

	@Override
	public TEntity createEntity() {
		@SuppressWarnings("unchecked")
		TEntity entity = (TEntity) new Entity(__contextInfo);
		__entities.add(entity);
		return entity;
	}

	@Override
	public void destroyEntity(TEntity entity) {
		
	}

	@Override
	public boolean hasEntity(TEntity entity) {
		return __entities.contains(entity);
	}

	@Override
	public TEntity[] getEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTotalComponents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEntitesCount() {
		// TODO Auto-generated method stub
		return 0;
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
