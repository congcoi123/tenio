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

package com.tenio.core.bootstrap;

import com.tenio.core.bootstrap.annotation.Autowired;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.bootstrap.configuration.ConfigurationHandler;
import com.tenio.core.command.client.ClientCommandManager;
import com.tenio.core.command.system.SystemCommandManager;
import com.tenio.core.event.handler.EventHandler;
import java.util.Map;
import javax.servlet.http.HttpServlet;

/**
 * This class provides instances for the events handler and the configuration setups.
 */
@Component
public final class BootstrapHandler {

  @Autowired
  private EventHandler eventHandler;

  @Autowired
  private SystemCommandManager systemCommandManager;

  @Autowired
  private ClientCommandManager clientCommandManager;

  @Autowired
  private ConfigurationHandler configurationHandler;

  private Map<String, HttpServlet> servletMap;

  /**
   * Retrieves an events handler.
   *
   * @return the {@link EventHandler} instance
   */
  public EventHandler getEventHandler() {
    return eventHandler;
  }

  /**
   * Retrieves a system commands' manager.
   *
   * @return the {@link SystemCommandManager} instance
   * @since 0.4.0
   */
  public SystemCommandManager getSystemCommandManager() {
    return systemCommandManager;
  }

  /**
   * Retrieves a client commands' manager.
   *
   * @return the {@link ClientCommandManager} instance
   * @since 0.5.0
   */
  public ClientCommandManager getClientCommandManager() {
    return clientCommandManager;
  }

  /**
   * Retrieves a configuration setups.
   *
   * @return the {@link ConfigurationHandler} instance
   */
  public ConfigurationHandler getConfigurationHandler() {
    return configurationHandler;
  }

  /**
   * Retrieves servlet configuration.
   *
   * @return a {@link Map} of servlet configuration
   */
  public Map<String, HttpServlet> getServletMap() {
    return servletMap;
  }

  /**
   * Sets servlet configuration.
   *
   * @param servletMap a {@link Map} of servlet configurations
   */
  public void setServletMap(Map<String, HttpServlet> servletMap) {
    this.servletMap = servletMap;
  }

  /**
   * Sets the system command manager.
   *
   * @param systemCommandManager an instance of {@link SystemCommandManager}
   */
  public void setSystemCommandManager(SystemCommandManager systemCommandManager) {
    this.systemCommandManager = systemCommandManager;
  }

  /**
   * Sets the client command manager.
   *
   * @param clientCommandManager an instance of {@link ClientCommandManager}
   */
  public void setClientCommandManager(ClientCommandManager clientCommandManager) {
    this.clientCommandManager = clientCommandManager;
  }
}
