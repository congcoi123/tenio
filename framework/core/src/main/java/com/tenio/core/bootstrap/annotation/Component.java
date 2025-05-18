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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.tenio.core.bootstrap.injector.Injector;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to mark classes as components that should be automatically
 * detected and instantiated by the dependency injection system.
 * This is a fundamental annotation for component scanning and automatic
 * bean creation.
 *
 * <p>Key features:
 * <ul>
 *   <li>Automatic component detection</li>
 *   <li>Singleton bean creation</li>
 *   <li>Dependency injection support</li>
 *   <li>Runtime retention for reflection-based processing</li>
 *   <li>Inheritance support through {@link Inherited}</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * &#64;Component
 * public class UserService {
 *     &#64;Autowired
 *     private UserRepository repository;
 *     
 *     public User findById(String id) {
 *         return repository.findById(id);
 *     }
 * }
 * </pre>
 *
 * <p>Note: Classes annotated with this annotation:
 * <ul>
 *   <li>Must have a no-args constructor</li>
 *   <li>Will be instantiated as singleton beans</li>
 *   <li>Can use other dependency injection annotations</li>
 *   <li>Will be automatically scanned by the {@link Injector}</li>
 * </ul>
 *
 * @see Injector
 * @see Autowired
 * @see AutowiredQualifier
 * @see Bean
 * @since 0.3.0
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@Inherited
public @interface Component {
}
