/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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
import java.io.Serial;

/**
 * When it is adding a duplicated bean, which share the same class type and name.
 *
 * @see Bean
 * @see AutowiredQualifier
 * @since 0.5.0
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
