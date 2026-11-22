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

package com.tenio.engine.heartbeat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.engine.message.ExtraMessage;
import com.tenio.engine.physic2d.graphic.Paint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HeartbeatTest {

  private HeartBeatManagerImpl manager;

  @BeforeEach
  void setUp() throws Exception {
    manager = new HeartBeatManagerImpl();
    manager.initialize(2);
  }

  @AfterEach
  void tearDown() {
    manager.clear();
  }

  @Test
  void testContainsReturnsFalseForUnknownId() {
    assertFalse(manager.contains("unknown"));
  }

  @Test
  void testCreateShouldRegisterHeartbeat() {
    manager.create("hb1", new TestHeartBeat());
    assertTrue(manager.contains("hb1"));
  }

  @Test
  void testDisposeShouldUnregisterHeartbeat() {
    manager.create("hb1", new TestHeartBeat());
    manager.dispose("hb1");
    assertFalse(manager.contains("hb1"));
  }

  @Test
  void testDisposeWithUnknownIdShouldNotThrow() {
    assertDoesNotThrow(() -> manager.dispose("nonexistent"));
  }

  @Test
  void testClearShouldNotThrow() {
    manager.create("hb1", new TestHeartBeat());
    assertDoesNotThrow(() -> manager.clear());
  }

  @Test
  void testSendMessageWithDelayShouldSucceed() {
    manager.create("hb1", new TestHeartBeat());
    assertDoesNotThrow(() -> manager.sendMessage("hb1", null, 0.5));
  }

  @Test
  void testSendMessageWithoutDelayShouldSucceed() {
    manager.create("hb1", new TestHeartBeat());
    assertDoesNotThrow(() -> manager.sendMessage("hb1", null));
  }

  private static class TestHeartBeat extends AbstractHeartBeat {

    @Override
    protected void onCreate() {}

    @Override
    protected void onMessage(ExtraMessage message) {}

    @Override
    protected void onUpdate(float deltaTime) {}

    @Override
    protected void onRender(Paint paint) {}

    @Override
    protected void onPause() {}

    @Override
    protected void onResume() {}

    @Override
    protected void onDispose() {}

    @Override
    protected void onAction1() {}

    @Override
    protected void onAction2() {}

    @Override
    protected void onAction3() {}
  }
}
