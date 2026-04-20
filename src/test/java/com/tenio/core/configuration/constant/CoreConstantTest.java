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

package com.tenio.core.configuration.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For CoreConstant")
class CoreConstantTest {

  @Test
  @DisplayName("Test UTF_8 constant value")
  void testUtf8Constant() {
    assertEquals("UTF-8", CoreConstant.UTF_8);
  }

  @Test
  @DisplayName("Test CONTENT_TYPE_JSON constant value")
  void testContentTypeJsonConstant() {
    assertEquals("application/json", CoreConstant.CONTENT_TYPE_JSON);
  }

  @Test
  @DisplayName("Test CONTENT_TYPE_TEXT constant value")
  void testContentTypeTextConstant() {
    assertEquals("text/html", CoreConstant.CONTENT_TYPE_TEXT);
  }

  @Test
  @DisplayName("Test DEFAULT_CONFIGURATION_FILE constant value")
  void testDefaultConfigurationFileConstant() {
    assertEquals("configuration.tenio.xml", CoreConstant.DEFAULT_CONFIGURATION_FILE);
  }

  @Test
  @DisplayName("Test DEFAULT_KEY_UDP_CONVEY_ID constant value")
  void testDefaultKeyUdpConveyIdConstant() {
    assertEquals("u", CoreConstant.DEFAULT_KEY_UDP_CONVEY_ID);
  }

  @Test
  @DisplayName("Test DEFAULT_KEY_UDP_MESSAGE_DATA constant value")
  void testDefaultKeyUdpMessageDataConstant() {
    assertEquals("d", CoreConstant.DEFAULT_KEY_UDP_MESSAGE_DATA);
  }

  @Test
  @DisplayName("Test NULL_PORT_VALUE constant value")
  void testNullPortValueConstant() {
    assertEquals(-1, CoreConstant.NULL_PORT_VALUE);
  }

  @Test
  @DisplayName("Test DEFAULT_PROCESSOR_THREAD_POOL_SIZE constant value")
  void testDefaultProcessorThreadPoolSizeConstant() {
    assertEquals(1, CoreConstant.DEFAULT_PROCESSOR_THREAD_POOL_SIZE);
  }

  @Test
  @DisplayName("Test DEFAULT_ENGINE_THREAD_POOL_SIZE constant value")
  void testDefaultEngineThreadPoolSizeConstant() {
    assertEquals(1, CoreConstant.DEFAULT_ENGINE_THREAD_POOL_SIZE);
  }

  @Test
  @DisplayName("Test DEFAULT_NUMBER_HTTP_WORKERS constant value")
  void testDefaultNumberHttpWorkersConstant() {
    assertEquals(8, CoreConstant.DEFAULT_NUMBER_HTTP_WORKERS);
  }

  @Test
  @DisplayName("Test DEFAULT_BOOTSTRAP_PACKAGE constant is non-empty")
  void testDefaultBootstrapPackageConstant() {
    assertTrue(CoreConstant.DEFAULT_BOOTSTRAP_PACKAGE.startsWith("com.tenio"));
  }

  @Test
  @DisplayName("Test private constructor throws UnsupportedOperationException")
  void testPrivateConstructorThrows() throws Exception {
    Constructor<CoreConstant> constructor = CoreConstant.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }
}
