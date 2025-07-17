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

package com.tenio.core.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import com.tenio.core.bootstrap.injector.BeanClass;
import com.tenio.core.command.client.ClientCommandManager;
import com.tenio.core.command.system.SystemCommandManager;
import jakarta.servlet.http.HttpServlet;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For BootstrapHandler")
class BootstrapHandlerTest {

  @Test
  @DisplayName("Test Constructor")
  void testConstructor() {
    BootstrapHandler actualBootstrapHandler = new BootstrapHandler();
    assertNull(actualBootstrapHandler.getConfigurationHandler());
    assertNull(actualBootstrapHandler.getEventHandler());
    assertNull(actualBootstrapHandler.getSystemCommandManager());
  }

  @Test
  @DisplayName("Test systemCommandManager getter/setter")
  void testSystemCommandManager() {
    BootstrapHandler handler = new BootstrapHandler();
    SystemCommandManager scm = mock(SystemCommandManager.class);
    handler.setSystemCommandManager(scm);
    assertSame(scm, handler.getSystemCommandManager());
  }

  @Test
  @DisplayName("Test clientCommandManager getter/setter")
  void testClientCommandManager() {
    BootstrapHandler handler = new BootstrapHandler();
    ClientCommandManager ccm = mock(ClientCommandManager.class);
    handler.setClientCommandManager(ccm);
    assertSame(ccm, handler.getClientCommandManager());
  }

  @Test
  @DisplayName("Test servletMap getter/setter")
  void testServletMap() {
    BootstrapHandler handler = new BootstrapHandler();
    Map<String, HttpServlet> map = new HashMap<>();
    HttpServlet servlet = mock(HttpServlet.class);
    map.put("test", servlet);
    handler.setServletMap(map);
    assertSame(map, handler.getServletMap());
  }

  @Test
  @DisplayName("Test createReversedClassesMap and setClassBeansMap")
  void testCreateReversedClassesMapAndSetClassBeansMap() {
    BootstrapHandler handler = new BootstrapHandler();
    Map<Class<?>, Class<?>> classesMap = new HashMap<>();
    classesMap.put(String.class, Object.class);
    handler.createReversedClassesMap(classesMap);
    Map<BeanClass, Object> beansMap = new HashMap<>();
    beansMap.put(new BeanClass(String.class, ""), "bean");
    handler.setClassBeansMap(beansMap);
    // getBeanByClazz should return null (no reversed mapping for String.class)
    assertNull(handler.getBeanByClazz(String.class));
    // Now, reversed mapping for Object.class -> String.class, so getBeanByClazz(Object.class) should return "bean"
    assertEquals("bean", handler.getBeanByClazz(Object.class));
  }

  @Test
  @DisplayName("Test getBeanByClazz with name")
  void testGetBeanByClazzWithName() {
    BootstrapHandler handler = new BootstrapHandler();
    Map<Class<?>, Class<?>> classesMap = new HashMap<>();
    classesMap.put(String.class, Object.class);
    handler.createReversedClassesMap(classesMap);
    Map<BeanClass, Object> beansMap = new HashMap<>();
    beansMap.put(new BeanClass(String.class, "special"), "specialBean");
    handler.setClassBeansMap(beansMap);
    assertEquals("specialBean", handler.getBeanByClazz(Object.class, "special"));
    assertNull(handler.getBeanByClazz(Object.class, "notfound"));
  }

  @Test
  @DisplayName("Test getBeanByClazz returns null for missing mapping")
  void testGetBeanByClazzReturnsNullForMissingMapping() {
    BootstrapHandler handler = new BootstrapHandler();
    // Initialize empty maps to avoid NPE
    handler.createReversedClassesMap(new HashMap<>());
    handler.setClassBeansMap(new HashMap<>());
    assertNull(handler.getBeanByClazz(Object.class));
  }

  @Test
  @DisplayName("Test getConfigurationHandler and getEventHandler (default null)")
  void testGetConfigurationHandlerAndEventHandler() {
    BootstrapHandler handler = new BootstrapHandler();
    assertNull(handler.getConfigurationHandler());
    assertNull(handler.getEventHandler());
  }
}
