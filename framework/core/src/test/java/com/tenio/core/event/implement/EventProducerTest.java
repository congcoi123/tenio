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
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tenio.core.configuration.define.ServerEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For EventProducer")
class EventProducerTest {

  private EventProducer producer;

  @BeforeEach
  void setUp() {
    producer = new EventProducer();
  }

  @Test
  @DisplayName("getEventHandler returns non-null handler")
  void testGetEventHandlerIsNotNull() {
    assertNotNull(producer.getEventHandler());
  }

  @Test
  @DisplayName("emit returns null when no subscriber is registered")
  void testEmitWithNoSubscriberReturnsNull() {
    assertNull(producer.emit(ServerEvent.SERVER_TEARDOWN));
  }

  @Test
  @DisplayName("emit returns subscriber result when subscriber is registered")
  void testEmitReturnsSubscriberResult() {
    producer.getEventHandler().subscribe(ServerEvent.FETCHED_CCU_INFO, params -> 42);
    Object result = producer.emit(ServerEvent.FETCHED_CCU_INFO, 1);
    assertEquals(42, result);
  }

  @Test
  @DisplayName("clear removes all subscribers so subsequent emit returns null")
  void testClearRemovesSubscribers() {
    producer.getEventHandler().subscribe(ServerEvent.FETCHED_CCU_INFO, params -> 99);
    producer.clear();
    assertNull(producer.emit(ServerEvent.FETCHED_CCU_INFO, 1));
  }
}
