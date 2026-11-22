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

package com.tenio.core.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ConcurrentQueueManager")
class ConcurrentQueueManagerTest {

  private ConcurrentQueueManager<String> manager;

  @BeforeEach
  void setUp() {
    manager = new ConcurrentQueueManager<>(3, ConcurrentLinkedQueue::new);
  }

  @Test
  @DisplayName("Test constructor with valid cacheSize creates queues")
  void testConstructorValid() {
    assertNotNull(manager);
    assertEquals(3, manager.getCacheSize());
  }

  @Test
  @DisplayName("Test constructor with zero cacheSize throws IllegalArgumentException")
  void testConstructorZeroCacheSizeThrows() {
    assertThrows(IllegalArgumentException.class,
        () -> new ConcurrentQueueManager<String>(0, ConcurrentLinkedQueue::new));
  }

  @Test
  @DisplayName("Test constructor with negative cacheSize throws IllegalArgumentException")
  void testConstructorNegativeCacheSizeThrows() {
    assertThrows(IllegalArgumentException.class,
        () -> new ConcurrentQueueManager<String>(-1, ConcurrentLinkedQueue::new));
  }

  @Test
  @DisplayName("Test getQueueByElementId returns non-null queue")
  void testGetQueueByElementIdReturnsNonNull() {
    assertNotNull(manager.getQueueByElementId(0L));
    assertNotNull(manager.getQueueByElementId(1L));
    assertNotNull(manager.getQueueByElementId(2L));
  }

  @Test
  @DisplayName("Test getQueueByElementId maps IDs deterministically")
  void testGetQueueByElementIdDeterministic() {
    var q0 = manager.getQueueByElementId(0L);
    var q1 = manager.getQueueByElementId(3L); // 3 % 3 = 0 (same queue as 0)
    assertEquals(q0, q1);
  }

  @Test
  @DisplayName("Test getQueueByElementId works for large ID")
  void testGetQueueByElementIdLargeId() {
    assertNotNull(manager.getQueueByElementId(Long.MAX_VALUE));
  }

  @Test
  @DisplayName("Test getQueueByElementId works for negative ID")
  void testGetQueueByElementIdNegativeId() {
    assertNotNull(manager.getQueueByElementId(-1L));
  }

  @Test
  @DisplayName("Test getQueueByIndex returns the correct queue")
  void testGetQueueByIndex() {
    assertNotNull(manager.getQueueByIndex(0));
    assertNotNull(manager.getQueueByIndex(1));
    assertNotNull(manager.getQueueByIndex(2));
  }

  @Test
  @DisplayName("Test enqueue and dequeue items from queue")
  void testEnqueueAndDequeue() {
    var queue = manager.getQueueByElementId(0L);
    queue.add("item1");
    assertEquals("item1", queue.poll());
  }

  @Test
  @DisplayName("Test clear empties all queues")
  void testClear() {
    manager.getQueueByIndex(0).add("a");
    manager.getQueueByIndex(1).add("b");
    manager.getQueueByIndex(2).add("c");
    manager.clear();
    assertTrue(manager.getQueueByIndex(0).isEmpty());
    assertTrue(manager.getQueueByIndex(1).isEmpty());
    assertTrue(manager.getQueueByIndex(2).isEmpty());
  }

  @Test
  @DisplayName("Test getCacheSize returns configured size")
  void testGetCacheSize() {
    assertEquals(3, manager.getCacheSize());
  }

  @Test
  @DisplayName("Test queue returned by elementId is empty initially")
  void testQueueIsEmptyInitially() {
    assertNull(manager.getQueueByElementId(0L).peek());
  }
}
