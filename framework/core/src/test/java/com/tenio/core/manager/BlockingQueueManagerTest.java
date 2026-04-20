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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For BlockingQueueManager")
class BlockingQueueManagerTest {

  private BlockingQueueManager<String> manager;

  @BeforeEach
  void setUp() {
    manager = new BlockingQueueManager<>(4, LinkedBlockingQueue::new);
  }

  @Test
  @DisplayName("Test constructor with valid cacheSize creates queues")
  void testConstructorValid() {
    assertNotNull(manager);
    assertEquals(4, manager.getCacheSize());
  }

  @Test
  @DisplayName("Test constructor with zero cacheSize throws IllegalArgumentException")
  void testConstructorZeroCacheSizeThrows() {
    assertThrows(IllegalArgumentException.class,
        () -> new BlockingQueueManager<String>(0, LinkedBlockingQueue::new));
  }

  @Test
  @DisplayName("Test constructor with negative cacheSize throws IllegalArgumentException")
  void testConstructorNegativeCacheSizeThrows() {
    assertThrows(IllegalArgumentException.class,
        () -> new BlockingQueueManager<String>(-1, LinkedBlockingQueue::new));
  }

  @Test
  @DisplayName("Test getQueueByElementId returns non-null BlockingQueue")
  void testGetQueueByElementIdReturnsNonNull() {
    assertNotNull(manager.getQueueByElementId(0L));
    assertNotNull(manager.getQueueByElementId(1L));
  }

  @Test
  @DisplayName("Test getQueueByElementId maps IDs deterministically")
  void testGetQueueByElementIdDeterministic() {
    var q0 = manager.getQueueByElementId(0L);
    var q4 = manager.getQueueByElementId(4L); // 4 % 4 = 0
    assertEquals(q0, q4);
  }

  @Test
  @DisplayName("Test getQueueByElementId handles negative IDs")
  void testGetQueueByElementIdNegativeId() {
    assertNotNull(manager.getQueueByElementId(-1L));
  }

  @Test
  @DisplayName("Test getQueueByIndex returns correct queue")
  void testGetQueueByIndex() {
    assertNotNull(manager.getQueueByIndex(0));
    assertNotNull(manager.getQueueByIndex(3));
  }

  @Test
  @DisplayName("Test enqueue and dequeue items")
  void testEnqueueAndDequeue() throws InterruptedException {
    var queue = manager.getQueueByElementId(0L);
    queue.put("test");
    assertEquals("test", queue.take());
  }

  @Test
  @DisplayName("Test clear empties all blocking queues")
  void testClear() {
    manager.getQueueByIndex(0).add("a");
    manager.getQueueByIndex(1).add("b");
    manager.clear();
    assertTrue(manager.getQueueByIndex(0).isEmpty());
    assertTrue(manager.getQueueByIndex(1).isEmpty());
  }

  @Test
  @DisplayName("Test getCacheSize returns configured size")
  void testGetCacheSize() {
    assertEquals(4, manager.getCacheSize());
  }
}
