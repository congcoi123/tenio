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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.tenio.core.bootstrap.injector.Injector;
import com.tenio.core.exception.IllegalDefinedAccessControlException;
import com.tenio.core.exception.IllegalReturnTypeException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to mark methods that create and configure beans in the application.
 * This annotation is used in conjunction with {@link BeanFactory} to define
 * bean creation methods that will be automatically processed by the dependency
 * injection system. When the name of bean is defined, it must work with
 * {@link AutowiredQualifier} to fetch the generated object.
 *
 * <p>Key features:
 * <ul>
 *   <li>Bean creation method marking</li>
 *   <li>Custom bean naming support</li>
 *   <li>Integration with dependency injection</li>
 *   <li>Runtime retention for reflection-based processing</li>
 *   <li>Method-level bean definition</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * {@code @BeanFactory
 * public class AppConfiguration {
 *     @Bean("userService")
 *     public UserService createUserService() {
 *         return new UserServiceImpl();
 *     }
 *     
 *     @Bean
 *     public DataSource createDataSource() {
 *         return new HikariDataSource();
 *     }
 * }
 * }
 * </pre>
 *
 * <p>Note: Methods annotated with this annotation must:
 * <ul>
 *   <li>Be public</li>
 *   <li>Return a non-primitive type</li>
 *   <li>Not return void</li>
 *   <li>Be defined within a class annotated with {@link BeanFactory}</li>
 * </ul>
 *
 * @see BeanFactory
 * @see Injector
 * @see IllegalReturnTypeException
 * @see IllegalDefinedAccessControlException
 * @since 0.3.0
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface Bean {

  /**
   * The name of the bean. If not specified, the bean will be registered
   * with its default name ("").
   *
   * @return the bean name
   */
  String value() default "";
}
