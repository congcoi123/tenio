/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

import static org.mockito.Mockito.mock;

import com.tenio.common.custom.DisabledTestFindingSolution;
import org.junit.jupiter.api.Test;

class WorkerPoolTest {
  @Test
  void testConstructor() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     WorkerPool.taskQueue
    //     WorkerPool.runnableWorkerPools
    //     WorkerPool.name
    //     WorkerPool.isStopped
    //     AbstractLogger.logger

    new WorkerPool("Name", 1, 3);

  }

  @Test
  void testConstructor2() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     WorkerPool.taskQueue
    //     WorkerPool.runnableWorkerPools
    //     WorkerPool.name
    //     WorkerPool.isStopped
    //     AbstractLogger.logger

    new WorkerPool("CREATED NEW WORKERS", 1, 3);

  }

  @Test
  void testConstructor3() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     WorkerPool.taskQueue
    //     WorkerPool.runnableWorkerPools
    //     WorkerPool.name
    //     WorkerPool.isStopped
    //     AbstractLogger.logger

    new WorkerPool("Name", Double.SIZE, 3);

  }

  @Test
  void testExecute() throws IllegalStateException {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by execute(Runnable, String)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    (new WorkerPool("Name", 1, 3)).execute(mock(Runnable.class), "Debug Text");
  }

  @Test
  void testExecute2() throws IllegalStateException {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by execute(Runnable, String)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    (new WorkerPool("com.tenio.common.worker.WorkerPoolRunnable", 1, 3)).execute(
        mock(Runnable.class),
        "EXECUTED A TASK");
  }

  @Test
  @DisabledTestFindingSolution
  void testStop() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     WorkerPool.isStopped
    //     WorkerPool.name
    //     WorkerPool.runnableWorkerPools
    //     WorkerPool.taskQueue

    (new WorkerPool("Name", 3, 1)).stop();
  }

  @Test
  void testWaitUntilAllTasksFinished() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     WorkerPool.isStopped
    //     WorkerPool.name
    //     WorkerPool.runnableWorkerPools
    //     WorkerPool.taskQueue

    (new WorkerPool("Name", 1, 3)).waitUntilAllTasksFinished();
  }

  @Test
  void testWaitUntilAllTasksFinished2() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     WorkerPool.isStopped
    //     WorkerPool.name
    //     WorkerPool.runnableWorkerPools
    //     WorkerPool.taskQueue

    (new WorkerPool("com.tenio.common.worker.WorkerPoolRunnable", 1,
        3)).waitUntilAllTasksFinished();
  }

  @Test
  void testWaitUntilAllTasksFinished3() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     WorkerPool.isStopped
    //     WorkerPool.name
    //     WorkerPool.runnableWorkerPools
    //     WorkerPool.taskQueue

    (new WorkerPool("Name", 1, 1)).waitUntilAllTasksFinished();
  }

  @Test
  void testWaitUntilAllTasksFinished4() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     WorkerPool.isStopped
    //     WorkerPool.name
    //     WorkerPool.runnableWorkerPools
    //     WorkerPool.taskQueue

    (new WorkerPool("Name", 2, 1)).waitUntilAllTasksFinished();
  }
}

