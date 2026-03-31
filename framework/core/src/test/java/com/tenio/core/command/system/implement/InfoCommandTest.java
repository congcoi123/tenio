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

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For InfoCommand")
class InfoCommandTest {

  private InfoCommand infoCommand;

  @BeforeEach
  void setUp() {
    infoCommand = new InfoCommand();
  }

  @Test
  @DisplayName("getLabel returns 'info'")
  void testGetLabel() {
    assertEquals("info", infoCommand.getLabel());
  }

  @Test
  @DisplayName("getDescription returns the info command description")
  void testGetDescription() {
    assertEquals("Provides brief information about players and rooms on the server",
        infoCommand.getDescription());
  }

  @Test
  @DisplayName("getUsage returns usage array with player and room")
  void testGetUsage() {
    assertArrayEquals(new String[]{"player", "room"}, infoCommand.getUsage());
  }

  @Test
  @DisplayName("isRunningBackground is false")
  void testIsRunningBackgroundIsFalse() {
    assertFalse(infoCommand.isRunningBackground());
  }

  @Test
  @DisplayName("execute with 'player' action does not throw")
  void testExecutePlayerActionDoesNotThrow() {
    assertDoesNotThrow(() -> infoCommand.execute(List.of("player")));
  }

  @Test
  @DisplayName("execute with 'room' action does not throw")
  void testExecuteRoomActionDoesNotThrow() {
    assertDoesNotThrow(() -> infoCommand.execute(List.of("room")));
  }

  @Test
  @DisplayName("execute with unknown action prints not-available message")
  void testExecuteUnknownActionDoesNotThrow() {
    assertDoesNotThrow(() -> infoCommand.execute(List.of("unknown")));
  }
}
