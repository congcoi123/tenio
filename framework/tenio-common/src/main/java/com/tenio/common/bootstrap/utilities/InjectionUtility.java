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
package com.tenio.common.bootstrap.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import com.tenio.common.bootstrap.annotations.Autowired;
import com.tenio.common.bootstrap.annotations.AutowiredAcceptNull;
import com.tenio.common.bootstrap.annotations.AutowiredQualifier;
import com.tenio.common.bootstrap.injector.Injector;
import com.tenio.common.exceptions.NoImplementedClassFoundException;

public final class InjectionUtility {

	private InjectionUtility() {

	}

	/**
	 * Perform injection recursively
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	public static void autowire(Injector injector, Class<?> clazz, Object clazzInstance)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {

		Set<Field> fields = __findFields(clazz);
		for (Field field : fields) {
			String qualifier = field.isAnnotationPresent(AutowiredQualifier.class)
					? field.getAnnotation(AutowiredQualifier.class).value()
					: null;
			if (field.isAnnotationPresent(AutowiredAcceptNull.class)) {
				try {
					Object fieldInstance = injector.getBeanInstance(field.getType(), field.getName(), qualifier);
					field.set(clazzInstance, fieldInstance);
					autowire(injector, fieldInstance.getClass(), fieldInstance);
				} catch (NoImplementedClassFoundException e) {
					// do nothing
				}
			} else {
				Object fieldInstance = injector.getBeanInstance(field.getType(), field.getName(), qualifier);
				field.set(clazzInstance, fieldInstance);
				autowire(injector, fieldInstance.getClass(), fieldInstance);
			}
		}
	}

	/**
	 * Get all the fields having {@link Autowired} or {@link AutowiredAcceptNull}
	 * annotation used while declaration
	 * 
	 * @param clazz
	 * @return a set of fields
	 */
	private static Set<Field> __findFields(Class<?> clazz) {
		Set<Field> fields = new HashSet<Field>();

		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(Autowired.class)
						|| field.isAnnotationPresent(AutowiredAcceptNull.class)) {
					field.setAccessible(true);
					fields.add(field);
				}
			}
			clazz = clazz.getSuperclass();
		}
		return fields;

	}
	
}
