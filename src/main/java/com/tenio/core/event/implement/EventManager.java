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

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.Subscriber;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Manages events and their subscribers in the game server.
 * This class provides a centralized event management system that handles event emission,
 * subscription, and logging. It extends SystemLogger to provide logging capabilities
 * for event-related operations.
 *
 * <p>Key features:
 * <ul>
 *   <li>Event emission with parameter passing</li>
 *   <li>Event subscription management</li>
 *   <li>Event tracing and debugging</li>
 *   <li>Thread-safe event handling</li>
 *   <li>Event result collection</li>
 * </ul>
 *
 * <p>Note: This class is not thread-safe and should be used in a single-threaded context
 * or with proper synchronization.
 *
 * @see ServerEvent
 * @see EventSubscriber
 * @see EventProducer
 * @see SystemLogger
 * @since 0.3.0
 */
@NotThreadSafe
public final class EventManager extends SystemLogger {

  /**
   * A list of subscribers.
   */
  private final List<EventSubscriber> eventSubscribers;
  /**
   * The producer.
   */
  private final EventProducer eventProducer;

  private EventManager() {
    eventSubscribers = new ArrayList<>();
    eventProducer = new EventProducer();
  }

  /**
   * Initialization.
   *
   * @return an instance of {@link EventManager}
   */
  public static EventManager newInstance() {
    return new EventManager();
  }

  /**
   * Emit an event with its parameters.
   *
   * @param event  see {@link ServerEvent}
   * @param params a list parameters of this event
   * @return the event result (the response of its subscribers), see {@link Object} or <b>null</b>
   * @see EventProducer#emit(ServerEvent, Object...)
   */
  public Object emit(ServerEvent event, Object... params) {
    if (isEventForTracing(event)) {
      if (isTraceEnabled()) {
        trace(event.toString(), params);
      }
    } else {
      if (isDebugEnabled()) {
        debugEvent(event.toString(), params);
      }
    }
    return eventProducer.emit(event, params);
  }

  /**
   * Add a subscriber's handler.
   *
   * @param event      see {@link ServerEvent}
   * @param subscriber see {@link Subscriber}
   */
  public void on(ServerEvent event, Subscriber subscriber) {
    if (hasSubscriber(event)) {
      if (isInfoEnabled()) {
        info("SERVER EVENT WARNING", "Duplicated", event);
      }
    }

    eventSubscribers.add(EventSubscriber.newInstance(event, subscriber));
  }

  /**
   * Collect all subscribers and these corresponding events.
   */
  public void subscribe() {
    // clear the old first
    eventProducer.clear();

    // only for log recording
    var events = new ArrayList<ServerEvent>();
    // start handling
    eventSubscribers.forEach(eventSubscriber -> {
      events.add(eventSubscriber.getEvent());
      eventProducer.getEventHandler().subscribe(eventSubscriber.getEvent(),
          eventSubscriber.getSubscriber()::dispatch);
    });
    if (isInfoEnabled()) {
      info("SERVER EVENT SUBSCRIBERS", "Subscribers", events.toString());
    }
  }

  /**
   * Check if an event has any subscribers or not.
   *
   * @param event see {@link ServerEvent}
   * @return {@code true} if an event has any subscribers
   */
  public boolean hasSubscriber(ServerEvent event) {
    for (var subscriber : eventSubscribers) {
      if (subscriber.getEvent() == event) {
        return true;
      }
    }
    return false;
  }

  /**
   * Clear all subscribers and these corresponding events.
   */
  public void clear() {
    eventSubscribers.clear();
    eventProducer.clear();
  }

  private boolean isEventForTracing(ServerEvent event) {
    return switch (event) {
      case DATAGRAM_CHANNEL_READ_MESSAGE_FIRST_TIME, RECEIVED_MESSAGE_FROM_PLAYER, SESSION_READ_MESSAGE,
          SEND_MESSAGE_TO_PLAYER, SESSION_WRITE_MESSAGE -> true;
      default -> false;
    };
  }
}
