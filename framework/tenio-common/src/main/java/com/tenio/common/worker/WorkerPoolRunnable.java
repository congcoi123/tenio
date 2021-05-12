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

import java.util.concurrent.BlockingQueue;

import com.tenio.common.loggers.AbstractLogger;

/**
 * @author kong
 */
// TODO: Add description
public final class WorkerPoolRunnable extends AbstractLogger implements Runnable {

	private Thread __thread;
	private final BlockingQueue<Runnable> __taskQueue;
	private final String __name;
	private final int __index;
	private boolean __isStopped;

	public WorkerPoolRunnable(String name, int index, BlockingQueue<Runnable> taskQueue) {
		__taskQueue = taskQueue;
		__name = name;
		__index = index;
		__isStopped = false;
	}

	public void run() {
		__thread = Thread.currentThread();
		__thread.setName(String.format("worker-%s-%d", __name, __index));
		while (!isStopped()) {
			try {
				Runnable runnable = (Runnable) __taskQueue.take();
				runnable.run();
			} catch (Exception e) {
				// log or otherwise report exception,
				// but keep pool thread alive.
				error(e);
			}
		}
	}

	public synchronized void doStop() {
		__isStopped = true;
		// break pool thread out of dequeue() call.
		__thread.interrupt();
	}

	public synchronized boolean isStopped() {
		return __isStopped;
	}
}
