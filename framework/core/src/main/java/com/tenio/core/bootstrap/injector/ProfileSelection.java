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

package com.tenio.core.bootstrap.injector;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Immutable startup policy shared by discovery, factories, mappings and field injection.
 *
 * @since 0.7.4
 */
public final class ProfileSelection {

  private static final String BOOTSTRAP_ANNOTATION = "com.tenio.core.bootstrap.annotation";
  private static final String PROFILE_TAG = "profile";

  /** Immutable normalized, case-sensitive active names. */
  private final Set<String> active;

  /**
   * Creates a detached selection; null/empty supports legacy unrestricted programmatic scans.
   *
   * @param profiles active names, including optional CSV entries
   */
  public ProfileSelection(Set<String> profiles) {
    active = Collections.unmodifiableSet(normalize(profiles == null ? null : profiles.toArray(String[]::new)));
  }

  /**
   * Parses the mandatory XML property, trimming tokens and collapsing duplicates.
   *
   * @param value comma-separated XML text
   * @return immutable names in declaration order
   * @throws IllegalArgumentException for missing, blank or comma-only text
   */
  public static Set<String> parseRequired(String value) {
    Set<String> names = normalize(new String[] {value});
    if (names.isEmpty()) {
      throw new IllegalArgumentException("Mandatory server property 'profile' must contain at least one profile name");
    }
    return Collections.unmodifiableSet(names);
  }

  /**
   * Exposes selected names without allowing mutation.
   *
   * @return immutable active names
   */
  public Set<String> activeProfiles() {
    return active;
  }

  /**
   * Evaluates OR alternatives; an empty/blank restriction participates everywhere.
   *
   * @param restrictions annotation values, optionally containing CSV entries
   * @return true for unrestricted or intersecting alternatives
   */
  public boolean matches(String... restrictions) {
    Set<String> names = normalize(restrictions);
    return names.isEmpty() || names.stream().anyMatch(active::contains);
  }

  /**
   * Combines all TenIO annotation restrictions on one element with AND. Class selection and member
   * selection are independent and must both succeed before a side effect occurs.
   *
   * @param element concrete class, factory/mapping method or injected field
   * @return whether this annotated element participates
   */
  public boolean matches(AnnotatedElement element) {
    for (Annotation annotation : element.getAnnotations()) {
      if (!annotation
          .annotationType()
          .getPackageName()
          .equals(BOOTSTRAP_ANNOTATION)) {
        continue;
      }
      try {
        if (!matches((String[]) annotation.annotationType().getMethod(PROFILE_TAG).invoke(annotation))) {
          return false;
        }
      } catch (NoSuchMethodException
          | IllegalAccessException
          | InvocationTargetException exception) {
        throw new IllegalStateException("Cannot inspect bootstrap annotation profile", exception);
      }
    }
    return true;
  }

  private static Set<String> normalize(String[] values) {
    Set<String> result = new LinkedHashSet<>();
    if (values != null) {
      for (String value : values) {
        if (value == null) {
          continue;
        }
        for (String token : value.split(",")) {
          if (!token.isBlank()) {
            result.add(token.trim());
          }
        }
      }
    }
    return result;
  }
}
