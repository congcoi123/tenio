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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.service.Service;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.http.HttpServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * This class provides methods for creating HTTP service.
 */
public final class JettyHttpService extends AbstractManager implements Service, Runnable {

  private Server server;
  private ExecutorService executorService;
  private int threadPoolSize;
  private int port;
  private Map<String, HttpServlet> servletMap;
  private volatile boolean initialized;
  private boolean stopping;

  private JettyHttpService(EventManager eventManager) {
    super(eventManager);
    initialized = false;
    stopping = false;
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

  private void setup() {
    // Create a Jetty server
    var threadPool = new QueuedThreadPool(threadPoolSize, 8);
    threadPool.setName(getName());
    threadPool.setDaemon(true);
    server = new Server(threadPool);
    var connector = new ServerConnector(server);
    connector.setPort(port);
    server.addConnector(connector);

    var context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");

    // Configuration
    servletMap.forEach(
        (uri, servlet) -> context.addServlet(new ServletHolder(servlet), "/" + uri));

    server.setHandler(context);
  }

  @Override
  public void run() {
    try {
      if (isInfoEnabled()) {
        info("START SERVICE", buildgen(getName(), " (", 1, ")"));
        info("Http Info",
            buildgen("Started at port: ", port, ", Endpoints: ", servletMap.keySet().toString()));
      }

      server.start();
      server.join();
    } catch (Throwable cause) {
      if (isErrorEnabled()) {
        error(cause);
      }
    }
  }


  @Override
  public void initialize() {
    setup();
    initialized = true;
  }

  @Override
  public void start() {
    if (!initialized) {
      return;
    }

    var threadFactory =
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("jetty-http-management-%d").build();
    executorService = Executors.newSingleThreadExecutor(threadFactory);
    executorService.execute(this);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (Objects.nonNull(executorService) && !executorService.isShutdown()) {
        try {
          shutdown();
        } catch (Exception exception) {
          if (isErrorEnabled()) {
            error(exception);
          }
        }
      }
    }));
  }

  @Override
  public void shutdown() {
    if (!initialized) {
      return;
    }

    if (stopping) {
      return;
    }

    stopping = true;

    try {
      server.stop();
      executorService.shutdownNow();

      if (isInfoEnabled()) {
        info("STOPPED SERVICE", buildgen(getName(), " (", 1, ")"));
      }
    } catch (Exception exception) {
      if (isErrorEnabled()) {
        error(exception);
      }
    }
  }

  @Override
  public boolean isActivated() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return "jetty-http-worker";
  }

  @Override
  public void setName(String name) {
    throw new UnsupportedOperationException();
  }

  /**
   * Sets the thread pool size.
   *
   * @param threadPoolSize thread pool size value
   */
  public void setThreadPoolSize(int threadPoolSize) {
    this.threadPoolSize = threadPoolSize;
  }

  /**
   * Sets the port number.
   *
   * @param port the port number
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Sets the servlet configuration.
   *
   * @param servletMap a {@link Map} of the servlet configuration map
   */
  public void setServletMap(Map<String, HttpServlet> servletMap) {
    this.servletMap = servletMap;
  }
}
