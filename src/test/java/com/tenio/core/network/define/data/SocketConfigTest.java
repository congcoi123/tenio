package com.tenio.core.network.define.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tenio.core.network.define.TransportType;
import org.junit.jupiter.api.Test;

class SocketConfigTest {
  @Test
  void testConstructor() {
    SocketConfig actualSocketConfig = new SocketConfig("Name", TransportType.UNKNOWN, 8080);

    assertEquals("Name", actualSocketConfig.getName());
    assertEquals(8080, actualSocketConfig.getPort());
    assertEquals(TransportType.UNKNOWN, actualSocketConfig.getType());
    assertEquals("SocketConfig{name='Name', type=UNKNOWN, port=8080}", actualSocketConfig.toString());
  }
}

