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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Worker Pool Runnable Utility")
class WorkerPoolRunnableTest {

  @Test
  void testConstructor() {
    assertFalse(
        (new WorkerPoolRunnable("Name", 1, new ArrayBlockingQueue<>(3))).isStopped());
  }

  @Test
  void testRunAndStop() throws InterruptedException {
    BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(3);
    WorkerPoolRunnable worker = new WorkerPoolRunnable("Test", 1, taskQueue);
    Thread thread = new Thread(worker);
    thread.start();

    assertFalse(worker.isStopped());
    
    // Test normal execution
    boolean[] executed = {false};
    taskQueue.put(() -> executed[0] = true);
    Thread.sleep(100);
    assertTrue(executed[0]);

    // Test exception handling in run loop
    taskQueue.put(() -> {
        throw new RuntimeException("Test Exception");
    });
    Thread.sleep(100);
    // Thread should still be alive
    assertTrue(thread.isAlive());

    worker.doStop();
    assertTrue(worker.isStopped());
    thread.join(1000);
    assertFalse(thread.isAlive());
  }

  @Test
  void testExceptionHandlingWithErrorLogging() throws InterruptedException {
    Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.ERROR);
    try {
      BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(3);
      WorkerPoolRunnable worker = new WorkerPoolRunnable("ErrTest", 0, taskQueue);
      Thread thread = new Thread(worker);
      thread.start();

      taskQueue.put(() -> {
        throw new RuntimeException("Logging Test Exception");
      });
      Thread.sleep(100);
      assertTrue(thread.isAlive());

      worker.doStop();
      thread.join(500);
    } finally {
      Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.OFF);
    }
  }
}
