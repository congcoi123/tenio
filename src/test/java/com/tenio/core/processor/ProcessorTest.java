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

package com.tenio.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.inbound.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Processor")
class ProcessorTest {

  private TestProcessor processor;

  @BeforeEach
  void setUp() {
    EventManager eventManager = mock(EventManager.class);
    processor = new TestProcessor(eventManager);
  }

  @Test
  @DisplayName("Test processor name getter/setter")
  void testNameGetterSetter() {
    processor.setName("Test");
    assertEquals("Test", processor.getName());
  }

  @Test
  @DisplayName("Test processor initialization and shutdown behaviours")
  void testInitializeAndStartAndShutdown() {
    processor.setName("Test");
    processor.setThreadPoolSize(1);
    processor.initialize();
    assertFalse(processor.isActivated());
    processor.start();
    processor.activate();
    assertTrue(processor.isActivated());
    processor.shutdown();
    assertFalse(processor.isActivated());
  }

  @Test
  @DisplayName("The processor should not start or shutdown again once it's done those actions")
  void testDoubleStartAndShutdown() {
    processor.setThreadPoolSize(1);
    processor.initialize();
    processor.start();
    processor.start(); // Should not throw
    processor.shutdown();
    processor.shutdown(); // Should not throw
  }

  @Test
  @DisplayName("Test processor thread pool size setter")
  void testThreadPoolSize() {
    processor.setThreadPoolSize(2);
    assertEquals(2, processor.getThreadPoolSize());
  }

  @Test
  @DisplayName("Default thread pool size is DEFAULT_NUMBER_WORKERS (5)")
  void testDefaultThreadPoolSize() {
    assertEquals(Processor.DEFAULT_NUMBER_WORKERS, processor.getThreadPoolSize());
  }

  @Test
  @DisplayName("isActivated is false before activate() is called")
  void testIsActivatedDefaultFalse() {
    assertFalse(processor.isActivated());
  }

  @Test
  @DisplayName("getName returns null before setName is called")
  void testGetNameDefaultNull() {
    assertNull(processor.getName());
  }

  @Test
  @DisplayName("setThreadPoolSize below 1 throws IllegalArgumentException")
  void testSetThreadPoolSizeBelowOneThrows() {
    assertThrows(IllegalArgumentException.class, () -> processor.setThreadPoolSize(0));
    assertThrows(IllegalArgumentException.class, () -> processor.setThreadPoolSize(-1));
  }

  @Test
  @DisplayName("getMaximumStartingTimeInMilliseconds equals threadPoolSize * delay constant")
  void testGetMaximumStartingTimeInMilliseconds() {
    processor.setThreadPoolSize(3);
    assertEquals(3 * CoreConstant.DELAY_BETWEEN_STARTING_WORKER_IN_MILLISECONDS,
        processor.getMaximumStartingTimeInMilliseconds());
  }

  @Test
  @DisplayName("shutdown before initialize is a no-op and does not throw")
  void testShutdownBeforeInitializeIsNoOp() {
    assertDoesNotThrow(() -> processor.shutdown());
  }

  @Test
  @DisplayName("enqueueRequest after initialize succeeds when queue is unlimited")
  void testEnqueueRequestSucceedsWhenQueueUnlimited() {
    processor.setThreadPoolSize(1);
    processor.initialize();
    Request req = mock(Request.class);
    when(req.getId()).thenReturn(1L);
    assertDoesNotThrow(() -> processor.enqueueRequest(req));
  }

  @Test
  @DisplayName("priority-enabled processor initializes without throwing")
  void testPriorityEnabledProcessorInitializesOk() {
    EventManager eventManager = mock(EventManager.class);
    var priorityProcessor = new PriorityTestProcessor(eventManager);
    priorityProcessor.setThreadPoolSize(1);
    assertDoesNotThrow(priorityProcessor::initialize);
  }

  static class TestProcessor extends AbstractProcessor {

    TestProcessor(EventManager eventManager) {
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

  static class PriorityTestProcessor extends AbstractProcessor {

    PriorityTestProcessor(EventManager eventManager) {
      super(eventManager);
    }

    @Override
    protected boolean isEnabledPriority() {
      return true;
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
