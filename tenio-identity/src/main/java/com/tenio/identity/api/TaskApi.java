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
package com.tenio.identity.api;

import java.util.concurrent.ScheduledFuture;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.task.ITaskManager;

/**
 * This class provides you a necessary interface for managing tasks.
 * 
 * @see ITaskManager
 * 
 * @author kong
 * 
 */
public final class TaskApi extends AbstractLogger {

	private final ITaskManager __taskManager;

	public TaskApi(ITaskManager taskManager) {
		__taskManager = taskManager;
	}

	/**
	 * Create a new task.
	 * 
	 * @param id   the unique id for management
	 * @param task the running task, see {@link ScheduledFuture}
	 */
	public void run(String id, ScheduledFuture<?> task) {
		__taskManager.create(id, task);
	}

	/**
	 * Kill or stop a running task.
	 * 
	 * @param id the unique id
	 */
	public void kill(String id) {
		__taskManager.kill(id);
	}

	/**
	 * Check if the desired task is running or not.
	 * 
	 * @param id the task's id
	 * @return <b>true</b> if this task is running
	 */
	public boolean isRunningTask(String id) {
		return getTaskRemainTime(id) > 0 ? true : false;
	}

	/**
	 * Retrieve the remain time of one task.
	 * 
	 * @param id the unique for retrieving the desired task
	 * @return the left time
	 */
	public int getTaskRemainTime(String id) {
		return __taskManager.getRemainTime(id);
	}

}
