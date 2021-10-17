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

import com.tenio.common.logger.AbstractLogger;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.network.jetty.servlet.ServletHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The base process servlet.
 */
public abstract class BaseProcessServlet extends AbstractLogger implements ServletHandler {

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response) {
    response.setContentType(CoreConstant.CONTENT_TYPE_JSON);
    response.setContentType(CoreConstant.CONTENT_TYPE_TEXT);
    response.setCharacterEncoding(CoreConstant.UTF_8);
    handleImpl(request, response);
  }

  /**
   * Implements handling.
   *
   * @param request  the request
   * @param response the response
   */
  protected abstract void handleImpl(HttpServletRequest request, HttpServletResponse response);
}
