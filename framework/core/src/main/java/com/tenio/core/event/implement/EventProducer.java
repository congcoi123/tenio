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

package com.tenio.core.event.implement;

import com.tenio.core.configuration.define.ServerEvent;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Creates and manages event handlers for the game server.
 * This class provides a factory for creating event handlers and managing event emissions.
 * It is designed to be used as a single point of event handling creation and management.
 *
 * <p>Key features:
 * <ul>
 *   <li>Event handler creation</li>
 *   <li>Event emission with parameter passing</li>
 *   <li>Event handler management</li>
 *   <li>Event result collection</li>
 * </ul>
 *
 * <p>Note: This class is not thread-safe and should be used in a single-threaded context
 * or with proper synchronization. It is designed to be the single point of event handler
 * creation and should not be instantiated multiple times.
 *
 * @see EventHandler
 * @see ServerEvent
 * @since 0.3.0
 */
@NotThreadSafe
public final class EventProducer {

  private final EventHandler<Object> eventHandler;

  /**
   * Constructor.
   */
  public EventProducer() {
    eventHandler = new EventHandler<>();
  }

  /**
   * Retrieves an event handler.
   *
   * @return see {@link EventHandler}
   */
  public EventHandler<Object> getEventHandler() {
    return eventHandler;
  }

  /**
   * Emit an event with its parameters.
   *
   * @param event  see {@link ServerEvent}
   * @param params a list parameters of this event
   * @return the event result (the response of its subscribers), see {@link Object} or <b>null</b>
   * @see EventHandler#emit(ServerEvent, Object...)
   */
  public Object emit(ServerEvent event, Object... params) {
    return eventHandler.emit(event, params);
  }

  /**
   * Clear all events and these handlers.
   *
   * @see EventHandler#clear()
   */
  public void clear() {
    eventHandler.clear();
  }
}
