/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.common.data;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates detached, recursively unmodifiable snapshots for generic data containers.
 */
public final class ImmutableSnapshot {

  private ImmutableSnapshot() {
    throw new UnsupportedOperationException("This class does not support creating an instance");
  }

  /**
   * Creates a recursively detached map that permits null keys and values.
   * Arrays are represented as unmodifiable lists so callers cannot mutate snapshot contents.
   *
   * @param source source map
   * @return immutable snapshot
   */
  public static Map<String, Object> ofMap(Map<String, Object> source) {
    return copyMap(source, new IdentityHashMap<>());
  }

  /**
   * Creates a recursively detached list that permits null values.
   * Arrays are represented as unmodifiable lists so callers cannot mutate snapshot contents.
   *
   * @param source source collection
   * @return immutable snapshot
   */
  public static List<Object> ofCollection(Collection<?> source) {
    return copyCollection(source, new IdentityHashMap<>());
  }

  private static Map<String, Object> copyMap(Map<String, Object> source,
      IdentityHashMap<Object, Boolean> visiting) {
    enter(source, visiting);
    try {
      var result = new LinkedHashMap<String, Object>(source.size());
      source.forEach((key, value) -> result.put(key, copy(value, visiting)));
      return Collections.unmodifiableMap(result);
    } finally {
      visiting.remove(source);
    }
  }

  private static List<Object> copyCollection(Collection<?> source,
      IdentityHashMap<Object, Boolean> visiting) {
    enter(source, visiting);
    try {
      var result = new ArrayList<>(source.size());
      for (var value : source) {
        result.add(copy(value, visiting));
      }
      return Collections.unmodifiableList(result);
    } finally {
      visiting.remove(source);
    }
  }

  @SuppressWarnings("unchecked")
  private static Object copy(Object value, IdentityHashMap<Object, Boolean> visiting) {
    if (value instanceof Map<?, ?> map) {
      return copyMap((Map<String, Object>) map, visiting);
    }
    if (value instanceof Collection<?> collection) {
      return copyCollection(collection, visiting);
    }
    if (value != null && value.getClass().isArray()) {
      enter(value, visiting);
      try {
        int length = Array.getLength(value);
        var result = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
          result.add(copy(Array.get(value, i), visiting));
        }
        return Collections.unmodifiableList(result);
      } finally {
        visiting.remove(value);
      }
    }
    return value;
  }

  private static void enter(Object value, IdentityHashMap<Object, Boolean> visiting) {
    if (visiting.put(value, Boolean.TRUE) != null) {
      throw new IllegalArgumentException("Cyclic data cannot be represented as a snapshot");
    }
  }
}
