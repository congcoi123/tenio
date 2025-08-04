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

package com.tenio.core.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.protocol.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Controller")
class ControllerTest {

  private TestController controller;

  @BeforeEach
  void setUp() {
    EventManager eventManager = mock(EventManager.class);
    controller = new TestController(eventManager);
  }

  @Test
  @DisplayName("Test controller name getter/setter")
  void testNameGetterSetter() {
    controller.setName("Test");
    assertEquals("Test", controller.getName());
  }

  @Test
  @DisplayName("Test controller initialization and shutdown behaviours")
  void testInitializeAndStartAndShutdown() {
    controller.setName("Test");
    controller.setThreadPoolSize(1);
    controller.initialize();
    assertFalse(controller.isActivated());
    controller.start();
    controller.activate();
    assertTrue(controller.isActivated());
    controller.shutdown();
    assertFalse(controller.isActivated());
  }

  @Test
  @DisplayName("The controller should not start or shutdown again once it's done those actions")
  void testDoubleStartAndShutdown() {
    controller.setThreadPoolSize(1);
    controller.initialize();
    controller.start();
    controller.start(); // Should not throw
    controller.shutdown();
    controller.shutdown(); // Should not throw
  }

  @Test
  @DisplayName("Test controller max request queue size setter")
  void testMaxRequestQueueSize() {
    controller.setMaxRequestQueueSize(2);
    assertEquals(2, controller.getMaxRequestQueueSize());
  }

  @Test
  @DisplayName("Test controller thread pool size setter")
  void testThreadPoolSize() {
    controller.setThreadPoolSize(2);
    assertEquals(2, controller.getThreadPoolSize());
  }

  @Test
  @DisplayName("Add a request into a full request queue should throw exception")
  void testEnqueueRequestQueueFull() {
    controller.setThreadPoolSize(1);
    controller.setMaxRequestQueueSize(1);
    controller.initialize();
    Request req1 = mock(Request.class);
    when(req1.getId()).thenReturn(0L);
    Request req2 = mock(Request.class);
    when(req2.getId()).thenReturn(0L);
    controller.enqueueRequest(req1);
    assertThrows(Exception.class, () -> controller.enqueueRequest(req2));
  }

  static class TestController extends AbstractController {

    TestController(EventManager eventManager) {
      super(eventManager);
    }

    @Override
    protected boolean isEnabledPriority() {
      return false;
    }

    @Override
    public void subscribe() {
    }

    @Override
    public void processRequest(Request request) {
    }

    @Override
    public void onInitialized() {
    }

    @Override
    public void onStarted() {
    }

    @Override
    public void onRunning() {
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public void onDestroyed() {
    }
  }
}
