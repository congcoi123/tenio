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

package com.tenio.core.bootstrap.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to specify which implementation should be injected when multiple
 * implementations of an interface are available. This annotation works in conjunction
 * with {@link Autowired} and {@link Bean} to provide fine-grained control over dependency
 * injection.
 *
 * <p>Key features:
 * <ul>
 *   <li>Name-based implementation selection</li>
 *   <li>Class-based implementation selection</li>
 *   <li>Integration with dependency injection</li>
 *   <li>Runtime retention for reflection-based processing</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * public class MyService {
 *     &#64;Autowired
 *     &#64;AutowiredQualifier(name = "default")
 *     private Repository repository;
 *     
 *     &#64;Autowired
 *     &#64;AutowiredQualifier(clazz = CustomRepository.class)
 *     private Repository customRepository;
 * }
 * </pre>
 *
 * <p>Note: This annotation can be used in combination with {@link Autowired} to
 * specify which implementation should be injected when multiple implementations
 * of an interface are available.
 *
 * @see Autowired
 * @see Component
 * @see Bean
 * @since 0.5.0
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
