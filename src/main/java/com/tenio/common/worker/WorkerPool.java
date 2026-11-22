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

package com.tenio.common.worker;

import com.tenio.common.logger.SystemLogger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class helps you create a worker pool.
 */
public final class WorkerPool extends SystemLogger {

  private final ThreadPoolExecutor executor;

  /**
   * Creates a new worker pool.
   *
   * @param name         the worker pool's name
   * @param noOfThreads  number of provided threads
   * @param maxNoOfTasks the maximum supported tasks
   */
  public WorkerPool(String name, int noOfThreads, int maxNoOfTasks) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Worker pool name must not be blank");
    }
    if (noOfThreads <= 0 || maxNoOfTasks <= 0) {
      throw new IllegalArgumentException("Thread and task limits must be positive");
    }
    var threadIndex = new AtomicInteger();
    ThreadFactory threadFactory = runnable -> {
      var thread = new Thread(runnable, String.format("worker-%s-%d", name, threadIndex.getAndIncrement()));
      thread.setDaemon(true);
      thread.setUncaughtExceptionHandler((ignored, cause) -> error(cause));
      return thread;
    };
    executor = new ThreadPoolExecutor(noOfThreads, noOfThreads, 0L,
        TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(maxNoOfTasks), threadFactory,
        new ThreadPoolExecutor.AbortPolicy());

    if (isInfoEnabled()) {
      info("CREATED NEW WORKERS",
          buildgen("Number of threads: ", noOfThreads, ", Max number of tasks: ", maxNoOfTasks));
    }

    executor.prestartAllCoreThreads();
  }

  /**
   * Executes a task.
   *
   * @param task      a runnable to do a particular job
   * @param debugText a supported text using for debugging or logging
   * @throws IllegalStateException when you try to execute an unstopped task
   */
  public void execute(Runnable task, String debugText) throws IllegalStateException {
    if (executor.isShutdown()) {
      throw new IllegalStateException("WorkersPool is stopped");
    }

    if (isTraceEnabled()) {
      trace("EXECUTED A TASK", debugText);
    }

    try {
      executor.execute(java.util.Objects.requireNonNull(task, "task"));
    } catch (RejectedExecutionException exception) {
      throw new RejectedExecutionException("Worker queue is full or stopped", exception);
    }
  }

  /**
   * Stop a task.
   */
  public void stop() {
    executor.shutdownNow();
  }
}
