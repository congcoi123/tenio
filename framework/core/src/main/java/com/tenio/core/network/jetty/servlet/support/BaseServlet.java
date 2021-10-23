/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

import com.tenio.common.logger.SystemLogger;
import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;

/**
 * The base HTTP servlet.
 */
public abstract class BaseServlet extends HttpServlet {

  private static final long serialVersionUID = -5030886807666928581L;

  private final PrivateLogger logger = new PrivateLogger();

  protected boolean hasHeaderKey(HttpServletRequest request, String key) {
    var headerNames = request.getHeaderNames();
    if (headerNames != null) {
      while (headerNames.hasMoreElements()) {
        if (headerNames.nextElement().equals(key)) {
          return true;
        }
      }
    }
    return false;
  }

  protected JSONObject getBody(HttpServletRequest request) {
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
      } catch (IOException e) {
        // swallow silently -- can't get body, won't
        logger.error(e);
      } finally {
        if (bufferedReader != null) {
          try {
            bufferedReader.close();
          } catch (IOException e) {
            // swallow silently -- can't get body, won't
            logger.error(e);
          }
        }
      }
      body = builder.toString();
    }
    return new JSONObject(body);
  }

  private final class PrivateLogger extends SystemLogger {
  }
}
