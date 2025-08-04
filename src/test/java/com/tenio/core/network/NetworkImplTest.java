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

package com.tenio.core.network;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.jetty.JettyHttp;
import com.tenio.core.network.kcp.KcpChannel;
import com.tenio.core.network.netty.NettyWebSocket;
import com.tenio.core.network.zero.ZeroSocket;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For NetworkImpl")
class NetworkImplTest {

  private NetworkImpl service;

  @BeforeEach
  void setUp() throws Exception {
    EventManager eventManager = EventManager.newInstance();
    service = (NetworkImpl) NetworkImpl.newInstance(eventManager);
    JettyHttp jettyService = mock(JettyHttp.class);
    KcpChannel kcpChannel = mock(KcpChannel.class);
    NettyWebSocket nettyService = mock(NettyWebSocket.class);
    ZeroSocket zeroService = mock(ZeroSocket.class);
    // Inject mocks into private final fields
    Field f1 = NetworkImpl.class.getDeclaredField("httpService");
    f1.setAccessible(true);
    f1.set(service, jettyService);
    Field f2 = NetworkImpl.class.getDeclaredField("webSocketService");
    f2.setAccessible(true);
    f2.set(service, nettyService);
    Field f3 = NetworkImpl.class.getDeclaredField("kcpChannelService");
    f3.setAccessible(true);
    f3.set(service, kcpChannel);
    Field f4 = NetworkImpl.class.getDeclaredField("socketService");
    f4.setAccessible(true);
    f4.set(service, zeroService);
  }

  @Test
  @DisplayName("Test start() and shutdown() behaviours")
  void testStartAndShutdown() {
    assertDoesNotThrow(() -> service.start());
    assertDoesNotThrow(() -> service.shutdown());
  }
}
