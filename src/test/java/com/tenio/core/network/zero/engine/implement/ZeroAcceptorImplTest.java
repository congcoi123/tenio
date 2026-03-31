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

package com.tenio.core.network.zero.engine.implement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.zero.engine.ZeroAcceptor;
import com.tenio.core.network.zero.engine.listener.ZeroReaderListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ZeroAcceptorImpl")
class ZeroAcceptorImplTest {

  private ZeroAcceptor acceptor;

  @BeforeEach
  void setUp() {
    acceptor = ZeroAcceptorImpl.newInstance(mock(EventManager.class));
  }

  @Test
  @DisplayName("newInstance creates an acceptor with name 'acceptor'")
  void testNewInstanceCreatesWithNameAcceptor() {
    assertEquals("acceptor", acceptor.getName());
  }

  @Test
  @DisplayName("setConnectionFilter does not throw")
  void testSetConnectionFilterDoesNotThrow() {
    ConnectionFilter filter = mock(ConnectionFilter.class);
    assertDoesNotThrow(() -> acceptor.setConnectionFilter(filter));
  }

  @Test
  @DisplayName("setServerAddress does not throw")
  void testSetServerAddressDoesNotThrow() {
    assertDoesNotThrow(() -> acceptor.setServerAddress("127.0.0.1"));
  }

  @Test
  @DisplayName("setSocketConfiguration does not throw")
  void testSetSocketConfigurationDoesNotThrow() {
    SocketConfiguration config = new SocketConfiguration("tcp", TransportType.TCP, 8080, 4);
    assertDoesNotThrow(() -> acceptor.setSocketConfiguration(config));
  }

  @Test
  @DisplayName("setZeroReaderListener does not throw")
  void testSetZeroReaderListenerDoesNotThrow() {
    ZeroReaderListener listener = mock(ZeroReaderListener.class);
    assertDoesNotThrow(() -> acceptor.setZeroReaderListener(listener));
  }

  @Test
  @DisplayName("getNumberOfExtraWorkers returns 0")
  void testGetNumberOfExtraWorkersReturnsZero() {
    assertEquals(0, ((AbstractZeroEngine) acceptor).getNumberOfExtraWorkers());
  }
}
