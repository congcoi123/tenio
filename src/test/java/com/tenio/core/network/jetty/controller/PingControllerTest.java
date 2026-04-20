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

package com.tenio.core.network.jetty.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For PingController")
class PingControllerTest {

  private PingController controller;

  @BeforeEach
  void setUp() {
    controller = new PingController();
  }

  @Test
  @DisplayName("Test doPing() returns a non-null HttpServlet")
  void testDoPingReturnsServlet() {
    HttpServlet servlet = controller.doPing();
    assertNotNull(servlet);
  }

  @Test
  @DisplayName("Test doAnotherPing() returns a non-null HttpServlet")
  void testDoAnotherPingReturnsServlet() {
    HttpServlet servlet = controller.doAnotherPing();
    assertNotNull(servlet);
  }

  @Test
  @DisplayName("Test doPing servlet handles doGet request")
  void testDoPingServletDoGet() throws Exception {
    HttpServlet servlet = controller.doPing();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    Method doGet = HttpServlet.class.getDeclaredMethod("doGet",
        HttpServletRequest.class, HttpServletResponse.class);
    doGet.setAccessible(true);
    assertDoesNotThrow(() -> doGet.invoke(servlet, request, response));

    verify(response).setContentType("application/json");
    verify(response).setStatus(HttpServletResponse.SC_OK);
  }

  @Test
  @DisplayName("Test doAnotherPing servlet handles doPost request")
  void testDoAnotherPingServletDoPost() throws Exception {
    HttpServlet servlet = controller.doAnotherPing();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    Method doPost = HttpServlet.class.getDeclaredMethod("doPost",
        HttpServletRequest.class, HttpServletResponse.class);
    doPost.setAccessible(true);
    assertDoesNotThrow(() -> doPost.invoke(servlet, request, response));

    verify(response).setContentType("application/json");
    verify(response).setStatus(HttpServletResponse.SC_OK);
  }

  @Test
  @DisplayName("doPing doGet covers IOException catch when getWriter throws")
  void testDoPingServletDoGetWriterThrowsIOException() throws Exception {
    HttpServlet servlet = controller.doPing();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getWriter()).thenThrow(new IOException("write error"));

    Method doGet = HttpServlet.class.getDeclaredMethod("doGet",
        HttpServletRequest.class, HttpServletResponse.class);
    doGet.setAccessible(true);
    assertDoesNotThrow(() -> doGet.invoke(servlet, request, response));
  }

  @Test
  @DisplayName("doAnotherPing doPost covers IOException catch when getWriter throws")
  void testDoAnotherPingServletDoPostWriterThrowsIOException() throws Exception {
    HttpServlet servlet = controller.doAnotherPing();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getWriter()).thenThrow(new IOException("write error"));

    Method doPost = HttpServlet.class.getDeclaredMethod("doPost",
        HttpServletRequest.class, HttpServletResponse.class);
    doPost.setAccessible(true);
    assertDoesNotThrow(() -> doPost.invoke(servlet, request, response));
  }
}
