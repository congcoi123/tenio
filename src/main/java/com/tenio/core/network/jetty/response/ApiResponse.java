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

package com.tenio.core.network.jetty.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * This class helps convert a map of data to standard response.
 *
 * @since 0.5.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiResponse {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final String timestamp;
  private String status;
  private Map<String, Object> data;
  private Map<String, Object> error;

  private ApiResponse() {
    timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
  }

  /**
   * Forms a response in case of success.
   *
   * @param data a {@link Map} of key-value data
   * @return a response in {@link String}
   */
  public static String ok(Map<String, Object> data) {
    var apiResponse = new ApiResponse();
    apiResponse.status = "success";
    apiResponse.data = data;
    try {
      return objectMapper.writeValueAsString(apiResponse);
    } catch (JsonProcessingException exception) {
      exception.printStackTrace();
    }
    return null;
  }

  /**
   * Forms a response in case of error.
   *
   * @param error a {@link Map} of key-value data
   * @return a response in {@link String}
   */
  public static String error(Map<String, Object> error) {
    var apiResponse = new ApiResponse();
    apiResponse.status = "error";
    apiResponse.error = error;
    try {
      return objectMapper.writeValueAsString(apiResponse);
    } catch (JsonProcessingException exception) {
      exception.printStackTrace();
    }
    return null;
  }

  /**
   * Forms a response in case of non result.
   *
   * @param data a {@link Map} of key-value data
   * @return a response in {@link String}
   */
  public static String noContent(Map<String, Object> data) {
    var apiResponse = new ApiResponse();
    apiResponse.status = "no-content";
    apiResponse.data = data;
    try {
      return objectMapper.writeValueAsString(apiResponse);
    } catch (JsonProcessingException exception) {
      exception.printStackTrace();
    }
    return null;
  }

  /**
   * Retrieves the creation timestamp.
   *
   * @return creation timestamp value in {@link String}
   */
  public String getTimestamp() {
    return timestamp;
  }

  /**
   * Retrieves the status.
   *
   * @return status value in {@link String}
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the response status.
   *
   * @param status the status value in {@link String}
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Retrieves the response content in case of success.
   *
   * @return a {@link Map} of key-value data
   */
  public Map<String, Object> getData() {
    return data;
  }

  /**
   * Sets the response content in case of success.
   *
   * @param data a {@link Map} of content data
   */
  public void setData(Map<String, Object> data) {
    this.data = data;
  }

  /**
   * Retrieves the response content in case of error.
   *
   * @return a {@link Map} of key-value data
   */
  public Map<String, Object> getError() {
    return error;
  }

  /**
   * Sets the response content in case of error.
   *
   * @param error a {@link Map} of content data
   */
  public void setError(Map<String, Object> error) {
    this.error = error;
  }

  @Override
  public String toString() {
    return "ApiResponse{" +
        "timestamp='" + timestamp + '\'' +
        ", status='" + status + '\'' +
        ", data=" + data +
        ", error=" + error +
        '}';
  }
}
