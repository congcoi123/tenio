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

package com.tenio.core.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Unit Test Cases For HttpUtility")
class HttpUtilityTest {

  @Test
  @DisplayName("Test all enum values")
  void testAllEnumValues() {
    for (HttpUtility util : HttpUtility.values()) {
      assertEquals(util.name(), util.toString());
    }
  }

  @Test
  @DisplayName("Test checking header keys")
  void testHasHeaderKey() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Enumeration<String> headers =
        Collections.enumeration(Collections.singletonList("X-Test-Header"));
    Mockito.when(request.getHeaderNames()).thenReturn(headers);
    assertTrue(HttpUtility.INSTANCE.hasHeaderKey(request, "X-Test-Header"));
    assertFalse(HttpUtility.INSTANCE.hasHeaderKey(request, "Non-Existent"));
  }

  @Test
  @DisplayName("Test getting body in JSON format")
  void testGetBodyJson() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("POST");
    String json = "{\"key\":\"value\"}";
    BufferedReader reader = new BufferedReader(new StringReader(json));
    Mockito.when(request.getReader()).thenReturn(reader);
    JSONObject obj = HttpUtility.INSTANCE.getBodyJson(request);
    assertEquals("value", obj.getString("key"));
  }

  @Test
  @DisplayName("Test getting body in PLAIN TEXT format")
  void testGetBodyText() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("POST");
    String text = "plain text body";
    BufferedReader reader = new BufferedReader(new StringReader(text));
    Mockito.when(request.getReader()).thenReturn(reader);
    String result = HttpUtility.INSTANCE.getBodyText(request);
    assertEquals(text, result);
  }

  @Test
  @DisplayName("Test sending response in JSON format")
  void testSendResponseJson() throws Exception {
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    Mockito.when(response.getWriter()).thenReturn(pw);
    HttpUtility.INSTANCE.sendResponseJson(response, 200, "{\"ok\":true}");
    Mockito.verify(response).setContentType(Mockito.anyString());
    Mockito.verify(response).setStatus(200);
    pw.flush();
    assertTrue(sw.toString().contains("ok"));
  }
}
