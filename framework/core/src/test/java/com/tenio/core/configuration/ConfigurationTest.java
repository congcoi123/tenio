/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.core.configuration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.define.data.SocketConfig;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigurationTest {

  private Configuration configuration;

  @BeforeEach
  public void initialize() throws Exception {
    configuration = new Configuration();
    configuration.load("configuration.example.xml");
  }

  @Test
  public void getConfigurationExtensionShouldReturnTrueValue() {
    assertAll("getExtensionConfiguration",
        () -> assertEquals("String", configuration.getString(TestConfigurationType.CUSTOM_VALUE_1)),
        () -> assertEquals(1, configuration.getInt(TestConfigurationType.CUSTOM_VALUE_2)),
        () -> assertEquals(1.5, configuration.getFloat(TestConfigurationType.CUSTOM_VALUE_3)),
        () -> assertEquals(true, configuration.getBoolean(TestConfigurationType.CUSTOM_VALUE_4)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getConfigurationSocketPortsShouldReturnTrueValue() {
    var listSockets =
        (List<SocketConfig>) (configuration.get(CoreConfigurationType.NETWORK_SOCKET_CONFIGS));
    assertAll("getSocketPortsConfiguration", () -> assertEquals(8032, listSockets.get(0).getPort()),
        () -> assertEquals(8033, listSockets.get(1).getPort()));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getConfigurationSocketPortsTypeShouldReturnTrueType() {
    var listSockets =
        (List<SocketConfig>) (configuration.get(CoreConfigurationType.NETWORK_SOCKET_CONFIGS));
    assertAll("getSocketPortsTypeConfiguration",
        () -> assertEquals(TransportType.TCP, listSockets.get(0).getType()),
        () -> assertEquals(TransportType.WEB_SOCKET, listSockets.get(1).getType())
    );
  }

  @AfterEach
  public void tearDown() {
    // do nothing
  }
}
