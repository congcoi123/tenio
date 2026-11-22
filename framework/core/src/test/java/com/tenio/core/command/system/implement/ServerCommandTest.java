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

package com.tenio.core.command.system.implement;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tenio.core.utility.CommandUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ServerCommand")
class ServerCommandTest {

  private ServerCommand serverCommand;

  @BeforeEach
  void setUp() {
    serverCommand = new ServerCommand();
  }

  @Test
  @DisplayName("getLabel returns 'server'")
  void testGetLabel() {
    assertEquals("server", serverCommand.getLabel());
  }

  @Test
  @DisplayName("getDescription returns the server command description")
  void testGetDescription() {
    assertEquals("Stops the server or requests a restart from its supervisor", serverCommand.getDescription());
  }

  @Test
  @DisplayName("getUsage returns stop and restart options")
  void testGetUsage() {
    assertArrayEquals(new String[]{"stop", "restart"}, serverCommand.getUsage());
  }

  @Test
  @DisplayName("isRunningBackground is false")
  void testIsRunningBackgroundIsTrue() {
    assertFalse(serverCommand.isRunningBackground());
  }

  @Test
  void stopShutsDownAndDoesNotExitForRestart() throws Exception {
    var stopped = new CountDownLatch(1);
    var exited = new AtomicBoolean();
    var command = new ServerCommand(stopped::countDown, () -> true, code -> exited.set(true));
    command.execute(List.of("stop"));
    assertTrue(stopped.await(3, TimeUnit.SECONDS));
    assertFalse(exited.get());
  }

  @Test
  void supervisedRestartShutsDownBeforeSignalingTheSupervisor() throws Exception {
    var actions = new LinkedBlockingQueue<String>();
    var command = new ServerCommand(() -> actions.add("shutdown"), () -> true,
        code -> actions.add("exit " + code));
    command.execute(List.of("restart"));
    assertEquals("shutdown", actions.poll(3, TimeUnit.SECONDS));
    assertEquals("exit 75", actions.poll(3, TimeUnit.SECONDS));
  }

  @Test
  void restartWithoutSupervisorKeepsTheServerRunning() {
    var output = new ArrayList<String>();
    var command = new ServerCommand(() -> { throw new AssertionError("Unexpected shutdown"); },
        () -> false, code -> { throw new AssertionError("Unexpected exit"); });
    CommandUtility.INSTANCE.withOutput(output::add,
        () -> command.execute(List.of("restart")));
    assertTrue(output.getFirst().contains("supervisor"));
  }
}
