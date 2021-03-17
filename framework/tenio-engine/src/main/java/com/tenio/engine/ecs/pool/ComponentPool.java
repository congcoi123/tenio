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

import javax.annotation.concurrent.GuardedBy;

import com.tenio.common.configuration.constant.CommonConstants;
import com.tenio.common.exception.NullElementPoolException;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.pool.IElementPool;
import com.tenio.engine.ecs.base.IComponent;

/**
 * The object pool mechanism for {@link IComponent}.
 * 
 * @author kong
 * 
 */
public final class ComponentPool extends AbstractLogger implements IElementPool<IComponent> {

	@GuardedBy("this")
	private IComponent[] __pool;
	@GuardedBy("this")
	private boolean[] __used;
	private Class<?> __clazz;

	public ComponentPool(Class<?> clazz) {
		__clazz = clazz;
		__pool = new IComponent[CommonConstants.DEFAULT_NUMBER_ELEMENTS_POOL];
		__used = new boolean[CommonConstants.DEFAULT_NUMBER_ELEMENTS_POOL];

		for (int i = 0; i < __pool.length; i++) {
			try {
				var component = (IComponent) __clazz.getDeclaredConstructor().newInstance();
				__pool[i] = component;
				__used[i] = false;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				_error(e);
			}
		}
	}

	@Override
	public synchronized IComponent get() {
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
		__used = new boolean[oldUsed.length + CommonConstants.ADDED_NUMBER_ELEMENTS_POOL];
		System.arraycopy(oldUsed, 0, __used, 0, oldUsed.length);

		var oldPool = __pool;
		__pool = new IComponent[oldPool.length + CommonConstants.ADDED_NUMBER_ELEMENTS_POOL];
		System.arraycopy(oldPool, 0, __pool, 0, oldPool.length);

		for (int i = oldPool.length; i < __pool.length; i++) {
			try {
				__pool[i] = (IComponent) __clazz.getDeclaredConstructor().newInstance();
				__used[i] = false;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				_error(e);
			}
		}

		_info("COMPONENT POOL",
				_buildgen("Increase the number of elements by ", CommonConstants.ADDED_NUMBER_ELEMENTS_POOL, " to ", __used.length));

		// and allocate the last old ELement
		__used[oldPool.length - 1] = true;
		return __pool[oldPool.length - 1];
	}

	@Override
	public synchronized void repay(IComponent element) {
		boolean flagFound = false;
		for (int i = 0; i < __pool.length; i++) {
			if (__pool[i] == element) {
				__used[i] = false;
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
