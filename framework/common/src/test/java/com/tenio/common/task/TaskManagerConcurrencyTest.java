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

package com.tenio.common.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.common.task.implement.TaskManagerImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TaskManagerConcurrencyTest {

  private final TaskManager manager = TaskManagerImpl.newInstance();
  private final ExecutorService executor = Executors.newFixedThreadPool(2);
  private final List<ControlledTask> createdTasks = new ArrayList<>();

  @AfterEach
  void cleanup() throws InterruptedException {
    createdTasks.forEach(task -> task.proceed.countDown());
    executor.shutdownNow();
    assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    manager.clear();
  }

  @Test
  void killingAnotherIdDoesNotCancelStartingTask() throws Exception {
    var starting = task(true);
    var future = start("first", starting);
    var other = task(false);
    manager.create("second", other);

    manager.kill("second");
    finish(starting, future);

    assertEquals(30, manager.getRemainTime("first"));
    assertEquals(0, starting.shutdownCalls.get());
    assertEquals(1, other.shutdownCalls.get());
  }

  @Test
  void duplicateIdDoesNotRunWhileFirstTaskIsStarting() throws Exception {
    var first = task(true);
    var future = start("same", first);
    var duplicate = task(false);

    manager.create("same", duplicate);
    finish(first, future);

    assertEquals(1, first.runCalls.get());
    assertEquals(0, duplicate.runCalls.get());
    assertEquals(30, manager.getRemainTime("same"));
  }

  @Test
  void killingStartingTaskAllowsReplacementWithoutCancellingIt() throws Exception {
    var first = task(true);
    var future = start("same", first);

    manager.kill("same");
    var replacement = task(false);
    manager.create("same", replacement);
    finish(first, future);

    assertEquals(1, first.shutdownCalls.get());
    assertEquals(0, replacement.shutdownCalls.get());
    assertEquals(30, manager.getRemainTime("same"));
  }

  @Test
  void clearCancelsAllStartsAlreadyInProgress() throws Exception {
    var first = task(true);
    var second = task(true);
    var firstFuture = start("first", first);
    var secondFuture = start("second", second);

    manager.clear();
    finish(first, firstFuture);
    finish(second, secondFuture);

    assertEquals(-1, manager.getRemainTime("first"));
    assertEquals(-1, manager.getRemainTime("second"));
    assertEquals(1, first.shutdownCalls.get());
    assertEquals(1, second.shutdownCalls.get());

    manager.create("first", task(false));
    assertEquals(30, manager.getRemainTime("first"));
  }

  @Test
  void failedStartupReleasesIdAndStopsPartialTask() {
    var failed = mock(Task.class);
    var failure = new IllegalStateException("startup failed");
    doThrow(failure).when(failed).run();

    assertSame(failure,
        assertThrows(IllegalStateException.class, () -> manager.create("same", failed)));
    verify(failed).shutdown();
    manager.create("same", task(false));
    assertEquals(30, manager.getRemainTime("same"));
  }

  @Test
  void missingSchedulerReleasesIdAndStopsPartialTask() {
    var failed = mock(Task.class);

    assertThrows(NullPointerException.class, () -> manager.create("same", failed));
    verify(failed).shutdown();
    manager.create("same", task(false));
    assertEquals(30, manager.getRemainTime("same"));
  }

  @Test
  void startupFailureKeepsOriginalCauseIfShutdownAlsoFails() {
    var failed = mock(Task.class);
    var startupFailure = new IllegalStateException("startup failed");
    var shutdownFailure = new IllegalArgumentException("shutdown failed");
    doThrow(startupFailure).when(failed).run();
    doThrow(shutdownFailure).when(failed).shutdown();

    var actual = assertThrows(IllegalStateException.class,
        () -> manager.create("same", failed));

    assertSame(startupFailure, actual);
    assertEquals(1, actual.getSuppressed().length);
    assertSame(shutdownFailure, actual.getSuppressed()[0]);
    manager.create("same", task(false));
    assertEquals(30, manager.getRemainTime("same"));
  }

  @Test
  void finishedTaskCanBeReplaced() {
    var first = task(false);
    manager.create("same", first);
    when(first.scheduler.isDone()).thenReturn(true);
    var replacement = task(false);

    manager.create("same", replacement);

    assertEquals(1, replacement.runCalls.get());
    assertEquals(30, manager.getRemainTime("same"));
  }

  private ControlledTask task(boolean blocked) {
    var task = new ControlledTask(blocked);
    createdTasks.add(task);
    return task;
  }

  private Future<?> start(String id, ControlledTask task) throws InterruptedException {
    var future = executor.submit(() -> manager.create(id, task));
    assertTrue(task.started.await(5, TimeUnit.SECONDS));
    return future;
  }

  private void finish(ControlledTask task, Future<?> future) throws Exception {
    task.proceed.countDown();
    future.get(5, TimeUnit.SECONDS);
  }

  private static final class ControlledTask implements Task {

    private final CountDownLatch started = new CountDownLatch(1);
    private final CountDownLatch proceed;
    private final ScheduledFuture<?> scheduler = mock(ScheduledFuture.class);
    private final AtomicInteger runCalls = new AtomicInteger();
    private final AtomicInteger shutdownCalls = new AtomicInteger();

    private ControlledTask(boolean blocked) {
      proceed = new CountDownLatch(blocked ? 1 : 0);
      when(scheduler.getDelay(TimeUnit.SECONDS)).thenReturn(30L);
    }

    @Override
    public void run() {
      runCalls.incrementAndGet();
      started.countDown();
      try {
        assertTrue(proceed.await(5, TimeUnit.SECONDS));
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(exception);
      }
    }

    @Override
    public ScheduledFuture<?> getScheduler() {
      return scheduler;
    }

    @Override
    public void shutdown() {
      shutdownCalls.incrementAndGet();
    }
  }
}
