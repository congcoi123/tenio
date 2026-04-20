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

package com.tenio.core.configuration.define;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ServerEvent")
class ServerEventTest {

  @Test
  @DisplayName("All expected enum constants are present")
  void testExpectedConstantsExist() {
    assertNotNull(ServerEvent.valueOf("SERVER_INITIALIZATION"));
    assertNotNull(ServerEvent.valueOf("PLAYER_LOGIN"));
    assertNotNull(ServerEvent.valueOf("PLAYER_CONNECTION_RETRY"));
    assertNotNull(ServerEvent.valueOf("PLAYER_CONNECTION_RESUMED"));
    assertNotNull(ServerEvent.valueOf("ROOM_CREATED_RESULT"));
    assertNotNull(ServerEvent.valueOf("PLAYER_JOINED_ROOM_RESULT"));
    assertNotNull(ServerEvent.valueOf("PLAYER_BEFORE_LEAVE_ROOM"));
    assertNotNull(ServerEvent.valueOf("PLAYER_AFTER_LEFT_ROOM"));
    assertNotNull(ServerEvent.valueOf("DISCONNECT_PLAYER"));
    assertNotNull(ServerEvent.valueOf("SERVER_TEARDOWN"));
    assertNotNull(ServerEvent.valueOf("CHANNEL_CREATED"));
    assertNotNull(ServerEvent.valueOf("CHANNEL_WILL_BE_REMOVED"));
    assertNotNull(ServerEvent.valueOf("BROADCAST_TO_CHANNEL"));
    assertNotNull(ServerEvent.valueOf("FETCHED_CCU_INFO"));
    assertNotNull(ServerEvent.valueOf("FETCHED_BANDWIDTH_INFO"));
    assertNotNull(ServerEvent.valueOf("SYSTEM_MONITORING"));
  }

  @Test
  @DisplayName("Total number of ServerEvent values is 33")
  void testTotalCount() {
    assertEquals(33, ServerEvent.values().length);
  }

  @Test
  @DisplayName("toString() returns the enum name for every constant")
  void testToStringEqualsName() {
    for (ServerEvent event : ServerEvent.values()) {
      assertEquals(event.name(), event.toString());
    }
  }
}
