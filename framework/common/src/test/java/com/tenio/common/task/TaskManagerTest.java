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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.task.implement.TaskManagerImpl;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Task Manager")
class TaskManagerTest {

  @Test
  @DisplayName("Allow creating a new task")
  void createNewTaskShouldWork() {
    var taskManager = TaskManagerImpl.newInstance();
    taskManager.create("test-task", new DefaultTask());

    assertTrue(taskManager.getRemainTime("test-task") > 0);
  }

  @Test
  @DisplayName("Allow killing a task")
  void killATaskShouldWork() {
    var taskManager = TaskManagerImpl.newInstance();
    taskManager.create("test-task", new DefaultTask());
    taskManager.kill("test-task");

    assertEquals(-1, taskManager.getRemainTime("test-task"));
  }

  @Test
  @DisplayName("Allow clearing all tasks")
  void clearAllTasksShouldWork() {
    var taskManager = TaskManagerImpl.newInstance();
    taskManager.create("test-task-1", new DefaultTask());
    taskManager.create("test-task-2", new DefaultTask());
    taskManager.clear();

    assertEquals(-1, taskManager.getRemainTime("test-task-1"));
    assertEquals(-1, taskManager.getRemainTime("test-task-2"));
  }

  @Test
  @DisplayName("Starting a running task should not throw any exception")
  void startARunningTaskShouldNotThrowException() {
    var taskManager = TaskManagerImpl.newInstance();
    taskManager.create("test-task", new DefaultTask());
    taskManager.create("test-task", new DefaultTask());

    assertTrue(true);
  }

  @Test
  @DisplayName("Killing a task that does not exist should do nothing")
  void killNonExistentTaskShouldDoNothing() {
    var taskManager = TaskManagerImpl.newInstance();
    assertDoesNotThrow(() -> taskManager.kill("nonExistent"));
    assertEquals(-1, taskManager.getRemainTime("nonExistent"));
  }

  @Test
  @DisplayName("Task operations should cover logging branches when logging is enabled")
  void taskOperationsWithLoggingEnabled() {
    Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.INFO);
    try {
      var taskManager = TaskManagerImpl.newInstance();
      taskManager.create("t1", new DefaultTask());
      taskManager.kill("t1");
      taskManager.create("t2", new DefaultTask());
      taskManager.create("t2", new DefaultTask());
      taskManager.clear();
    } finally {
      Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.OFF);
    }
  }
}
