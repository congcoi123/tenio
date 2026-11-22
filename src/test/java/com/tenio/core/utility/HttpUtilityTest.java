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

package com.tenio.core.utility;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
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

  @Test
  @DisplayName("Test hasHeaderKey returns false when headerNames is null")
  void testHasHeaderKeyNullHeaderNames() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getHeaderNames()).thenReturn(null);
    assertFalse(HttpUtility.INSTANCE.hasHeaderKey(request, "X-Any-Header"));
  }

  @Test
  @DisplayName("Test getBodyJson returns empty object for GET request")
  void testGetBodyJsonForGetRequest() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("GET");
    JSONObject obj = HttpUtility.INSTANCE.getBodyJson(request);
    assertEquals(0, obj.length());
  }

  @Test
  @DisplayName("Test getBodyText returns empty string for GET request")
  void testGetBodyTextForGetRequest() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("GET");
    String result = HttpUtility.INSTANCE.getBodyText(request);
    assertEquals("", result);
  }

  @Test
  @DisplayName("Test getBodyJson parses body for PUT request")
  void testGetBodyJsonForPutRequest() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("PUT");
    String json = "{\"key\":\"updated\"}";
    Mockito.when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
    JSONObject obj = HttpUtility.INSTANCE.getBodyJson(request);
    assertEquals("updated", obj.getString("key"));
  }

  @Test
  @DisplayName("Test getBodyJson parses body for DELETE request")
  void testGetBodyJsonForDeleteRequest() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("DELETE");
    String json = "{\"id\":42}";
    Mockito.when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
    JSONObject obj = HttpUtility.INSTANCE.getBodyJson(request);
    assertEquals(42, obj.getInt("id"));
  }

  @Test
  @DisplayName("Test getBodyText parses body for PUT request")
  void testGetBodyTextForPutRequest() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("PUT");
    String text = "put body";
    Mockito.when(request.getReader()).thenReturn(new BufferedReader(new StringReader(text)));
    String result = HttpUtility.INSTANCE.getBodyText(request);
    assertEquals(text, result);
  }

  @Test
  @DisplayName("Test getBodyText parses body for DELETE request")
  void testGetBodyTextForDeleteRequest() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("DELETE");
    String text = "delete body";
    Mockito.when(request.getReader()).thenReturn(new BufferedReader(new StringReader(text)));
    String result = HttpUtility.INSTANCE.getBodyText(request);
    assertEquals(text, result);
  }

  @Test
  @DisplayName("Test getBodyText returns empty string when reader throws IOException")
  void testGetBodyTextIOException() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("POST");
    Mockito.when(request.getReader()).thenThrow(new java.io.IOException("read error"));
    String result = HttpUtility.INSTANCE.getBodyText(request);
    assertEquals("", result);
  }

  @Test
  @DisplayName("Test sendResponseJson when getWriter throws IOException does not propagate")
  void testSendResponseJsonWriterIOException() throws Exception {
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(response.getWriter()).thenThrow(new java.io.IOException("write error"));
    // Should not throw - exception is caught internally
    HttpUtility.INSTANCE.sendResponseJson(response, 200, "payload");
  }

  @Test
  @DisplayName("getBodyText when reader.read() throws covers finally close branch")
  void testGetBodyTextReadThrowsIOExceptionClosesReader() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("POST");
    BufferedReader reader = Mockito.mock(BufferedReader.class);
    Mockito.when(request.getReader()).thenReturn(reader);
    Mockito.when(reader.read(any(char[].class))).thenThrow(new IOException("read error"));
    String result = HttpUtility.INSTANCE.getBodyText(request);
    verify(reader).close();
    assertEquals("", result);
  }

  @Test
  @DisplayName("getBodyText when close() throws IOException covers inner catch branch")
  void testGetBodyTextCloseThrowsIOException() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("POST");
    BufferedReader reader = Mockito.mock(BufferedReader.class);
    Mockito.when(request.getReader()).thenReturn(reader);
    Mockito.when(reader.read(any(char[].class))).thenThrow(new IOException("read error"));
    doThrow(new IOException("close error")).when(reader).close();
    // should not propagate the close exception
    String result = HttpUtility.INSTANCE.getBodyText(request);
    assertEquals("", result);
  }

  @Test
  @DisplayName("getBodyJson when getReader() throws IOException covers outer catch branch")
  void testGetBodyJsonReaderThrowsIOException() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("POST");
    Mockito.when(request.getReader()).thenThrow(new IOException("read error"));
    assertThrows(Exception.class, () -> HttpUtility.INSTANCE.getBodyJson(request));
  }

  @Test
  @DisplayName("getBodyJson when read() throws and close() also throws covers inner catch")
  void testGetBodyJsonReadAndCloseBothThrow() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("POST");
    BufferedReader reader = Mockito.mock(BufferedReader.class);
    Mockito.when(request.getReader()).thenReturn(reader);
    Mockito.when(reader.read(any(char[].class))).thenThrow(new IOException("read fail"));
    doThrow(new IOException("close fail")).when(reader).close();
    // body remains "" after read() throws; new JSONObject("") throws JSONException
    assertThrows(Exception.class, () -> HttpUtility.INSTANCE.getBodyJson(request));
  }
}
