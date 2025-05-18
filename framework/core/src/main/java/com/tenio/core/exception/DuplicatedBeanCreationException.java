/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.core.exception;

import com.tenio.core.bootstrap.annotation.AutowiredQualifier;
import com.tenio.core.bootstrap.annotation.Bean;
import com.tenio.core.bootstrap.annotation.BeanFactory;
import com.tenio.core.bootstrap.annotation.Component;
import java.io.Serial;

/**
 * Exception thrown when attempting to create a duplicate bean in the dependency injection container.
 * This exception occurs when multiple bean definitions are found for the same type or name,
 * violating the singleton pattern or unique bean requirement.
 *
 * <p>Common causes:
 * <ul>
 *   <li>Multiple classes implementing the same interface</li>
 *   <li>Duplicate bean names in configuration</li>
 *   <li>Multiple component scan paths including the same class</li>
 *   <li>Incorrect bean scope configuration</li>
 * </ul>
 *
 * <p>Note: This exception provides information about the duplicate bean type or name,
 * helping to identify which bean definitions need to be resolved.
 *
 * @see BeanFactory
 * @see Component
 * @since 0.3.0
 */
public final class DuplicatedBeanCreationException extends Exception {

  @Serial
  private static final long serialVersionUID = -2167364908295120478L;

  /**
   * Initialization.
   *
   * @param clazz the bean's {@link Class} type
   * @param name  the bean's {@link String} name
   */
  public DuplicatedBeanCreationException(Class<?> clazz, String name) {
    super(String.format("Duplicated bean creation with type: %s, and name: %s",
        clazz.getSimpleName(), name));
  }
}
