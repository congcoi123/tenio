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

package com.tenio.core.bootstrap.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Using <code>AutowiredQualifier</code> annotation with {@link Autowired}
 * or {@link AutowiredAcceptNull} annotation.
 *
 * <p>This annotation can be used to avoid conflict if there are multiple
 * implementations of a same interface.
 */
@Target({FIELD})
@Retention(RUNTIME)
@Documented
public @interface AutowiredQualifier {

  /**
   * Declares the name should be used to distinguish implementations.
   *
   * @return the qualifier name for an implementation
   */
  String name() default "";

  /**
   * Declares the name should be used to distinguish implementations.
   *
   * @return the qualifier name for an implementation
   */
  Class<?> clazz() default DEFAULT.class;

  /**
   * Dummy default class
   */
  final class DEFAULT {
  }
}
