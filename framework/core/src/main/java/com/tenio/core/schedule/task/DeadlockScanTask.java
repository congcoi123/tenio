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

package com.tenio.core.schedule.task;

import com.tenio.core.configuration.CoreConfiguration;
import com.tenio.core.event.implement.EventManager;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * To detect deadlock in period time. You can configure this time in your own
 * configurations, see {@link CoreConfiguration}
 */
public final class DeadlockScanTask extends AbstractTask {

  private final ThreadMXBean threadMxBean;

  private DeadlockScanTask(EventManager eventManager) {
    super(eventManager);

    threadMxBean = ManagementFactory.getThreadMXBean();
  }

  public static DeadlockScanTask newInstance(EventManager eventManager) {
    return new DeadlockScanTask(eventManager);
  }

  @Override
  public ScheduledFuture<?> run() {
    return Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
        this::checkForDeadlockedThreads, 0, interval, TimeUnit.SECONDS);
  }

  private void checkForDeadlockedThreads() {
    long[] threadIds = findDeadlockedThreads();

    if (Objects.nonNull(threadIds) && threadIds.length > 0) {
      var threads = new Thread[threadIds.length];

      var logger = buildgen("\n");

      for (int i = 0; i < threads.length; ++i) {
        var threadInfo = threadMxBean.getThreadInfo(threadIds[i]);

        logger.append("Deadlocked Thread\n");
        logger.append("\t").append("Thread Id : ").append(threadInfo.getThreadId()).append("\n");
        logger.append("\t").append("Thread Name : ").append(threadInfo.getThreadName())
            .append("\n");
        logger.append("\t").append("LockName : ").append(threadInfo.getLockName()).append("\n");
        logger.append("\t").append("LockOwnerId : ").append(threadInfo.getLockOwnerId())
            .append("\n");
        logger.append("\t").append("LockOwnerName : ").append(threadInfo.getLockOwnerName())
            .append("\n");

        try {
          threads[i] = findMatchingThread(threadInfo);
        } catch (IllegalStateException e) {
          error(e);
        }
      }

      info("DEADLOCKED THREAD DETECTOR", logger);
    }
  }

  private long[] findDeadlockedThreads() {
    return threadMxBean.isSynchronizerUsageSupported() ? threadMxBean.findDeadlockedThreads()
        : threadMxBean.findMonitorDeadlockedThreads();
  }

  private Thread findMatchingThread(ThreadInfo threadInfo) throws IllegalStateException {
    var iterator = Thread.getAllStackTraces().keySet().iterator();

    Thread thread;
    do {
      if (!iterator.hasNext()) {
        throw new IllegalStateException("Deadlocked Thread not found");
      }

      thread = iterator.next();
    } while (thread.getId() != threadInfo.getThreadId());

    return thread;
  }
}
