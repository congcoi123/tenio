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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.tenio.common.loggers.SystemLogger;

/**
 * @author kong
 */
// TODO: Add description
public final class WorkersPool extends SystemLogger {

	private final BlockingQueue<Runnable> __taskQueue;
	private final List<WorkerPoolRunnable> __runnables;
	private final String __name;
	private boolean __isStopped;

	public WorkersPool(String name, int noOfThreads, int maxNoOfTasks) {
		__taskQueue = new ArrayBlockingQueue<Runnable>(maxNoOfTasks);
		__runnables = new ArrayList<WorkerPoolRunnable>();
		__name = name;
		__isStopped = false;

		info("CREATED NEW WORKERS",
				buildgen("Number of threads: ", noOfThreads, ", Max number of tasks: ", maxNoOfTasks));

		for (int i = 0; i < noOfThreads; i++) {
			__runnables.add(new WorkerPoolRunnable(__name, i, __taskQueue));
		}
		for (WorkerPoolRunnable runnable : __runnables) {
			new Thread(runnable).start();
		}
	}

	public synchronized void execute(Runnable task, String debugText) throws Exception {
		if (__isStopped) {
			throw new IllegalStateException("WorkersPool is stopped");
		}

		trace("EXECUTED A TASK", debugText);
		__taskQueue.offer(task);
	}

	public synchronized void stop() {
		__isStopped = true;
		for (WorkerPoolRunnable runnable : __runnables) {
			runnable.doStop();
		}
	}

	public synchronized void waitUntilAllTasksFinished() {
		while (__taskQueue.size() > 0) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				error(e);
			}
		}
	}

}
