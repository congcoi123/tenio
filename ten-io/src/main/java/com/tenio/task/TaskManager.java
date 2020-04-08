/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.GuardedBy;

import com.tenio.exception.RunningScheduledTaskException;
import com.tenio.logger.AbstractLogger;

/**
 * 
 * See {@link ITaskManager}
 * 
 * @author kong
 * 
 */
public final class TaskManager extends AbstractLogger implements ITaskManager {

	/**
	 * A list of tasks in the server
	 */
	@GuardedBy("this")
	private final Map<String, ScheduledFuture<?>> __tasks = new HashMap<String, ScheduledFuture<?>>();

	@Override
	public synchronized void create(String id, ScheduledFuture<?> task) {
		if (__tasks.containsKey(id)) {
			try {
				if (!__tasks.get(id).isDone() || !__tasks.get(id).isCancelled()) {
					throw new RunningScheduledTaskException();
				}
			} catch (RunningScheduledTaskException e) {
				error("TASK", id, e);
				return;
			}
		}

		__tasks.put(id, task);
		info("RUN TASK", buildgen(id, " >Time left> ", task.getDelay(TimeUnit.SECONDS), " seconds"));
	}

	@Override
	public synchronized void kill(String id) {
		var task = __tasks.get(id);
		if (task != null) {
			task.cancel(true);
			info("KILLED TASK", buildgen(id, " >Time left> ", task.getDelay(TimeUnit.SECONDS), " seconds"));
			__tasks.remove(id);
		}
	}

	@Override
	public synchronized int getRemainTime(String id) {
		var task = __tasks.get(id);
		if (task != null) {
			return (int) task.getDelay(TimeUnit.SECONDS);
		}
		return -1;
	}

}
