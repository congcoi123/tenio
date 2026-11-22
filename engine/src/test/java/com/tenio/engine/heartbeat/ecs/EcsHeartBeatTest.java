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

package com.tenio.engine.heartbeat.ecs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.tenio.engine.ecs.system.System;
import com.tenio.engine.message.ExtraMessage;
import com.tenio.engine.physic2d.graphic.Paint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EcsHeartBeatTest {

  private EcsHeartBeat heartBeat;

  @BeforeEach
  void setUp() {
    heartBeat = new EcsHeartBeat(800, 600);
  }

  @Test
  void testConstructor() {
    assertNotNull(heartBeat);
  }

  @Test
  void testAddSystem() {
    var system = mock(System.class);
    heartBeat.addSystem(system);
  }

  @Test
  void testClearSystems() {
    var system = mock(System.class);
    heartBeat.addSystem(system);
    heartBeat.clearSystems();
  }

  @Test
  void testOnCreate() {
    heartBeat.onCreate();
  }

  @Test
  void testOnMessage() {
    var message = mock(ExtraMessage.class);
    heartBeat.onMessage(message);
  }

  @Test
  void testOnUpdate() {
    heartBeat.onUpdate(0.016f);
  }

  @Test
  void testOnRender() {
    heartBeat.onRender(Paint.getInstance());
  }

  @Test
  void testOnPause() {
    heartBeat.onPause();
  }

  @Test
  void testOnResume() {
    heartBeat.onResume();
  }

  @Test
  void testOnDispose() {
    heartBeat.onDispose();
  }

  @Test
  void testOnAction1() {
    heartBeat.onAction1();
  }

  @Test
  void testOnAction2() {
    heartBeat.onAction2();
  }

  @Test
  void testOnAction3() {
    heartBeat.onAction3();
  }
}
