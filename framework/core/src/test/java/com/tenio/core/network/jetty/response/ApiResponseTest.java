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

package com.tenio.core.network.jetty.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ApiResponse")
class ApiResponseTest {

  @Test
  @DisplayName("Test ok() response contains success status")
  void testOkResponseContainsSuccessStatus() {
    String json = ApiResponse.ok(Map.of("message", "hello"));
    assertNotNull(json);
    assertTrue(json.contains("\"status\":\"success\""));
  }

  @Test
  @DisplayName("Test ok() response contains data key")
  void testOkResponseContainsData() {
    String json = ApiResponse.ok(Map.of("key", "value"));
    assertNotNull(json);
    assertTrue(json.contains("\"data\""));
  }

  @Test
  @DisplayName("Test error() response contains error status")
  void testErrorResponseContainsErrorStatus() {
    String json = ApiResponse.error(Map.of("message", "oops"));
    assertNotNull(json);
    assertTrue(json.contains("\"status\":\"error\""));
  }

  @Test
  @DisplayName("Test error() response contains error key")
  void testErrorResponseContainsErrorKey() {
    String json = ApiResponse.error(Map.of("code", "500"));
    assertNotNull(json);
    assertTrue(json.contains("\"error\""));
  }

  @Test
  @DisplayName("Test noContent() response contains no-content status")
  void testNoContentResponseContainsNoContentStatus() {
    String json = ApiResponse.noContent(Map.of("info", "empty"));
    assertNotNull(json);
    assertTrue(json.contains("\"status\":\"no-content\""));
  }

  @Test
  @DisplayName("Test ok() response with null data returns valid JSON")
  void testOkResponseWithNullDataReturnsJson() {
    String json = ApiResponse.ok(null);
    assertNotNull(json);
    assertTrue(json.contains("\"status\":\"success\""));
  }

  @Test
  @DisplayName("Test response JSON contains a timestamp field")
  void testResponseContainsTimestamp() {
    String json = ApiResponse.ok(Map.of("x", 1));
    assertNotNull(json);
    assertTrue(json.contains("\"timestamp\""));
  }

  @Test
  @DisplayName("Test error() response with null data returns valid JSON")
  void testErrorResponseWithNullDataReturnsJson() {
    String json = ApiResponse.error(null);
    assertNotNull(json);
    assertTrue(json.contains("\"status\":\"error\""));
  }

  @Test
  @DisplayName("Test noContent() response with null data returns valid JSON")
  void testNoContentResponseWithNullDataReturnsJson() {
    String json = ApiResponse.noContent(null);
    assertNotNull(json);
    assertTrue(json.contains("\"status\":\"no-content\""));
  }

  private ApiResponse createInstance() throws Exception {
    Constructor<ApiResponse> ctor = ApiResponse.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    return ctor.newInstance();
  }

  @Test
  @DisplayName("getTimestamp returns a non-null ISO timestamp string")
  void testGetTimestampReturnsNonNull() throws Exception {
    ApiResponse instance = createInstance();
    assertNotNull(instance.getTimestamp());
  }

  @Test
  @DisplayName("setStatus and getStatus work correctly")
  void testSetAndGetStatus() throws Exception {
    ApiResponse instance = createInstance();
    instance.setStatus("test-status");
    assertEquals("test-status", instance.getStatus());
  }

  @Test
  @DisplayName("setData and getData work correctly")
  void testSetAndGetData() throws Exception {
    ApiResponse instance = createInstance();
    Map<String, Object> data = Map.of("k", "v");
    instance.setData(data);
    assertEquals(data, instance.getData());
  }

  @Test
  @DisplayName("setError and getError work correctly")
  void testSetAndGetError() throws Exception {
    ApiResponse instance = createInstance();
    Map<String, Object> error = Map.of("code", "500");
    instance.setError(error);
    assertEquals(error, instance.getError());
  }

  @Test
  @DisplayName("getData returns null initially")
  void testGetDataInitiallyNull() throws Exception {
    ApiResponse instance = createInstance();
    assertNull(instance.getData());
  }

  @Test
  @DisplayName("getError returns null initially")
  void testGetErrorInitiallyNull() throws Exception {
    ApiResponse instance = createInstance();
    assertNull(instance.getError());
  }

  @Test
  @DisplayName("toString contains status and timestamp fields")
  void testToStringContainsFields() throws Exception {
    ApiResponse instance = createInstance();
    instance.setStatus("success");
    String str = instance.toString();
    assertTrue(str.contains("status="));
    assertTrue(str.contains("timestamp="));
  }
}
