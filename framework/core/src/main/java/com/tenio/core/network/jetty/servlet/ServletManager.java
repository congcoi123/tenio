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

package com.tenio.core.network.jetty.servlet;

import com.tenio.common.data.common.CommonMap;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.define.RestMethod;
import com.tenio.core.network.define.data.PathConfig;
import com.tenio.core.network.jetty.servlet.support.BaseProcessServlet;
import com.tenio.core.network.jetty.servlet.support.BaseServlet;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * The servlet manager.
 */
@ThreadSafe
public final class ServletManager extends BaseServlet {

  private static final long serialVersionUID = -1971993446960398293L;

  /**
   * Event manager
   */
  private final EventManager eventManager;
  /**
   * The class cannot extend {@link AbstractLogger} for logging, so it is necessary to create a
   * private logger instance
   */
  private final PrivateLogger logger;

  /**
   * Post process
   */
  private ProcessPost processPost;
  /**
   * Put process
   */
  private ProcessPut processPut;
  /**
   * Get process
   */
  private ProcessGet processGet;
  /**
   * Delete process
   */
  private ProcessDelete processDelete;

  /**
   * Initialization.
   *
   * @param eventManager the event manager
   * @param pathConfigs  the paths configuration
   */
  public ServletManager(EventManager eventManager, List<PathConfig> pathConfigs) {
    this.eventManager = eventManager;
    logger = new PrivateLogger();
    for (var pathConfig : pathConfigs) {
      switch (pathConfig.getMethod()) {
        case POST:
          processPost = new ProcessPost();
          break;
        case PUT:
          processPut = new ProcessPut();
          break;
        case GET:
          processGet = new ProcessGet();
          break;
        case DELETE:
          processDelete = new ProcessDelete();
          break;
        default:
          break;
      }
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    if (Objects.nonNull(processGet)) {
      processGet.handle(request, response);
    } else {
      sendUnsupportedMethod(response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    if (Objects.nonNull(processPost)) {
      processPost.handle(request, response);
    } else {
      sendUnsupportedMethod(response);
    }
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) {
    if (Objects.nonNull(processPut)) {
      processPut.handle(request, response);
    } else {
      sendUnsupportedMethod(response);
    }
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
    if (Objects.nonNull(processDelete)) {
      processDelete.handle(request, response);
    } else {
      sendUnsupportedMethod(response);
    }
  }

  private void sendUnsupportedMethod(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    try {
      var json = new JSONObject();
      CommonMap.newInstance().add("status", "failed").add("message", "405 Method Not Allowed")
          .forEach(json::put);
      response.getWriter().println(json);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  private final class ProcessPost extends BaseProcessServlet {

    @Override
    protected void handleImpl(HttpServletRequest request, HttpServletResponse response) {
      var check = eventManager.emit(ServerEvent.HTTP_REQUEST_VALIDATION, RestMethod.POST, request,
          response);
      if (Objects.isNull(check)) {
        eventManager.emit(ServerEvent.HTTP_REQUEST_HANDLE, RestMethod.POST, request, response);
      }
    }

  }

  private final class ProcessPut extends BaseProcessServlet {

    @Override
    protected void handleImpl(HttpServletRequest request, HttpServletResponse response) {
      var check = eventManager.emit(ServerEvent.HTTP_REQUEST_VALIDATION, RestMethod.PUT, request,
          response);
      if (Objects.isNull(check)) {
        eventManager.emit(ServerEvent.HTTP_REQUEST_HANDLE, RestMethod.PUT, request, response);
      }
    }

  }

  private final class ProcessGet extends BaseProcessServlet {

    @Override
    protected void handleImpl(HttpServletRequest request, HttpServletResponse response) {
      var check = eventManager.emit(ServerEvent.HTTP_REQUEST_VALIDATION, RestMethod.GET, request,
          response);
      if (Objects.isNull(check)) {
        eventManager.emit(ServerEvent.HTTP_REQUEST_HANDLE, RestMethod.GET, request, response);
      }
    }

  }

  private final class ProcessDelete extends BaseProcessServlet {

    @Override
    protected void handleImpl(HttpServletRequest request, HttpServletResponse response) {
      var check =
          eventManager.emit(ServerEvent.HTTP_REQUEST_VALIDATION, RestMethod.DELETE, request,
              response);
      if (Objects.isNull(check)) {
        eventManager.emit(ServerEvent.HTTP_REQUEST_HANDLE, RestMethod.DELETE, request, response);
      }
    }

  }

  private final class PrivateLogger extends SystemLogger {
  }
}