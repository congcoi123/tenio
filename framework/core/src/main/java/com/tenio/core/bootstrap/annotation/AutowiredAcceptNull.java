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

import com.tenio.core.bootstrap.injector.Injector;
import com.tenio.core.exception.NoImplementedClassFoundException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to mark fields that can be autowired with {@code null} values.
 * This annotation is similar to {@link Autowired} but allows the dependency
 * injection system to inject {@code null} values when no suitable bean is found.
 *
 * <p>Key features:
 * <ul>
 *   <li>Optional dependency injection</li>
 *   <li>Null value acceptance</li>
 *   <li>Integration with dependency injection</li>
 *   <li>Runtime retention for reflection-based processing</li>
 *   <li>Field-level annotation</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * &#64;Component
 * public class UserService {
 *     &#64;AutowiredAcceptNull
 *     private EmailService emailService; // Can be null if no EmailService bean exists
 *     
 *     public void sendNotification(User user) {
 *         if (emailService != null) {
 *             emailService.sendEmail(user.getEmail());
 *         }
 *     }
 * }
 * </pre>
 *
 * <p>Note: This annotation is useful when:
 * <ul>
 *   <li>A dependency is optional</li>
 *   <li>Fallback behavior is needed when no bean is found</li>
 *   <li>Gradual feature enablement is desired</li>
 * </ul>
 *
 * @see Autowired
 * @see AutowiredQualifier
 * @see Injector
 * @see NoImplementedClassFoundException
 * @since 0.3.0
 */
@Retention(RUNTIME)
@Target(FIELD)
@Documented
public @interface AutowiredAcceptNull {
}
