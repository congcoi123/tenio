/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.core.command.system;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.core.bootstrap.annotation.SystemCommand;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SystemCommand(label = "TEST", description = "desc", usage = {"usage1",
    "usage2"}, isBackgroundRunning = true)
class TestSystemCommandHandler extends AbstractSystemCommandHandler {

  @Override
  public void execute(List<String> arguments) {
  }
}

@DisplayName("Unit Test Cases For AbstractSystemCommandHandler")
class AbstractSystemCommandHandlerTest {

  @Test
  @DisplayName("Test handler annotations")
  void testAnnotationBasedMethods() {
    TestSystemCommandHandler handler = new TestSystemCommandHandler();
    assertEquals("TEST", handler.getLabel());
    assertEquals("desc", handler.getDescription());
    assertArrayEquals(new String[] {"usage1", "usage2"}, handler.getUsage());
    assertTrue(handler.isRunningBackground());
  }

  @Test
  @DisplayName("Test handler getter/setter")
  void testSetAndGetCommandManager() {
    TestSystemCommandHandler handler = new TestSystemCommandHandler();
    SystemCommandManager manager = mock(SystemCommandManager.class);
    handler.setCommandManager(manager);
    assertEquals(manager, handler.getCommandManager());
  }

  @Test
  @DisplayName("When the CommandManager is set as null, it should throw exception")
  void testSetCommandManagerNullThrows() {
    TestSystemCommandHandler handler = new TestSystemCommandHandler();
    assertThrows(IllegalArgumentException.class, () -> handler.setCommandManager(null));
  }

  @Test
  @DisplayName("When the CommandManager is not set, it should throw exception")
  void testGetCommandManagerNotSetThrows() {
    TestSystemCommandHandler handler = new TestSystemCommandHandler();
    assertThrows(IllegalStateException.class, handler::getCommandManager);
  }

  @Test
  @DisplayName("Test handler toString()")
  void testToString() {
    TestSystemCommandHandler handler = new TestSystemCommandHandler();
    assertTrue(handler.toString().contains("TEST"));
    assertTrue(handler.toString().contains("desc"));
    assertTrue(handler.toString().contains("usage1"));
  }
}
