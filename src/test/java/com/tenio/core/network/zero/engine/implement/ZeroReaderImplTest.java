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

package com.tenio.core.network.zero.engine.implement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.zero.engine.ZeroReader;
import com.tenio.core.network.zero.engine.reader.policy.DatagramPacketPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ZeroReaderImpl")
class ZeroReaderImplTest {

  private ZeroReader reader;

  @BeforeEach
  void setUp() {
    reader = ZeroReaderImpl.newInstance(mock(EventManager.class));
  }

  @Test
  @DisplayName("newInstance creates a reader with name 'reader'")
  void testNewInstanceCreatesWithNameReader() {
    assertEquals("reader", reader.getName());
  }

  @Test
  @DisplayName("getNetworkReaderStatistic is null before being set")
  void testGetNetworkReaderStatisticIsNullBeforeSet() {
    assertNull(reader.getNetworkReaderStatistic());
  }

  @Test
  @DisplayName("setNetworkReaderStatistic and getNetworkReaderStatistic work correctly")
  void testSetAndGetNetworkReaderStatistic() {
    NetworkReaderStatistic statistic = mock(NetworkReaderStatistic.class);
    reader.setNetworkReaderStatistic(statistic);
    assertEquals(statistic, reader.getNetworkReaderStatistic());
  }

  @Test
  @DisplayName("getNumberOfExtraWorkers returns 0 when no UDP config is set")
  void testGetNumberOfExtraWorkersReturnsZeroWithoutUdpConfig() {
    assertEquals(0, ((AbstractZeroEngine) reader).getNumberOfExtraWorkers());
  }

  @Test
  @DisplayName("getNumberOfExtraWorkers returns 1 after UDP config is set")
  void testGetNumberOfExtraWorkersReturnsOneWithUdpConfig() {
    reader.setUdpChannelConfiguration(
        new SocketConfiguration("udp", TransportType.UDP, 8081, 4));
    assertEquals(1, ((AbstractZeroEngine) reader).getNumberOfExtraWorkers());
  }

  @Test
  @DisplayName("setServerAddress does not throw")
  void testSetServerAddressDoesNotThrow() {
    assertDoesNotThrow(() -> reader.setServerAddress("0.0.0.0"));
  }

  @Test
  @DisplayName("setUdpChannelConfiguration does not throw")
  void testSetUdpChannelConfigurationDoesNotThrow() {
    SocketConfiguration config = new SocketConfiguration("udp", TransportType.UDP, 9000, 2);
    assertDoesNotThrow(() -> reader.setUdpChannelConfiguration(config));
  }

  @Test
  @DisplayName("setDatagramPacketPolicy does not throw")
  void testSetDatagramPacketPolicyDoesNotThrow() {
    DatagramPacketPolicy policy = mock(DatagramPacketPolicy.class);
    assertDoesNotThrow(() -> reader.setDatagramPacketPolicy(policy));
  }
}
