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
package com.tenio.core.schedule.tasks;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.tenio.core.configuration.CoreConfiguration;
import com.tenio.core.event.implement.EventManager;

/**
 * To detect deadlock in period time. You can configure this time in your own
 * configurations, see {@link CoreConfiguration}
 */
public final class DeadlockScanTask extends AbstractTask {

	private final ThreadMXBean __threadBean;

	public static DeadlockScanTask newInstance(EventManager eventManager) {
		return new DeadlockScanTask(eventManager);
	}

	private DeadlockScanTask(EventManager eventManager) {
		super(eventManager);

		__threadBean = ManagementFactory.getThreadMXBean();
	}

	@Override
	public ScheduledFuture<?> run() {
		return Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

			__checkForDeadlockedThreads();

		}, 0, __interval, TimeUnit.SECONDS);
	}

	private void __checkForDeadlockedThreads() {
		long[] threadIds = __findDeadlockedThreads();

		if (threadIds != null && threadIds.length > 0) {
			Thread[] threads = new Thread[threadIds.length];

			var logger = buildgen("\n");

			for (int i = 0; i < threads.length; ++i) {
				ThreadInfo threadInfo = __threadBean.getThreadInfo(threadIds[i]);

				logger.append("Deadlocked Thread\n");
				logger.append("\t").append("Thread Id : ").append(threadInfo.getThreadId()).append("\n");
				logger.append("\t").append("Thread Name : ").append(threadInfo.getThreadName()).append("\n");
				logger.append("\t").append("LockName : " + threadInfo.getLockName()).append("\n");
				logger.append("\t").append("LockOwnerId : " + threadInfo.getLockOwnerId()).append("\n");
				logger.append("\t").append("LockOwnerName : " + threadInfo.getLockOwnerName()).append("\n");

				try {
					threads[i] = __findMatchingThread(threadInfo);
				} catch (IllegalStateException e) {
					error(e);
				}
			}

			info("DEADLOCKED THREAD DETECTOR", logger);
		}

	}

	private long[] __findDeadlockedThreads() {
		return __threadBean.isSynchronizerUsageSupported() ? __threadBean.findDeadlockedThreads()
				: __threadBean.findMonitorDeadlockedThreads();
	}

	private Thread __findMatchingThread(ThreadInfo threadInfo) throws IllegalStateException {
		var threadIter = Thread.getAllStackTraces().keySet().iterator();

		Thread thread;
		do {
			if (!threadIter.hasNext()) {
				throw new IllegalStateException("Deadlocked Thread not found");
			}

			thread = (Thread) threadIter.next();
		} while (thread.getId() != threadInfo.getThreadId());

		return thread;
	}

}
