package com.tenio.core.network.define.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tenio.core.network.define.RestMethod;
import org.junit.jupiter.api.Test;

class PathConfigTest {
  @Test
  void testConstructor() {
    PathConfig actualPathConfig = new PathConfig("Name", RestMethod.POST, "Uri",
        "The characteristics of someone or something", 1);

    assertEquals("The characteristics of someone or something", actualPathConfig.getDescription());
    assertEquals(RestMethod.POST, actualPathConfig.getMethod());
    assertEquals("Name", actualPathConfig.getName());
    assertEquals("Uri", actualPathConfig.getUri());
    assertEquals(1, actualPathConfig.getVersion());
    assertEquals(
        "PathConfig{name='Name', description='The characteristics of someone or something', version=1, method=POST, uri='Uri'}",
        actualPathConfig.toString());
  }
}

