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

package com.tenio.core.network.zero.engine.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tenio.core.configuration.constant.CoreConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For DatagramChannelManager")
class DatagramChannelManagerTest {

  private DatagramChannelManager manager;

  @BeforeEach
  void setUp() {
    manager = DatagramChannelManager.newInstance();
  }

  @Test
  @DisplayName("New instance should initialize UDP port to NULL_PORT_VALUE")
  void testNewInstanceInitializesUdpPortToNullValue() {
    assertEquals(CoreConstant.NULL_PORT_VALUE, manager.getUdpPort());
  }

  @Test
  @DisplayName("New instance should initialize KCP port to NULL_PORT_VALUE")
  void testNewInstanceInitializesKcpPortToNullValue() {
    assertEquals(CoreConstant.NULL_PORT_VALUE, manager.getKcpPort());
  }

  @Test
  @DisplayName("Configure and get UDP port")
  void testConfigureAndGetUdpPort() {
    manager.configureUdpPort(8080);
    assertEquals(8080, manager.getUdpPort());
  }

  @Test
  @DisplayName("Configure and get KCP port")
  void testConfigureAndGetKcpPort() {
    manager.configureKcpPort(9090);
    assertEquals(9090, manager.getKcpPort());
  }

  @Test
  @DisplayName("getCurrentUdpConveyId should start at zero")
  void testGetCurrentUdpConveyIdStartsAtZero() {
    assertEquals(0, manager.getCurrentUdpConveyId());
  }

  @Test
  @DisplayName("getCurrentUdpConveyId should increment on each call")
  void testGetCurrentUdpConveyIdIncrements() {
    manager.getCurrentUdpConveyId(); // 0
    assertEquals(1, manager.getCurrentUdpConveyId());
    assertEquals(2, manager.getCurrentUdpConveyId());
  }

  @Test
  @DisplayName("getCurrentKcpConveyId should start at zero")
  void testGetCurrentKcpConveyIdStartsAtZero() {
    assertEquals(0, manager.getCurrentKcpConveyId());
  }

  @Test
  @DisplayName("getCurrentKcpConveyId should increment on each call")
  void testGetCurrentKcpConveyIdIncrements() {
    manager.getCurrentKcpConveyId(); // 0
    assertEquals(1, manager.getCurrentKcpConveyId());
    assertEquals(2, manager.getCurrentKcpConveyId());
  }

  @Test
  @DisplayName("UDP and KCP convey ID counters are independent")
  void testUdpAndKcpConveyIdsAreIndependent() {
    manager.getCurrentUdpConveyId(); // advance UDP to 1
    manager.getCurrentUdpConveyId(); // advance UDP to 2
    // KCP counter should still be at 0
    assertEquals(0, manager.getCurrentKcpConveyId());
    // UDP counter should now be at 2
    assertEquals(2, manager.getCurrentUdpConveyId());
  }
}
