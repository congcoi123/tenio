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
package com.tenio.common.api;

import java.util.concurrent.ScheduledFuture;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.task.TaskManager;

/**
 * This class provides you a necessary interface for managing tasks.
 * 
 * @see TaskManager
 * 
 * @author kong
 * 
 */
@ThreadSafe
public final class TaskApi extends AbstractLogger {

	private final TaskManager __taskManager;

	public TaskApi(TaskManager taskManager) {
		__taskManager = taskManager;
	}

	/**
	 * Create a new task.
	 * 
	 * @param taskId   the unique taskId for management
	 * @param task the running task, see {@link ScheduledFuture}
	 */
	public void run(String taskId, ScheduledFuture<?> task) {
		__taskManager.create(taskId, task);
	}

	/**
	 * Kill or stop a running task.
	 * 
	 * @param taskId the unique taskId
	 */
	public void kill(String taskId) {
		__taskManager.kill(taskId);
	}

	/**
	 * Check if the desired task is running or not.
	 * 
	 * @param taskId the task's taskId
	 * @return <b>true</b> if this task is running
	 */
	public boolean isRunningTask(String taskId) {
		return getTaskRemainTime(taskId) > 0 ? true : false;
	}

	/**
	 * Retrieve the remain time of one task.
	 * 
	 * @param taskId the unique for retrieving the desired task
	 * @return the left time
	 */
	public int getTaskRemainTime(String taskId) {
		return __taskManager.getRemainTime(taskId);
	}

}
