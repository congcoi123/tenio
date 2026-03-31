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

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For UnBanAddressCommand")
class UnBanAddressCommandTest {

  private UnBanAddressCommand unBanAddressCommand;

  @BeforeEach
  void setUp() {
    unBanAddressCommand = new UnBanAddressCommand();
  }

  @Test
  @DisplayName("getLabel returns 'unban'")
  void testGetLabel() {
    assertEquals("unban", unBanAddressCommand.getLabel());
  }

  @Test
  @DisplayName("getDescription returns the unban command description")
  void testGetDescription() {
    assertEquals("Allows removing banned Ip addresses from the ban list",
        unBanAddressCommand.getDescription());
  }

  @Test
  @DisplayName("getUsage returns the usage pattern")
  void testGetUsage() {
    assertArrayEquals(new String[]{"[<address>,<command>,<command>]"},
        unBanAddressCommand.getUsage());
  }

  @Test
  @DisplayName("isRunningBackground is false")
  void testIsRunningBackgroundIsFalse() {
    assertFalse(unBanAddressCommand.isRunningBackground());
  }

  @Test
  @DisplayName("execute with address arg does not throw")
  void testExecuteWithAddressDoesNotThrow() {
    assertDoesNotThrow(() -> unBanAddressCommand.execute(List.of("192.168.1.1")));
  }

  @Test
  @DisplayName("execute with empty args does not throw")
  void testExecuteEmptyArgsDoesNotThrow() {
    assertDoesNotThrow(() -> unBanAddressCommand.execute(Collections.emptyList()));
  }
}
