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

import com.tenio.core.bootstrap.annotation.Component;
import java.io.Serial;

/**
 * Exception thrown when a required implementation class cannot be found or instantiated.
 * This exception occurs when the system attempts to load or create an instance of a
 * required class but fails to find a valid implementation.
 *
 * <p>Common causes:
 * <ul>
 *   <li>Missing implementation class in classpath</li>
 *   <li>Class instantiation failures</li>
 *   <li>Incompatible implementation version</li>
 *   <li>Class loading errors</li>
 * </ul>
 *
 * <p>Note: This exception provides information about the class that was attempted
 * to be loaded, helping to identify which implementation needs to be provided.
 *
 * @see ClassLoader
 * @see Class
 * @since 0.3.0
 */
public final class NoImplementedClassFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -2836756456705984458L;

  /**
   * Creates a new instance.
   *
   * @param clazz the interface of which there is no implementation
   */
  public NoImplementedClassFoundException(Class<?> clazz) {
    super(String.format("Unable to find any implementation for the class: %s", clazz.getName()));
  }
}
