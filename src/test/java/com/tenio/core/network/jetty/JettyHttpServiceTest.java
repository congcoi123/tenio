/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.jetty;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import com.tenio.core.event.implement.EventManager;
import jakarta.servlet.http.HttpServlet;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For JettyHttpService")
class JettyHttpServiceTest {

  private JettyHttpService service;

  @BeforeEach
  void setUp() {
    EventManager eventManager = EventManager.newInstance();
    service = JettyHttpService.newInstance(eventManager);
    service.setThreadPoolSize(16); // valid max threads
    service.setPort(8080); // valid port
    service.setServletMap(Collections.singletonMap("test", mock(HttpServlet.class)));
  }

  @Test
  @DisplayName("Test start() and shutdown() behaviours")
  void testStartAndShutdown() {
    assertDoesNotThrow(() -> service.initialize());
    assertDoesNotThrow(() -> service.start());
    assertDoesNotThrow(() -> service.shutdown());
  }
}
