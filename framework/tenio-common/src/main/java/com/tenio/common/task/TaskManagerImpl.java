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
package com.tenio.common.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.exceptions.RunningScheduledTaskException;
import com.tenio.common.loggers.SystemLogger;

/**
 * This class uses Java scheduler ({@link ScheduledFuture}) to manage your
 * tasks. The scheduler is used to schedule a thread or task that executes at a
 * certain period of time or periodically at a fixed interval. It's useful when
 * you want to create a time counter before starting a match or send messages
 * periodically for one player.
 * 
 * @see TaskManager
 * 
 * @author kong
 * 
 */
@ThreadSafe
public final class TaskManagerImpl extends SystemLogger implements TaskManager {

	/**
	 * A list of tasks in the server
	 */
	private final Map<String, ScheduledFuture<?>> __tasks;

	public static TaskManager newInstance() {
		return new TaskManagerImpl();
	}

	private TaskManagerImpl() {
		__tasks = new ConcurrentHashMap<String, ScheduledFuture<?>>();
	}

	@Override
	public void create(String id, ScheduledFuture<?> task) {
		if (__tasks.containsKey(id)) {
			try {
				if (!__tasks.get(id).isDone() || !__tasks.get(id).isCancelled()) {
					throw new RunningScheduledTaskException();
				}
			} catch (RunningScheduledTaskException e) {
				error(e, "task id: ", id);
				return;
			}
		}

		__tasks.put(id, task);
		info("RUN TASK", buildgen(id, " >Time left> ", task.getDelay(TimeUnit.SECONDS), " seconds"));
	}

	@Override
	public void kill(String id) {
		if (__tasks.containsKey(id)) {
			info("KILLED TASK", id);
			__tasks.remove(id);
			var task = __tasks.get(id);
			if (task != null && (!task.isDone() || !task.isCancelled())) {
				task.cancel(true);
			}
		}
	}

	@Override
	public void clear() {
		__tasks.forEach((id, task) -> {
			info("KILLED TASK", id);
			if (task != null && (!task.isDone() || !task.isCancelled())) {
				task.cancel(true);
			}
		});
		__tasks.clear();
	}

	@Override
	public int getRemainTime(String id) {
		var task = __tasks.get(id);
		if (task != null) {
			return (int) task.getDelay(TimeUnit.SECONDS);
		}
		return -1;
	}

}
