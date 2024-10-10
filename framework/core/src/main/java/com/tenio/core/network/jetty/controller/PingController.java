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

package com.tenio.core.network.jetty.controller;

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.bootstrap.annotation.RestController;
import com.tenio.core.bootstrap.annotation.RestMapping;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.network.jetty.response.ApiResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.util.Map;

/**
 * The default servlet to let client checks if the HTTP server is available or not.
 */
@RestController("ping")
public class PingController extends SystemLogger {

  /**
   * A ping method.
   *
   * @return an instance of {@link HttpServlet}
   */
  @RestMapping
  public HttpServlet doPing() {
    return new HttpServlet() {

      @Serial
      private static final long serialVersionUID = 2310114412199639230L;

      @Override
      protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(CoreConstant.CONTENT_TYPE_JSON);
        response.setStatus(HttpServletResponse.SC_OK);
        try {
          var json = ApiResponse.ok(Map.of("message", "PING PONG"));
          response.getWriter().println(json);
        } catch (IOException exception) {
          if (isErrorEnabled()) {
            error(exception);
          }
        }
      }
    };
  }

  /**
   * It is just another ping method.
   *
   * @return an instance of {@link HttpServlet}
   */
  @RestMapping("another")
  public HttpServlet doAnotherPing() {
    return new HttpServlet() {

      @Serial
      private static final long serialVersionUID = 2310114412199639230L;

      @Override
      protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(CoreConstant.CONTENT_TYPE_JSON);
        response.setStatus(HttpServletResponse.SC_OK);
        try {
          var json = ApiResponse.ok(Map.of("message", "PING PONG ANOTHER"));
          response.getWriter().println(json);
        } catch (IOException exception) {
          if (isErrorEnabled()) {
            error(exception);
          }
        }
      }
    };
  }
}
