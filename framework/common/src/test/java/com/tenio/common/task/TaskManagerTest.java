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

package com.tenio.common.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.task.implement.TaskManagerImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Task Manager")
class TaskManagerTest {

  @Test
  @DisplayName("Allow creating a new task")
  void createNewTaskShouldWork() {
    var taskManager = TaskManagerImpl.newInstance();
    taskManager.create("test-task", new DefaultTask().run());

    assertEquals(DefaultTask.DELAY_SECOND - 1, taskManager.getRemainTime("test-task"));
  }

  @Test
  @DisplayName("Allow killing a task")
  void killATaskShouldWork() {
    var taskManager = TaskManagerImpl.newInstance();
    taskManager.create("test-task", new DefaultTask().run());
    taskManager.kill("test-task");

    assertEquals(-1, taskManager.getRemainTime("test-task"));
  }

  @Test
  @DisplayName("Starting a running task should not throw any exception")
  void startARunningTaskShouldNotThrowException() {
    var taskManager = TaskManagerImpl.newInstance();
    taskManager.create("test-task", new DefaultTask().run());
    taskManager.create("test-task", new DefaultTask().run());

    assertTrue(true);
  }
}
