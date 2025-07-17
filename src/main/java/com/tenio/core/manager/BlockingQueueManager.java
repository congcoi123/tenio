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

package com.tenio.core.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

/**
 * A generic manager for distributing elements across a fixed number of
 * {@link BlockingQueue}s based on a long identifier (e.g., an entity or session ID).
 *
 * <p>This class is useful for load balancing or concurrent processing where
 * items (of type {@code T}) are grouped by an ID and each group is assigned
 * to a queue deterministically using {@code id % cacheSize}.</p>
 *
 * <p>Thread-safe: Uses {@link LinkedBlockingQueue}s, which are thread-safe
 * and suitable for high throughput scenarios.</p>
 *
 * <p>Typical use cases include event dispatching, game entity updates,
 * session handling, or message routing across multiple worker threads.</p>
 *
 * @param <T> the type of elements stored in the queues
 * @since 0.6.6
 */
public class BlockingQueueManager<T> implements Manager {

  /**
   * List of internal queues, each holding elements of type {@code T}.
   */
  private final List<BlockingQueue<T>> queues;
  /**
   * Total number of queues created.
   */
  private final int cacheSize;

  /**
   * Constructs a manager with custom queue instances using a supplier (factory).
   *
   * @param cacheSize     the number of queues to manage
   * @param queueSupplier a supplier (factory) to create instances of {@link Queue}
   * @throws IllegalArgumentException if {@code cacheSize <= 0}
   */
  public BlockingQueueManager(int cacheSize, Supplier<BlockingQueue<T>> queueSupplier) {
    if (cacheSize <= 0) {
      throw new IllegalArgumentException("cacheSize must be greater than 0");
    }
    this.cacheSize = cacheSize;
    queues = new ArrayList<>(this.cacheSize);
    for (int i = 0; i < this.cacheSize; i++) {
      queues.add(queueSupplier.get());
    }
  }

  /**
   * Retrieves the queue associated with the given identifier.
   *
   * <p>The queue is selected deterministically using {@code id % cacheSize}, ensuring
   * that the same ID always maps to the same queue. Useful for partitioned processing.</p>
   *
   * @param elementId the identifier used to determine which queue to return
   * @return the {@link BlockingQueue} assigned to the given {@code elementId}
   */
  public BlockingQueue<T> getQueueByElementId(long elementId) {
    int index = Math.floorMod(elementId, cacheSize);
    return queues.get(index);
  }

  /**
   * Retrieves the queue associated with the given index.
   *
   * @param index the identifier used to determine which queue to return
   * @return the {@link BlockingQueue} assigned to the given {@code index}
   */
  public BlockingQueue<T> getQueueByIndex(int index) {
    return queues.get(index);
  }

  /**
   * Cleanup all internal queues.
   */
  public void clear() {
    for (var queue : queues) {
      queue.clear();
    }
  }

  /**
   * Returns the cache size.
   *
   * @return the cache size
   */
  public int getCacheSize() {
    return cacheSize;
  }
}
