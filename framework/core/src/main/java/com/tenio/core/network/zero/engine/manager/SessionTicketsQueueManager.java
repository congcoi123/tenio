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

package com.tenio.core.network.zero.engine.manager;

import com.tenio.core.manager.BlockingQueueManager;
import com.tenio.core.network.entity.session.Session;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages a collection of ticket queues for sessions, partitioned by session ID.
 *
 * <p>This manager creates a fixed number of {@link Queue}s or {@link BlockingQueue}s (based on
 * the provided {@code cacheSize}), and maps each {@link Session} to a specific queue using a
 * hashing mechanism based on {@code session.getId()}. This is useful for distributing session
 * processing work across multiple queues to reduce contention and improve scalability.</p>
 *
 * <p>Use this when you want to decouple session processing into separate threads or workers
 * that pull sessions from specific queues.</p>
 *
 * <p>Thread-safe: The use of {@link ConcurrentLinkedQueue} or {@link LinkedBlockingQueue} ensures
 * thread-safe access to each individual queue.</p>
 *
 * @since 0.6.6
 */
public final class SessionTicketsQueueManager extends BlockingQueueManager<Session> {

  /**
   * Constructs a {@code SessionTicketsQueueManager} with the specified number of queues.
   *
   * @param cacheSize the number of queues to create. Must be &gt; 0.
   * @throws IllegalArgumentException if {@code cacheSize} is less than or equal to 0
   */
  public SessionTicketsQueueManager(int cacheSize) {
    super(cacheSize, LinkedBlockingQueue::new);
  }
}
