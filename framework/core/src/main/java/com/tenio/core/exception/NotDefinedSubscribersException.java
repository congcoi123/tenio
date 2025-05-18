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

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.event.implement.EventSubscriber;
import java.io.Serial;

/**
 * Exception thrown when attempting to emit an event that has no registered subscribers.
 * This exception indicates that an event was emitted but no handlers were registered
 * to process it, which may indicate a configuration or initialization issue.
 *
 * <p>Common causes:
 * <ul>
 *   <li>Event subscribers not properly registered during initialization</li>
 *   <li>Missing event handler configuration</li>
 *   <li>Event type mismatch between emitter and subscribers</li>
 *   <li>Premature event emission before subscriber registration</li>
 * </ul>
 *
 * <p>Note: This exception provides information about the event that was attempted
 * to be emitted, helping to identify which event handlers need to be registered.
 *
 * @see EventManager
 * @see EventSubscriber
 * @see ServerEvent
 * @since 0.3.0
 */
public final class NotDefinedSubscribersException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 4569867192216119437L;

  /**
   * Creates a new exception.
   *
   * @param classes an array of {@link Class} that are missing in declaration
   */
  public NotDefinedSubscribersException(Class<?>... classes) {
    super(getMessage(classes));
  }

  private static String getMessage(Class<?>... classes) {
    var builder = new StringBuilder();
    builder.append("Need to implement interfaces: ");
    for (var clazz : classes) {
      builder.append(clazz.getName());
      builder.append(", ");
    }
    builder.setLength(builder.length() - 2);
    return builder.toString();
  }
}
