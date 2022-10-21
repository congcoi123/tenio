/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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
import com.tenio.core.event.Subscriber;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An instance which creates a mapping between a server event type with its corresponding
 * subscriber.
 */
@ThreadSafe
public final class EventSubscriber {

  private final ServerEvent serverEvent;

  private final Subscriber subscriber;

  private EventSubscriber(ServerEvent serverEvent, Subscriber subscriber) {
    this.serverEvent = serverEvent;
    this.subscriber = subscriber;
  }

  /**
   * Initialization.
   *
   * @param event      the {@link ServerEvent} that should be subscribed
   * @param subscriber the corresponding {@link Subscriber} for the event
   * @return a new instance of {@link EventSubscriber}
   */
  public static EventSubscriber newInstance(ServerEvent event, Subscriber subscriber) {
    return new EventSubscriber(event, subscriber);
  }

  /**
   * Retrieves a server event that is subscribed.
   *
   * @return {@link ServerEvent} that is subscribed
   */
  public ServerEvent getEvent() {
    return serverEvent;
  }

  /**
   * Retrieves a server event subscriber.
   *
   * @return an instance of {@link Subscriber} for the server event
   */
  public Subscriber getSubscriber() {
    return subscriber;
  }
}
