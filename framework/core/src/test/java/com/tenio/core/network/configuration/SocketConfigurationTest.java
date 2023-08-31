package com.tenio.core.network.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.define.TransportType;
import org.junit.jupiter.api.Test;

class SocketConfigurationTest {
  @Test
  void testConstructor() {
    SocketConfiguration
        actualSocketConfiguration = new SocketConfiguration("Name", TransportType.UNKNOWN, 8080);

    assertEquals("Name", actualSocketConfiguration.name());
    assertEquals(8080, actualSocketConfiguration.port());
    assertEquals(TransportType.UNKNOWN, actualSocketConfiguration.type());
    assertEquals("SocketConfiguration[name=Name, type=UNKNOWN, port=8080]",
        actualSocketConfiguration.toString());
  }
}

