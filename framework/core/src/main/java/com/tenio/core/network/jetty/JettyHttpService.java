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

package com.tenio.core.network.jetty;

import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.DuplicatedUriAndMethodException;
import com.tenio.core.exception.ServiceRuntimeException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.define.RestMethod;
import com.tenio.core.network.define.data.PathConfig;
import com.tenio.core.network.jetty.servlet.PingServlet;
import com.tenio.core.network.jetty.servlet.ServletManager;
import com.tenio.core.service.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * This class provides methods for creating HTTP service.
 */
public final class JettyHttpService extends AbstractManager implements Service, Runnable {

  private Server server;
  private ExecutorService executorService;
  private int port;
  private List<PathConfig> pathConfigs;
  private boolean initialized;

  private JettyHttpService(EventManager eventManager) {
    super(eventManager);

    initialized = false;
  }

  /**
   * Initialization.
   *
   * @param eventManager an instance of {@link EventManager}
   * @return an instance of {@link JettyHttpService}
   */
  public static JettyHttpService newInstance(EventManager eventManager) {
    return new JettyHttpService(eventManager);
  }

  private void setup() throws DuplicatedUriAndMethodException {
    // Collect the same URI path for one servlet
    Map<String, List<PathConfig>> servlets = new HashMap<>();
    for (var path : pathConfigs) {
      if (!servlets.containsKey(path.getUri())) {
        var servlet = new ArrayList<PathConfig>();
        servlet.add(path);
        servlets.put(path.getUri(), servlet);
      } else {
        servlets.get(path.getUri()).add(path);
      }
    }

    for (var entry : servlets.entrySet()) {
      if (isUriHasDuplicatedMethod(RestMethod.POST, entry.getValue())) {
        throw new DuplicatedUriAndMethodException(RestMethod.POST, entry.getValue());
      }
      if (isUriHasDuplicatedMethod(RestMethod.PUT, entry.getValue())) {
        throw new DuplicatedUriAndMethodException(RestMethod.PUT, entry.getValue());
      }
      if (isUriHasDuplicatedMethod(RestMethod.GET, entry.getValue())) {
        throw new DuplicatedUriAndMethodException(RestMethod.GET, entry.getValue());
      }
      if (isUriHasDuplicatedMethod(RestMethod.DELETE, entry.getValue())) {
        throw new DuplicatedUriAndMethodException(RestMethod.DELETE, entry.getValue());
      }
    }

    // Create a Jetty server
    server = new Server(port);

    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");

    // Configuration
    context.addServlet(new ServletHolder(new PingServlet()), CoreConstant.PING_PATH);
    servlets.forEach(
        (uri, list) -> context.addServlet(new ServletHolder(new ServletManager(eventManager, list)),
            uri));

    server.setHandler(context);
  }

  @Override
  public void run() {
    try {
      info("START SERVICE", buildgen(getName(), " (", 1, ")"));

      info("Http Info",
          buildgen("Started at port: ", port, ", Configuration: ", pathConfigs.toString()));

      server.start();
      server.join();
    } catch (Exception e) {
      error(e);
    }
  }

  private boolean isUriHasDuplicatedMethod(RestMethod method, List<PathConfig> pathConfigs) {
    return
        pathConfigs.stream().filter(pathConfig -> pathConfig.getMethod().equals(method))
            .count() > 1;
  }

  @Override
  public void initialize() {
    try {
      setup();
    } catch (DuplicatedUriAndMethodException e) {
      throw new ServiceRuntimeException(e.getMessage());
    }
    initialized = true;
  }

  @Override
  public void start() {
    if (!initialized) {
      return;
    }

    executorService = Executors.newSingleThreadExecutor();
    executorService.execute(this);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (Objects.nonNull(executorService) && !executorService.isShutdown()) {
        try {
          shutdown();
        } catch (Exception e) {
          error(e);
        }
      }
    }));
  }

  @Override
  public void shutdown() {
    if (!initialized) {
      return;
    }

    try {
      server.stop();
      executorService.shutdownNow();

      info("STOPPED SERVICE", buildgen(getName(), " (", 1, ")"));
      destroy();
      info("DESTROYED SERVICE", buildgen(getName(), " (", 1, ")"));
    } catch (Exception e) {
      error(e);
    }
  }

  private void destroy() {
    server = null;
    executorService = null;
  }

  @Override
  public boolean isActivated() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return "jetty-http";
  }

  @Override
  public void setName(String name) {
    throw new UnsupportedOperationException();
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setPathConfigs(List<PathConfig> pathConfigs) {
    this.pathConfigs = pathConfigs;
  }
}
