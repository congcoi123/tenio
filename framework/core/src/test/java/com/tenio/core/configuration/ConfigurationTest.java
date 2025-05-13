/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.configuration.SocketConfiguration;
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
        () -> assertTrue(configuration.getBoolean(TestConfigurationType.CUSTOM_VALUE_4)));
  }

  @Test
  public void getConfigurationSocketsShouldReturnTrueValue() {
    var tcp = (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_TCP);
    var webSocket = (SocketConfiguration) configuration.get(CoreConfigurationType.NETWORK_WEBSOCKET);
    assertAll("getSocketPortsConfiguration",
        () -> assertEquals("8032", tcp.port()),
        () -> assertEquals("8033", webSocket.port()),
        () -> assertEquals(TransportType.TCP, tcp.type()),
        () -> assertEquals(TransportType.WEB_SOCKET, webSocket.type())
    );
  }
}
