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
package com.tenio.core.bootstrap.injector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;

import com.tenio.core.bootstrap.annotation.ExtComponent;
import com.tenio.core.bootstrap.utitlity.ClazzLoaderUtility;
import com.tenio.core.bootstrap.utitlity.InjectionUtility;
import com.tenio.core.exception.MultipleImplementedClassForInterfaceException;
import com.tenio.core.exception.NoImplementedClassFoundException;

/**
 * @author kong
 */
public final class Injector {

	private static final String DEFAULT_BOOTSTRAP_PACKAGE = "com.tenio.core.bootstrap";
	private static final String DEFAULT_EXTENSION_EVENT_PACKAGE = "com.tenio.core.extension.event";

	private final Map<Class<?>, Class<?>> __implementedClazzsMap;
	private final Map<Class<?>, Object> __clazzInstancesMap;

	public Injector() {
		__implementedClazzsMap = new HashMap<Class<?>, Class<?>>();
		__clazzInstancesMap = new HashMap<Class<?>, Object>();
	}

	public <T> T getInstance(Class<T> clazz) throws Exception {
		return __getBeanInstance(clazz);
	}

	public void scanPackages(Class<?> entryClazz)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		Class<?>[] clazzsBootstrap = ClazzLoaderUtility.getClazzs(DEFAULT_BOOTSTRAP_PACKAGE);
		Class<?>[] clazzsEvent = ClazzLoaderUtility.getClazzs(DEFAULT_EXTENSION_EVENT_PACKAGE);
		Class<?>[] clazzs = ClazzLoaderUtility.getClazzs(entryClazz.getPackage().getName());
		clazzs = Stream.concat(Arrays.stream(clazzs), Arrays.stream(clazzsBootstrap)).toArray(Class<?>[]::new);
		clazzs = Stream.concat(Arrays.stream(clazzs), Arrays.stream(clazzsEvent)).toArray(Class<?>[]::new);

		Reflections reflectionsBootstrap = new Reflections(DEFAULT_BOOTSTRAP_PACKAGE);
		Reflections reflectionsEvent = new Reflections(DEFAULT_EXTENSION_EVENT_PACKAGE);
		Reflections reflections = new Reflections(entryClazz.getPackage().getName());
		reflections = reflections.merge(reflectionsBootstrap).merge(reflectionsEvent);

		Set<Class<?>> implementedClazzs = reflections.getTypesAnnotatedWith(ExtComponent.class);

		// scan all interface with its implemented classes
		for (Class<?> implementedClazz : implementedClazzs) {
			Class<?>[] clazzInterfaces = implementedClazz.getInterfaces();
			if (clazzInterfaces.length == 0) {
				__implementedClazzsMap.put(implementedClazz, implementedClazz);
			} else {
				for (Class<?> clazzInterface : clazzInterfaces) {
					__implementedClazzsMap.put(implementedClazz, clazzInterface);
				}
			}
		}

		// create class instance based on annotations
		for (Class<?> clazz : clazzs) {
			if (clazz.isAnnotationPresent(ExtComponent.class)) {
				Object clazzInstance = clazz.getDeclaredConstructor().newInstance();
				__clazzInstancesMap.put(clazz, clazzInstance);
				// recursively create field instance for this class instance
				InjectionUtility.autowire(this, clazz, clazzInstance);
			}
		}

	}

	public <T> Object getBeanInstance(Class<T> clazzInterface, String fieldName, String qualifier)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Class<?> implementedClazz = __getImplementedClazz(clazzInterface, fieldName, qualifier);

		if (__clazzInstancesMap.containsKey(implementedClazz)) {
			return __clazzInstancesMap.get(implementedClazz);
		}

		synchronized (__clazzInstancesMap) {
			Object clazzInstance = implementedClazz.getDeclaredConstructor().newInstance();
			__clazzInstancesMap.put(implementedClazz, clazzInstance);
			return clazzInstance;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T __getBeanInstance(Class<T> clazzInterface) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return (T) getBeanInstance(clazzInterface, null, null);
	}

	private Class<?> __getImplementedClazz(Class<?> clazzInterface, final String fieldName, final String qualifier) {
		Set<Entry<Class<?>, Class<?>>> implementedClazzs = __implementedClazzsMap.entrySet().stream()
				.filter(entry -> entry.getValue() == clazzInterface).collect(Collectors.toSet());

		if (implementedClazzs == null || implementedClazzs.isEmpty()) {
			throw new NoImplementedClassFoundException(clazzInterface.getName());
		} else if (implementedClazzs.size() == 1) {
			// just only one implemented class for the interface
			Optional<Entry<Class<?>, Class<?>>> optional = implementedClazzs.stream().findFirst();
			if (optional.isPresent()) {
				return optional.get().getKey();
			}
		} else if (implementedClazzs.size() > 1) {
			// multiple implemented class from the interface, need to be selected by
			// "qualifier" value
			final String findBy = (qualifier == null || qualifier.trim().length() == 0) ? fieldName : qualifier;
			Optional<Entry<Class<?>, Class<?>>> optional = implementedClazzs.stream()
					.filter(entry -> entry.getKey().getSimpleName().equalsIgnoreCase(findBy)).findAny();
			if (optional.isPresent()) {
				return optional.get().getKey();
			} else {
				// could not find a appropriately single instance, so throw an exception
				throw new MultipleImplementedClassForInterfaceException(clazzInterface.getName());
			}
		}

		return null;
	}
}
