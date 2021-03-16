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
package com.tenio.engine.ecs.pool;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import javax.annotation.concurrent.GuardedBy;

import com.tenio.common.configuration.constant.CommonConstants;
import com.tenio.common.exception.NullElementPoolException;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.pool.IElementPool;
import com.tenio.engine.ecs.base.ContextInfo;
import com.tenio.engine.ecs.base.Entity;
import com.tenio.engine.ecs.base.IEntity;

/**
 * The object pool mechanism for {@link IEntity}.
 * 
 * @author kong
 * 
 */
public final class EntityPool extends AbstractLogger implements IElementPool<IEntity> {

	@GuardedBy("this")
	private IEntity[] __pool;
	@GuardedBy("this")
	private boolean[] __used;
	private Class<? extends Entity> __clazz;
	private ContextInfo __contextInfo;

	public EntityPool(Class<? extends Entity> clazz, ContextInfo contextInfo) {
		__clazz = clazz;
		__contextInfo = contextInfo;
		__pool = new IEntity[CommonConstants.BASE_ELEMENT_POOL];
		__used = new boolean[CommonConstants.BASE_ELEMENT_POOL];

		for (int i = 0; i < __pool.length; i++) {
			try {
				var entity = __clazz.getDeclaredConstructor().newInstance();
				entity.setId(UUID.randomUUID().toString());
				entity.setContextInfo(__contextInfo);
				__pool[i] = entity;
				__used[i] = false;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				_error(e);
			}
		}
	}

	@Override
	public synchronized IEntity get() {
		for (int i = 0; i < __used.length; i++) {
			if (!__used[i]) {
				__used[i] = true;
				return __pool[i];
			}
		}
		// If we got here, then all the Elements are in use. We will
		// increase the number in our pool by @ADD_ELEMENT_POOL (arbitrary value for
		// illustration purposes).
		var oldUsed = __used;
		__used = new boolean[oldUsed.length + CommonConstants.ADD_ELEMENT_POOL];
		System.arraycopy(oldUsed, 0, __used, 0, oldUsed.length);

		var oldPool = __pool;
		__pool = new IEntity[oldPool.length + CommonConstants.ADD_ELEMENT_POOL];
		System.arraycopy(oldPool, 0, __pool, 0, oldPool.length);

		for (int i = oldPool.length; i < __pool.length; i++) {
			try {
				var entity = __clazz.getDeclaredConstructor().newInstance();
				entity.setId(UUID.randomUUID().toString());
				entity.setContextInfo(__contextInfo);
				__pool[i] = entity;
				__used[i] = false;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				_error(e);
			}
		}

		_info("COMPONENT POOL",
				_buildgen("Increase the number of elements by ", CommonConstants.ADD_ELEMENT_POOL, " to ", __used.length));

		// and allocate the last old ELement
		__used[oldPool.length - 1] = true;
		return __pool[oldPool.length - 1];
	}

	@Override
	public synchronized void repay(IEntity element) {
		boolean flagFound = false;
		for (int i = 0; i < __pool.length; i++) {
			if (__pool[i] == element) {
				__used[i] = false;
				element.reset();
				flagFound = true;
				break;
			}
		}
		if (!flagFound) {
			var e = new NullElementPoolException();
			_error(e);
			throw e;
		}
	}

	@Override
	public synchronized void cleanup() {
		for (int i = 0; i < __pool.length; i++) {
			__pool[i] = null;
		}
		__used = null;
		__pool = null;
	}

	@Override
	public synchronized int getPoolSize() {
		return (__pool.length == __used.length) ? __pool.length : -1;
	}

}
