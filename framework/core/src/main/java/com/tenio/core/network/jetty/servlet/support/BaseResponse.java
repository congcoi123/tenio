/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.jetty.servlet.support;

import com.tenio.common.logger.AbstractLogger;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * The base response for HTTP request.
 */
public abstract class BaseResponse extends AbstractLogger {

  /**
   * Processes a request from client side and returns a response.
   *
   * @param requestedAgent {@link String} value, a requested agent
   * @param request        an instance of {@link HttpServletRequest}
   * @param body           a {@link JSONObject} request's body
   * @param response       the {@link HttpServletResponse} from the server
   */
  public abstract void process(String requestedAgent, HttpServletRequest request, JSONObject body,
                               HttpServletResponse response);

  /**
   * Determines whether a header key is present in a request.
   *
   * @param request an instance of {@link HttpServletRequest}
   * @param key     the checking {@link String} key
   * @return {@code true} if the key is available in the request's header,
   * {@code false} otherwise
   */
  protected boolean hasHeaderKey(HttpServletRequest request, String key) {
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
}
