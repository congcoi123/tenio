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

package com.tenio.examples.example8.handler;

import com.tenio.common.bootstrap.annotation.Component;
import com.tenio.common.data.common.CommonMap;
import com.tenio.core.extension.AbstractExtension;
import com.tenio.core.extension.events.EventHttpRequestHandle;
import com.tenio.core.network.define.RestMethod;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@Component
public final class HttpRequestHandler extends AbstractExtension implements EventHttpRequestHandle {

  @Override
  public void handle(RestMethod method, HttpServletRequest request, HttpServletResponse response) {
    var json = new JSONObject();
    CommonMap.newInstance().add("status", "ok").add("message", "handler")
        .forEach((key, value) -> {
          json.put(key, value);
        });
    try {
      response.getWriter().println(json);
    } catch (IOException e) {
      error(e, "handler");
    }
  }
}
