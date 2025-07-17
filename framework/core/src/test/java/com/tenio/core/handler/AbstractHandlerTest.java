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

package com.tenio.core.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.data.DataType;
import com.tenio.core.api.ServerApi;
import com.tenio.core.command.client.ClientCommandManager;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.exception.UnsupportedDataTypeInUseException;
import com.tenio.core.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For AbstractHandler")
class AbstractHandlerTest {

  private Server server;
  private AbstractHandler handler;

  @BeforeEach
  void setUp() throws Exception {
    server = mock(Server.class);
    handler = new TestHandler(server);
    // Inject mock server into private final field using reflection
    var field = AbstractHandler.class.getDeclaredField("server");
    field.setAccessible(true);
    field.set(handler, server);
  }

  @Test
  @DisplayName("Test configuration setter/getter")
  void testConfiguration() {
    Configuration config = mock(Configuration.class);
    when(server.getConfiguration()).thenReturn(config);
    assertEquals(config, handler.configuration());
  }

  @Test
  @DisplayName("Test serverApi setter/getter")
  void testApi() {
    ServerApi api = mock(ServerApi.class);
    when(server.getApi()).thenReturn(api);
    assertEquals(api, handler.api());
  }

  @Test
  @DisplayName("Test clientCommand setter/getter")
  void testClientCommand() {
    ClientCommandManager ccm = mock(ClientCommandManager.class);
    when(server.getClientCommandManager()).thenReturn(ccm);
    assertEquals(ccm, handler.clientCommand());
  }

  @Test
  @DisplayName("Test response setter/getter")
  void testResponse() {
    assertNotNull(handler.response());
  }

  @Test
  @DisplayName("Test array() and map() methods in case of data type ZERO")
  void testArrayAndMapWithZeroDataType() {
    when(server.getDataType()).thenReturn(DataType.ZERO);
    assertNotNull(handler.array());
    assertNotNull(handler.map());
  }

  @Test
  @DisplayName("Invoke array() method in case of data type MSGPACK should throw exception")
  void testArrayThrowsForNonZeroDataType() {
    when(server.getDataType()).thenReturn(DataType.MSG_PACK);
    assertThrows(UnsupportedDataTypeInUseException.class, () -> handler.array());
  }

  @Test
  @DisplayName("Invoke map() method in case of data type MSGPACK should throw exception")
  void testMapThrowsForNonZeroDataType() {
    when(server.getDataType()).thenReturn(DataType.MSG_PACK);
    assertThrows(UnsupportedDataTypeInUseException.class, () -> handler.map());
  }

  @Test
  @DisplayName("Test msgmap() method in case of data type MSGPACK")
  void testMsgmapWithMsgPackDataType() {
    when(server.getDataType()).thenReturn(DataType.MSG_PACK);
    assertNotNull(handler.msgmap());
  }

  @Test
  @DisplayName("Invoke msgmap() method in case of data type ZERO should throw exception")
  void testMsgmapThrowsForNonMsgPackDataType() {
    when(server.getDataType()).thenReturn(DataType.ZERO);
    assertThrows(UnsupportedDataTypeInUseException.class, () -> handler.msgmap());
  }

  @Test
  @DisplayName("Test roomSetting() method")
  void testRoomSetting() {
    assertNotNull(handler.roomSetting());
    assertInstanceOf(InitialRoomSetting.Builder.class, handler.roomSetting());
  }

  static class TestHandler extends AbstractHandler {
    private final Server injectedServer;

    TestHandler(Server server) {
      this.injectedServer = server;
    }

    // No @Override here, just for test injection
    protected Server getInjectedServer() {
      return injectedServer;
    }
  }
}
