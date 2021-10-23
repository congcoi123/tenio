/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
import com.tenio.core.event.Emitter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class for handling events and these subscribers.
 *
 * @param <T> the template
 */
public final class EventHandler<T> {

  /**
   * An instance creates a mapping between an event with its list of event
   * handlers.
   */
  private final Map<ServerEvent, Emitter<T>> delegate;

  public EventHandler() {
    delegate = new HashMap<ServerEvent, Emitter<T>>();
  }

  /**
   * Create a link between an event and its list of event handlers.
   *
   * @param event   see {@link ServerEvent}
   * @param emitter see {@link Emitter}
   */
  public void subscribe(ServerEvent event, Emitter<T> emitter) {
    delegate.put(event, emitter);
  }

  /**
   * Emit an event with its parameters.
   *
   * @param event  see {@link ServerEvent}
   * @param params a list parameters of this event
   * @return the event result (the response of its subscribers), see {@link Object} or <b>null</b>
   */
  public Object emit(ServerEvent event, @SuppressWarnings("unchecked") T... params) {
    if (delegate.containsKey(event)) {
      return delegate.get(event).emit(params);
    }
    return null;
  }

  /**
   * Clear all events and these handlers.
   */
  public void clear() {
    if (!delegate.isEmpty()) {
      delegate.clear();
    }
  }
}
