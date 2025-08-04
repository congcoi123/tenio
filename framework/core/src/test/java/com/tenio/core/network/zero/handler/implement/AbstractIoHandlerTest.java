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

package com.tenio.core.network.zero.handler.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For AbstractIoHandler")
class AbstractIoHandlerTest {

  private AbstractIoHandler handler;
  private SessionManager sessionManager;
  private NetworkReaderStatistic readerStatistic;

  @BeforeEach
  void setUp() {
    EventManager eventManager = mock(EventManager.class);
    sessionManager = mock(SessionManager.class);
    readerStatistic = mock(NetworkReaderStatistic.class);
    handler = new AbstractIoHandler(eventManager) {
    };
  }

  @Test
  @DisplayName("Test session manager setter")
  void testSetSessionManager() {
    handler.setSessionManager(sessionManager);
    assertEquals(sessionManager, handler.sessionManager);
  }

  @Test
  @DisplayName("Test network reader statistic setter")
  void testSetNetworkReaderStatistic() {
    handler.setNetworkReaderStatistic(readerStatistic);
    assertEquals(readerStatistic, handler.networkReaderStatistic);
  }
}
