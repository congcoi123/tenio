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

package com.tenio.core.bootstrap.injector;

import com.tenio.core.bootstrap.annotation.Bean;
import com.tenio.core.bootstrap.annotation.BeanFactory;

/**
 * A record that represents a bean class with its associated name qualifier.
 * This class is used internally by the dependency injection system to uniquely
 * identify and manage bean instances.
 *
 * <p>Key features:
 * <ul>
 *   <li>Bean class identification</li>
 *   <li>Name qualifier support</li>
 *   <li>Immutable record design</li>
 *   <li>Thread-safe by design</li>
 *   <li>Hash code and equality support</li>
 * </ul>
 *
 * <p>Note: This record is primarily used internally by the {@link Injector}
 * for bean management and should not be used directly in application code.
 *
 * @see Injector
 * @see Bean
 * @see BeanFactory
 * @since 0.5.0
 */
public record BeanClass(Class<?> clazz, String name) {
}
