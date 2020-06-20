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
package com.tenio.identity.logger.pool;

import javax.annotation.concurrent.GuardedBy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tenio.common.configuration.constant.Constants;
import com.tenio.common.exception.NullElementPoolException;
import com.tenio.common.logger.pool.StringBuilderPool;
import com.tenio.common.pool.IElementPool;

/**
 * The object pool mechanism for {@link StringBuilder}.
 * 
 * @author kong
 * 
 */
public final class StringBuilderPool implements IElementPool<StringBuilder> {

	private static volatile StringBuilderPool __instance;

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static StringBuilderPool getInstance() {
		var ref = __instance;
		if (ref == null) {
			synchronized (StringBuilderPool.class) {
				ref = __instance;
				if (ref == null) {
					__instance = ref = new StringBuilderPool();
				}
			}
		}
		return ref;
	}

	private Logger __logger = LogManager.getLogger(getClass());
	@GuardedBy("this")
	private StringBuilder[] __pool;
	@GuardedBy("this")
	private boolean[] __used;

	public StringBuilderPool() {
		__pool = new StringBuilder[Constants.BASE_ELEMENT_POOL];
		__used = new boolean[Constants.BASE_ELEMENT_POOL];

		for (int i = 0; i < __pool.length; i++) {
			__pool[i] = new StringBuilder();
			__used[i] = false;
		}
	}

	@Override
	public synchronized StringBuilder get() {
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
		__used = new boolean[oldUsed.length + Constants.ADD_ELEMENT_POOL];
		System.arraycopy(oldUsed, 0, __used, 0, oldUsed.length);

		var oldPool = __pool;
		__pool = new StringBuilder[oldPool.length + Constants.ADD_ELEMENT_POOL];
		System.arraycopy(oldPool, 0, __pool, 0, oldPool.length);

		for (int i = oldPool.length; i < __pool.length; i++) {
			__pool[i] = new StringBuilder();
			__used[i] = false;
		}

		__infoWithoutPool("STRINGBUILDER POOL",
				__strgen("Increase the number of elements by ", Constants.ADD_ELEMENT_POOL, " to ", __used.length));

		// and allocate the last old ELement
		__used[oldPool.length - 1] = true;
		return __pool[oldPool.length - 1];
	}

	@Override
	public synchronized void repay(StringBuilder element) {
		boolean flagFound = false;
		for (int i = 0; i < __pool.length; i++) {
			if (__pool[i] == element) {
				__used[i] = false;
				// Clear
				element.setLength(0);
				flagFound = true;
				break;
			}
		}
		if (!flagFound) {
			throw new NullElementPoolException();
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

	/**
	 * Only use for {@link StringBuilderPool}. It might cause out of memory, so be
	 * careful if you use it. You are warned!
	 * 
	 * @param tag the tag type
	 * @param msg the message content
	 */
	private final void __infoWithoutPool(final String tag, final String msg) {
		if (!__logger.isInfoEnabled()) {
			return;
		}
		var builder = new StringBuilder();
		builder.append("[").append(tag).append("] ").append(msg);
		__logger.info(builder.toString());
	}

	/**
	 * To generate {@code String} for logging information by the corresponding
	 * objects
	 * 
	 * @param objects the corresponding objects, see {@link Object}
	 * @return a string value
	 */
	private final String __strgen(final Object... objects) {
		var builder = new StringBuilder();
		for (var object : objects) {
			builder.append(object);
		}
		return builder.toString();
	}

}
