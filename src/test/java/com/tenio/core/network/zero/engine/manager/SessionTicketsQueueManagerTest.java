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

package com.tenio.core.network.zero.engine.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.tenio.core.network.entity.session.Session;
import java.util.concurrent.BlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For SessionTicketsQueueManager")
class SessionTicketsQueueManagerTest {

  private SessionTicketsQueueManager manager;

  @BeforeEach
  void setUp() {
    manager = new SessionTicketsQueueManager(4);
  }

  @Test
  @DisplayName("Constructor with zero cache size should throw IllegalArgumentException")
  void testConstructorWithZeroCacheSizeThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> new SessionTicketsQueueManager(0));
  }

  @Test
  @DisplayName("Constructor with negative cache size should throw IllegalArgumentException")
  void testConstructorWithNegativeCacheSizeThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> new SessionTicketsQueueManager(-1));
  }

  @Test
  @DisplayName("getCacheSize should return the configured value")
  void testGetCacheSizeReturnsConfiguredValue() {
    assertEquals(4, manager.getCacheSize());
  }

  @Test
  @DisplayName("getQueueByElementId should return a non-null queue")
  void testGetQueueByElementIdReturnsNonNullQueue() {
    assertNotNull(manager.getQueueByElementId(0L));
    assertNotNull(manager.getQueueByElementId(3L));
  }

  @Test
  @DisplayName("Same session ID should always map to the same queue")
  void testSameSessionIdMapsToSameQueue() {
    BlockingQueue<Session> first = manager.getQueueByElementId(7L);
    BlockingQueue<Session> second = manager.getQueueByElementId(7L);
    assertEquals(first, second);
  }

  @Test
  @DisplayName("getQueueByIndex and getQueueByElementId return the same queue for equivalent IDs")
  void testGetQueueByIndexMatchesGetQueueByElementId() {
    BlockingQueue<Session> byIndex = manager.getQueueByIndex(0);
    BlockingQueue<Session> byElement = manager.getQueueByElementId(0L);
    assertEquals(byIndex, byElement);
  }

  @Test
  @DisplayName("clear should remove all elements from all queues")
  void testClearRemovesAllElements() {
    Session session = mock(Session.class);
    manager.getQueueByElementId(0L).add(session);
    assertEquals(1, manager.getQueueByElementId(0L).size());

    manager.clear();

    assertEquals(0, manager.getQueueByElementId(0L).size());
  }

  @Test
  @DisplayName("IDs that share the same modulo map to the same queue")
  void testIdModuloCacheSizeMapsToCorrectQueue() {
    // With cacheSize 4, IDs 0, 4, 8 all map to queue index 0
    BlockingQueue<Session> queue0 = manager.getQueueByElementId(0L);
    BlockingQueue<Session> queue4 = manager.getQueueByElementId(4L);
    BlockingQueue<Session> queue8 = manager.getQueueByElementId(8L);
    assertEquals(queue0, queue4);
    assertEquals(queue0, queue8);
  }

  @Test
  @DisplayName("Different modulo groups map to different queues")
  void testDifferentModuloGroupsMaptoDifferentQueues() {
    BlockingQueue<Session> queue0 = manager.getQueueByElementId(0L);
    BlockingQueue<Session> queue1 = manager.getQueueByElementId(1L);
    // These should be different queue objects
    assertNotNull(queue0);
    assertNotNull(queue1);
    // They map to different indices (0 vs 1), so they are different instances
    assert !queue0.equals(queue1);
  }
}
