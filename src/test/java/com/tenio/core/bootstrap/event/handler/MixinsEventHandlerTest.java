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

package com.tenio.core.bootstrap.event.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.handler.implement.MixinsEventHandler;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.handler.event.EventFetchedBandwidthInfo;
import com.tenio.core.handler.event.EventFetchedCcuInfo;
import com.tenio.core.handler.event.EventServerInitialization;
import com.tenio.core.handler.event.EventServerTeardown;
import com.tenio.core.handler.event.EventSystemMonitoring;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For MixinsEventHandler")
class MixinsEventHandlerTest {

  @Test
  @DisplayName("Test initialize with all null event listeners does not throw")
  void testInitialize() {
    MixinsEventHandler mixinsEventHandler = new MixinsEventHandler();
    mixinsEventHandler.initialize(EventManager.newInstance());
  }

  @Test
  @DisplayName("Test SERVER_INITIALIZATION event dispatches to listener")
  void testServerInitializationEvent() throws Exception {
    MixinsEventHandler handler = new MixinsEventHandler();
    EventManager em = EventManager.newInstance();
    EventServerInitialization mockEvent = mock(EventServerInitialization.class);
    Field field = MixinsEventHandler.class.getDeclaredField("eventServerInitialization");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    em.emit(ServerEvent.SERVER_INITIALIZATION, "TestServer");
    verify(mockEvent).onServerInitialization("TestServer");
  }

  @Test
  @DisplayName("Test SERVER_TEARDOWN event dispatches to listener")
  void testServerTeardownEvent() throws Exception {
    MixinsEventHandler handler = new MixinsEventHandler();
    EventManager em = EventManager.newInstance();
    EventServerTeardown mockEvent = mock(EventServerTeardown.class);
    Field field = MixinsEventHandler.class.getDeclaredField("eventServerTeardown");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    em.emit(ServerEvent.SERVER_TEARDOWN, "TestServer");
    verify(mockEvent).onServerTeardown("TestServer");
  }

  @Test
  @DisplayName("Test FETCHED_BANDWIDTH_INFO event dispatches to listener")
  void testFetchedBandwidthInfoEvent() throws Exception {
    MixinsEventHandler handler = new MixinsEventHandler();
    EventManager em = EventManager.newInstance();
    EventFetchedBandwidthInfo mockEvent = mock(EventFetchedBandwidthInfo.class);
    Field field = MixinsEventHandler.class.getDeclaredField("eventFetchedBandwidthInfo");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    em.emit(ServerEvent.FETCHED_BANDWIDTH_INFO, 100L, 10L, 0L, 200L, 20L, 1L, 0L);
    verify(mockEvent).onFetchedBandwidthInfo(100L, 10L, 0L, 200L, 20L, 1L, 0L);
  }

  @Test
  @DisplayName("Test FETCHED_CCU_INFO event dispatches to listener")
  void testFetchedCcuInfoEvent() throws Exception {
    MixinsEventHandler handler = new MixinsEventHandler();
    EventManager em = EventManager.newInstance();
    EventFetchedCcuInfo mockEvent = mock(EventFetchedCcuInfo.class);
    Field field = MixinsEventHandler.class.getDeclaredField("eventFetchedCcuInfo");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    em.emit(ServerEvent.FETCHED_CCU_INFO, 42);
    verify(mockEvent).onFetchedCcuInfo(42);
  }

  @Test
  @DisplayName("Test SYSTEM_MONITORING event dispatches to listener")
  void testSystemMonitoringEvent() throws Exception {
    MixinsEventHandler handler = new MixinsEventHandler();
    EventManager em = EventManager.newInstance();
    EventSystemMonitoring mockEvent = mock(EventSystemMonitoring.class);
    Field field = MixinsEventHandler.class.getDeclaredField("eventSystemMonitoring");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    em.emit(ServerEvent.SYSTEM_MONITORING, 0.5d, 1024L, 512L, 512L, 4L);
    verify(mockEvent).onSystemMonitoring(0.5d, 1024L, 512L, 512L, 4L);
  }
}
