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

package com.tenio.core.command.system;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.bootstrap.annotation.SystemCommand;
import com.tenio.core.exception.AddedDuplicatedCommandException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SystemCommand(label = "cmd", usage = {"arg1"}, description = "a test command")
class StubCommandHandler extends AbstractSystemCommandHandler {

  boolean executed = false;

  @Override
  public void execute(List<String> arguments) {
    executed = true;
  }
}

@SystemCommand(label = "bgcmd", usage = {}, description = "bg", isBackgroundRunning = true)
class BgCommandHandler extends AbstractSystemCommandHandler {

  volatile boolean executed = false;
  final CountDownLatch latch = new CountDownLatch(1);

  @Override
  public void execute(List<String> arguments) {
    executed = true;
    latch.countDown();
  }
}

@DisplayName("Unit Test Cases For SystemCommandManager")
class SystemCommandManagerTest {

  private SystemCommandManager manager;

  @BeforeEach
  void setUp() {
    manager = new SystemCommandManager();
  }

  @Test
  @DisplayName("registerCommand stores handler and makes it retrievable")
  void testRegisterAndGetHandler() {
    var handler = new StubCommandHandler();
    manager.registerCommand("cmd", handler);
    assertEquals(handler, manager.getHandler("cmd"));
  }

  @Test
  @DisplayName("registerCommand normalizes label to lowercase")
  void testRegisterCommandNormalizesLabelToLowercase() {
    var handler = new StubCommandHandler();
    manager.registerCommand("CMD", handler);
    assertNotNull(manager.getHandler("cmd"));
    assertNull(manager.getHandler("CMD"));
  }

  @Test
  @DisplayName("registerCommand with duplicate label throws AddedDuplicatedCommandException")
  void testRegisterDuplicateLabelThrows() {
    manager.registerCommand("cmd", new StubCommandHandler());
    assertThrows(AddedDuplicatedCommandException.class,
        () -> manager.registerCommand("cmd", new StubCommandHandler()));
  }

  @Test
  @DisplayName("getHandler for unknown label returns null")
  void testGetHandlerUnknownLabelReturnsNull() {
    assertNull(manager.getHandler("unknown"));
  }

  @Test
  @DisplayName("getHandlers returns all registered handlers")
  void testGetHandlersReturnsAll() {
    manager.registerCommand("cmd", new StubCommandHandler());
    assertTrue(manager.getHandlers().containsKey("cmd"));
    assertEquals(1, manager.getHandlers().size());
  }

  @Test
  @DisplayName("getHandlersAsList returns list of registered handlers")
  void testGetHandlersAsList() {
    manager.registerCommand("cmd", new StubCommandHandler());
    assertEquals(1, manager.getHandlersAsList().size());
  }

  @Test
  @DisplayName("getAnnotations returns stored annotations")
  void testGetAnnotationsReturnsAnnotations() {
    manager.registerCommand("cmd", new StubCommandHandler());
    assertNotNull(manager.getAnnotations().get("cmd"));
    assertEquals("a test command", manager.getAnnotations().get("cmd").description());
  }

  @Test
  @DisplayName("getAnnotationsAsList returns list of annotations")
  void testGetAnnotationsAsList() {
    manager.registerCommand("cmd", new StubCommandHandler());
    assertEquals(1, manager.getAnnotationsAsList().size());
  }

  @Test
  @DisplayName("invoke with blank input does nothing")
  void testInvokeBlankInputDoesNothing() {
    assertDoesNotThrow(() -> manager.invoke(""));
    assertDoesNotThrow(() -> manager.invoke("   "));
  }

  @Test
  @DisplayName("invoke with unknown command label does nothing")
  void testInvokeUnknownCommandDoesNothing() {
    assertDoesNotThrow(() -> manager.invoke("nonexistent arg1"));
  }

  @Test
  @DisplayName("invoke with known command calls execute synchronously")
  void testInvokeKnownCommandCallsExecute() {
    var handler = new StubCommandHandler();
    manager.registerCommand("cmd", handler);
    manager.invoke("cmd arg1");
    assertTrue(handler.executed);
  }

  @Test
  @DisplayName("invoke with background command runs execute in background thread")
  void testInvokeBackgroundCommandRunsInBackground() throws InterruptedException {
    var handler = new BgCommandHandler();
    manager.registerCommand("bgcmd", handler);
    manager.invoke("bgcmd somearg");
    assertTrue(handler.latch.await(2, TimeUnit.SECONDS));
    assertTrue(handler.executed);
  }

  @Test
  @DisplayName("clear removes all handlers and annotations")
  void testClearEmptiesManager() {
    manager.registerCommand("cmd", new StubCommandHandler());
    manager.clear();
    assertTrue(manager.getHandlers().isEmpty());
    assertTrue(manager.getAnnotations().isEmpty());
    assertNull(manager.getHandler("cmd"));
  }
}
