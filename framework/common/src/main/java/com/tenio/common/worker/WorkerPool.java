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

import com.tenio.common.logger.SystemLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class helps you create a worker pool.
 */
public final class WorkerPool extends SystemLogger {

  private final BlockingQueue<Runnable> taskQueue;
  private final List<WorkerPoolRunnable> runnableWorkerPools;
  private final String name;
  private boolean isStopped;

  /**
   * Create a new worker pool.
   *
   * @param name         the worker pool's name
   * @param noOfThreads  number of provided threads
   * @param maxNoOfTasks the maximum supported tasks
   */
  public WorkerPool(String name, int noOfThreads, int maxNoOfTasks) {
    taskQueue = new ArrayBlockingQueue<Runnable>(maxNoOfTasks);
    runnableWorkerPools = new ArrayList<WorkerPoolRunnable>();
    this.name = name;
    isStopped = false;

    info("CREATED NEW WORKERS",
        buildgen("Number of threads: ", noOfThreads, ", Max number of tasks: ", maxNoOfTasks));

    for (int i = 0; i < noOfThreads; i++) {
      runnableWorkerPools.add(new WorkerPoolRunnable(this.name, i, taskQueue));
    }
    for (WorkerPoolRunnable runnable : runnableWorkerPools) {
      new Thread(runnable).start();
    }
  }

  /**
   * Executes a task.
   *
   * @param task      a runnable to do a particular job
   * @param debugText a supported text using for debugging or logging
   * @throws IllegalStateException when you try to execute an unstopped task
   */
  public synchronized void execute(Runnable task, String debugText) throws IllegalStateException {
    if (isStopped) {
      throw new IllegalStateException("WorkersPool is stopped");
    }

    trace("EXECUTED A TASK", debugText);
    taskQueue.offer(task);
  }

  /**
   * Stop a task.
   */
  public synchronized void stop() {
    isStopped = true;
    for (var runnable : runnableWorkerPools) {
      runnable.doStop();
    }
  }

  /**
   * Wait until all tasks are finished.
   */
  public synchronized void waitUntilAllTasksFinished() {
    while (taskQueue.size() > 0) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        error(e);
      }
    }
  }
}
