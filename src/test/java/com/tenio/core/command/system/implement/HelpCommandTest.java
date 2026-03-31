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
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.tenio.core.command.system.SystemCommandManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For HelpCommand")
class HelpCommandTest {

  private HelpCommand helpCommand;
  private SystemCommandManager manager;

  @BeforeEach
  void setUp() {
    helpCommand = new HelpCommand();
    manager = new SystemCommandManager();
    manager.registerCommand("help", helpCommand);
    helpCommand.setCommandManager(manager);
  }

  @Test
  @DisplayName("getLabel returns 'help'")
  void testGetLabel() {
    assertEquals("help", helpCommand.getLabel());
  }

  @Test
  @DisplayName("getDescription returns the help command description")
  void testGetDescription() {
    assertEquals("Shows all supporting commands", helpCommand.getDescription());
  }

  @Test
  @DisplayName("getUsage returns usage array")
  void testGetUsage() {
    assertArrayEquals(new String[]{"[<command>,<command>,<command>]"}, helpCommand.getUsage());
  }

  @Test
  @DisplayName("isRunningBackground is false")
  void testIsRunningBackgroundIsFalse() {
    assertFalse(helpCommand.isRunningBackground());
  }

  @Test
  @DisplayName("execute with empty args lists all registered commands without throwing")
  void testExecuteNoArgsListsAllCommands() {
    assertDoesNotThrow(() -> helpCommand.execute(Collections.emptyList()));
  }

  @Test
  @DisplayName("execute with valid label shows that command's details")
  void testExecuteWithValidLabelShowsDetails() {
    assertDoesNotThrow(() -> helpCommand.execute(new ArrayList<>(List.of("help"))));
  }

  @Test
  @DisplayName("execute with nonexistent label prints not-found message")
  void testExecuteWithNonexistentLabelPrintsNotFound() {
    assertDoesNotThrow(() -> helpCommand.execute(new ArrayList<>(List.of("nonexistent"))));
  }

  @Test
  @DisplayName("execute with multiple valid labels shows each command's details")
  void testExecuteWithMultipleValidLabels() {
    var serverCommand = new ServerCommand();
    manager.registerCommand("server", serverCommand);
    serverCommand.setCommandManager(manager);
    assertDoesNotThrow(() -> helpCommand.execute(new ArrayList<>(List.of("help,server"))));
  }
}
