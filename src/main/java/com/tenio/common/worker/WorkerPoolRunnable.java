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

package com.tenio.common.worker;

import com.tenio.common.logger.AbstractLogger;
import java.util.concurrent.BlockingQueue;

/**
 * Worker Poll management class.
 */
public final class WorkerPoolRunnable extends AbstractLogger implements Runnable {

  private final BlockingQueue<Runnable> taskQueue;
  private final String name;
  private final int index;
  private Thread thread;
  private boolean isStopped;

  /**
   * Create a worker pool management.
   *
   * @param name      the pool's name
   * @param index     the index set to differentiate threads
   * @param taskQueue a queue of tasks
   */
  public WorkerPoolRunnable(String name, int index, BlockingQueue<Runnable> taskQueue) {
    this.taskQueue = taskQueue;
    this.name = name;
    this.index = index;
    isStopped = false;
  }

  /**
   * Start a new thread for a worker pool.
   */
  public void run() {
    thread = Thread.currentThread();
    thread.setName(String.format("worker-%s-%d", name, index));

    while (!isStopped()) {
      try {
        Runnable runnable = taskQueue.take();
        runnable.run();
      } catch (Exception e) {
        // log or otherwise report exception,
        // but keep pool thread alive.
        error(e);
      }
    }
  }

  /**
   * Stop the current worker pool.
   */
  public synchronized void doStop() {
    isStopped = true;
    // break pool thread out of dequeue() call.
    thread.interrupt();
  }

  public synchronized boolean isStopped() {
    return isStopped;
  }
}
