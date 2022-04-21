package com.tenio.core.network.define.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tenio.core.network.define.RestMethod;
import org.junit.jupiter.api.Test;

class HttpConfigTest {
  @Test
  void testConstructor() {
    HttpConfig actualHttpConfig = new HttpConfig("https://example.org/example", 8080);

    assertEquals("https://example.org/example", actualHttpConfig.getName());
    assertEquals(8080, actualHttpConfig.getPort());
    assertEquals("{ paths:[], name:https://example.org/example, port:8080}",
        actualHttpConfig.toString());
  }

  @Test
  void testAddPath() {
    HttpConfig httpConfig = new HttpConfig("https://example.org/example", 8080);
    httpConfig
        .addPath(new PathConfig("Name", RestMethod.POST, "Uri",
            "The characteristics of someone or something", 1));
    assertEquals(1, httpConfig.getPaths().size());
  }
}

