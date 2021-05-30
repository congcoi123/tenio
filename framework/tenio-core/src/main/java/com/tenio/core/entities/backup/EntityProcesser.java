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
package com.tenio.core.entities.backup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.tenio.core.entities.backup.annotations.Column;
import com.tenio.core.entities.backup.annotations.Entity;
import com.tenio.core.entities.backup.annotations.Sanitizer;

/**
 * This class is used to export game objects with supported annotation to JSON
 * data (backup) or create game objects from JSON data (restore).
 * 
 * @see Column
 * @see Entity
 * @see Sanitizer
 */
public final class EntityProcesser {

	private EntityProcesser() {

	}

	/**
	 * Execute an object method (included private level access)
	 * 
	 * @param object the desired object
	 * @throws Exception the exception
	 */
	private static void __sanitizerObject(Object object) throws Exception {
		Class<?> clazz = object.getClass();
		for (Method method : clazz.getDeclaredMethods()) {
			// Check if a method contains @Sanitizer annotation for processing
			if (method.isAnnotationPresent(Sanitizer.class)) {
				method.setAccessible(true);
				method.invoke(object);
			}
		}
	}

	/**
	 * Check if the corresponding object has necessary annotation or not
	 * 
	 * @param object the desired object
	 * @throws Exception the exception
	 */
	private static void __checkEntity(Object object) throws Exception {
		if (Objects.isNull(object)) {
			throw new Exception("The object to serialize is null");
		}

		Class<?> clazz = object.getClass();
		// Check if a method contains @Entity annotation for processing
		if (!clazz.isAnnotationPresent(Entity.class)) {
			throw new Exception("The class is not Entity");
		}
	}

	/**
	 * Use recursion method to get all fields of an object (included its parent's
	 * fields)
	 * 
	 * @param clazz the class type
	 * @return the list of fields
	 */
	private static Field[] __getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<Field>();
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		if (clazz.getSuperclass() != null) {
			fields.addAll(Arrays.asList(__getAllFields(clazz.getSuperclass())));
		}
		return fields.toArray(new Field[] {});
	}

	/**
	 * Convert the desired object to JSON data
	 * 
	 * @param object the desired object
	 * @return the JSON data in string, see {@link String}
	 * @throws Exception the exception
	 */
	public static String exportToJSON(Object object) throws Exception {
		Class<?> clazz = object.getClass();
		Map<String, String> jsonMap = new HashMap<String, String>();

		try {
			__checkEntity(object);
			__sanitizerObject(object);

			// Declare map attributes
			// - key: object's name
			// - value: object's value
			Field[] fields = __getAllFields(clazz);
			for (Field field : fields) {
				field.setAccessible(true);
				if (field.isAnnotationPresent(Column.class)) {
					String value = "NA";
					if (field.get(object) != null) {
						value = field.get(object).toString();
					}
					jsonMap.put(field.getAnnotation(Column.class).name(), value);
				}
			}

			// Convert Map data to JSON string
			String jsonString = jsonMap.entrySet().stream()
					.map(entry -> String.format("\"%s\":\"%s\"", entry.getKey(), entry.getValue()))
					.collect(Collectors.joining(","));
			return String.format("{%s}", jsonString);

		} catch (Exception e) {
			throw e;
		}
	}

}
