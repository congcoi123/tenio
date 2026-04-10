/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.Subscriber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For EventSubscriber")
class EventSubscriberTest {

  @Test
  @DisplayName("newInstance creates a non-null EventSubscriber")
  void testNewInstanceNotNull() {
    Subscriber sub = params -> null;
    assertNotNull(EventSubscriber.newInstance(ServerEvent.PLAYER_LOGIN, sub));
  }

  @Test
  @DisplayName("getEvent returns the event passed to newInstance")
  void testGetEventReturnsCorrectEvent() {
    Subscriber sub = params -> null;
    EventSubscriber es = EventSubscriber.newInstance(ServerEvent.ROOM_CREATED_RESULT, sub);
    assertEquals(ServerEvent.ROOM_CREATED_RESULT, es.getEvent());
  }

  @Test
  @DisplayName("getSubscriber returns the same subscriber instance passed to newInstance")
  void testGetSubscriberReturnsSameInstance() {
    Subscriber sub = params -> "result";
    EventSubscriber es = EventSubscriber.newInstance(ServerEvent.DISCONNECT_PLAYER, sub);
    assertSame(sub, es.getSubscriber());
  }

  @Test
  @DisplayName("subscriber can be invoked through getSubscriber")
  void testSubscriberIsInvocable() {
    Subscriber sub = params -> (int) params[0] * 2;
    EventSubscriber es = EventSubscriber.newInstance(ServerEvent.FETCHED_CCU_INFO, sub);
    assertEquals(10, es.getSubscriber().dispatch(5));
  }
}
