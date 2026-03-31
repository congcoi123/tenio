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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
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
    assertEquals("Allows stopping or restarting the server", serverCommand.getDescription());
  }

  @Test
  @DisplayName("getUsage returns stop and restart options")
  void testGetUsage() {
    assertArrayEquals(new String[]{"stop", "restart"}, serverCommand.getUsage());
  }

  @Test
  @DisplayName("isRunningBackground is true")
  void testIsRunningBackgroundIsTrue() {
    assertTrue(serverCommand.isRunningBackground());
  }

  @Test
  @DisplayName("execute with 'stop' does not throw")
  void testExecuteStopDoesNotThrow() {
    assertDoesNotThrow(() -> serverCommand.execute(List.of("stop")));
  }

  @Test
  @DisplayName("execute with 'restart' does not throw")
  void testExecuteRestartDoesNotThrow() {
    assertDoesNotThrow(() -> serverCommand.execute(List.of("restart")));
  }

  @Test
  @DisplayName("execute with empty args does not throw")
  void testExecuteEmptyArgsDoesNotThrow() {
    assertDoesNotThrow(() -> serverCommand.execute(Collections.emptyList()));
  }
}
