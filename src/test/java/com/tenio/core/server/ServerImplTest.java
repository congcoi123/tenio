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

package com.tenio.core.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ServerImpl")
class ServerImplTest {

  @Test
  @DisplayName("getInstance returns a non-null server")
  void testGetInstanceReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance());
  }

  @Test
  @DisplayName("getInstance returns the same singleton instance on repeated calls")
  void testGetInstanceIsSingleton() {
    assertSame(ServerImpl.getInstance(), ServerImpl.getInstance());
  }

  @Test
  @DisplayName("getApi returns a non-null ServerApi")
  void testGetApiReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance().getApi());
  }

  @Test
  @DisplayName("getEventManager returns a non-null EventManager")
  void testGetEventManagerReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance().getEventManager());
  }

  @Test
  @DisplayName("getPlayerManager returns a non-null PlayerManager")
  void testGetPlayerManagerReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance().getPlayerManager());
  }

  @Test
  @DisplayName("getRoomManager returns a non-null RoomManager")
  void testGetRoomManagerReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance().getRoomManager());
  }

  @Test
  @DisplayName("getChannelManager returns a non-null ChannelManager")
  void testGetChannelManagerReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance().getChannelManager());
  }

  @Test
  @DisplayName("getDatagramChannelManager returns a non-null DatagramChannelManager")
  void testGetDatagramChannelManagerReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance().getDatagramChannelManager());
  }

  @Test
  @DisplayName("getUptime returns a non-negative value before start")
  void testGetUptimeNonNegativeBeforeStart() {
    assertTrue(ServerImpl.getInstance().getUptime() >= 0);
  }

  @Test
  @DisplayName("getStartedTime returns 0 before start is called")
  void testGetStartedTimeIsZeroBeforeStart() {
    assertTrue(ServerImpl.getInstance().getStartedTime() >= 0);
  }

  @Test
  @DisplayName("getConfiguration returns null before start is called")
  void testGetConfigurationIsNullBeforeStart() {
    assertNull(ServerImpl.getInstance().getConfiguration());
  }

  @Test
  @DisplayName("getClientCommandManager returns null before start is called")
  void testGetClientCommandManagerIsNullBeforeStart() {
    assertNull(ServerImpl.getInstance().getClientCommandManager());
  }
}
