/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.configuration.constant.CoreConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

/**
 * The class provides utility methods to work with http request/response.
 *
 * @since 0.5.0
 */
public enum HttpUtility {
  /**
   * Singleton instance.
   */
  INSTANCE;

  /**
   * The class cannot extend {@link AbstractLogger} for logging, so it is necessary to create a
   * private logger instance
   */
  private final PrivateLogger logger = new PrivateLogger();

  /**
   * Determines whether a header key is present in a request.
   *
   * @param request an instance of {@link HttpServletRequest}
   * @param key     the checking {@link String} key
   * @return {@code true} if the key is available in the request's header,
   * {@code false} otherwise
   */
  public boolean hasHeaderKey(HttpServletRequest request, String key) {
    var headerNames = request.getHeaderNames();
    if (Objects.nonNull(headerNames)) {
      while (headerNames.hasMoreElements()) {
        if (headerNames.nextElement().equals(key)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Retrieves a JSON body from coming request.
   *
   * @param request a coming {@link HttpServletRequest}
   * @return a {@link JSONObject} taken from the request
   */
  public JSONObject getBodyJson(HttpServletRequest request) {
    var body = "{}";
    if (request.getMethod().equals("POST") || request.getMethod().equals("PUT")
        || request.getMethod().equals("DELETE")) {
      var builder = new StringBuilder();
      BufferedReader bufferedReader = null;

      try {
        bufferedReader = request.getReader();
        char[] charBuffer = new char[1024];
        int bytesRead;
        while ((bytesRead = bufferedReader.read(charBuffer)) != -1) {
          builder.append(charBuffer, 0, bytesRead);
        }
      } catch (IOException exception) {
        // swallow silently -- can't get body, won't
        logger.error(exception);
      } finally {
        if (Objects.nonNull(bufferedReader)) {
          try {
            bufferedReader.close();
          } catch (IOException exception) {
            // swallow silently -- can't get body, won't
            logger.error(exception);
          }
        }
      }
      body = builder.toString();
    }
    return new JSONObject(body);
  }

  /**
   * Retrieves a JSON body content of a request.
   *
   * @param request an instance of {@link HttpServletRequest}
   * @return a JSON {@link String} contains the body content
   */
  public String getBodyText(HttpServletRequest request) {
    var body = "";
    if (request.getMethod().equals("POST") || request.getMethod().equals("PUT")
        || request.getMethod().equals("DELETE")) {
      var builder = new StringBuilder();
      BufferedReader bufferedReader = null;

      try {
        bufferedReader = request.getReader();
        char[] charBuffer = new char[1024];
        int bytesRead;
        while ((bytesRead = bufferedReader.read(charBuffer)) != -1) {
          builder.append(charBuffer, 0, bytesRead);
        }
      } catch (IOException exception) {
        // swallow silently -- can't get body, won't
        logger.error(exception);
      } finally {
        if (Objects.nonNull(bufferedReader)) {
          try {
            bufferedReader.close();
          } catch (IOException exception) {
            // swallow silently -- can't get body, won't
            logger.error(exception);
          }
        }
      }
      body = builder.toString();
    }
    return body;
  }

  /**
   * Sends the request and gets the response.
   *
   * @param response   an instance of {@link HttpServletResponse}
   * @param statusCode the status code {@link HttpStatus}
   * @param payload    the sending payload data in {@link String}
   */
  public void sendResponseJson(HttpServletResponse response, int statusCode, String payload) {
    response.setContentType(CoreConstant.CONTENT_TYPE_JSON);
    response.setStatus(statusCode);
    try {
      response.getWriter().println(payload);
    } catch (IOException exception) {
      logger.error(exception);
    }
  }
}

class PrivateLogger extends SystemLogger {
}
