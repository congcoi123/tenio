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

package com.tenio.core.bootstrap.event.handlers;

import com.tenio.common.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.common.bootstrap.annotation.Component;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.extension.events.EventHttpRequestHandle;
import com.tenio.core.extension.events.EventHttpRequestValidation;
import com.tenio.core.network.define.RestMethod;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dispatching all events related to HTTP.
 */
@Component
public final class HttpEventHandler {

  @AutowiredAcceptNull
  private EventHttpRequestHandle eventHttpRequestHandle;

  @AutowiredAcceptNull
  private EventHttpRequestValidation eventHttpRequestValidation;

  /**
   * Initialization.
   *
   * @param eventManager the event manager
   */
  public void initialize(EventManager eventManager) {

    var eventHttpRequestHandleOp =
        Optional.ofNullable(eventHttpRequestHandle);
    var eventHttpRequestValidatedOp =
        Optional.ofNullable(eventHttpRequestValidation);

    eventHttpRequestValidatedOp.ifPresent(
        event -> eventManager.on(ServerEvent.HTTP_REQUEST_VALIDATION, params -> {
          var method = (RestMethod) params[0];
          var request = (HttpServletRequest) params[1];
          var response = (HttpServletResponse) params[2];

          return event.handle(method, request, response);
        }));

    eventHttpRequestHandleOp.ifPresent(event -> eventManager.on(ServerEvent.HTTP_REQUEST_HANDLE,
        params -> {
          var method = (RestMethod) params[0];
          var request = (HttpServletRequest) params[1];
          var response = (HttpServletResponse) params[2];

          event.handle(method, request, response);

          return null;
        }));
  }
}
