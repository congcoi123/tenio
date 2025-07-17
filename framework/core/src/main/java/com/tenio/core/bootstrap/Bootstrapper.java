/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

import com.tenio.core.bootstrap.annotation.Bootstrap;
import com.tenio.core.bootstrap.injector.Injector;
import com.tenio.common.logger.SystemLogger;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

/**
 * Handles the initialization and bootstrapping of the game server application.
 * This class is responsible for scanning and initializing components, managing
 * dependency injection, and setting up the application's core services.
 *
 * <p>Key features:
 * <ul>
 *   <li>Component scanning and initialization</li>
 *   <li>Dependency injection management</li>
 *   <li>Command handler registration</li>
 *   <li>Servlet bean management</li>
 *   <li>Singleton instance management</li>
 * </ul>
 *
 * <p>Initialization process:
 * <ol>
 *   <li>Scan specified packages for annotated components</li>
 *   <li>Initialize dependency injection container</li>
 *   <li>Register command handlers</li>
 *   <li>Configure servlet beans</li>
 *   <li>Set up bootstrap handler</li>
 * </ol>
 *
 * <p>Note: This class implements the singleton pattern and should be accessed
 * through the {@link #newInstance()} method.
 *
 * @see Bootstrap
 * @see Injector
 * @see BootstrapHandler
 * @since 0.3.0
 */
public final class Bootstrapper extends SystemLogger {

  private static final Bootstrapper instance = new Bootstrapper();

  private final Injector injector;
  private BootstrapHandler bootstrapHandler;

  private Bootstrapper() {
    if (instance != null) {
      throw new CommandLine.InitializationException("Could not recreate this class instance");
    }
    injector = Injector.newInstance();
  }

  /**
   * Creates a new instance.
   *
   * @return a new instance of {@link Bootstrapper}
   */
  public static Bootstrapper newInstance() {
    return instance;
  }

  /**
   * Start the bootstrapping.
   *
   * @param entryClass a {@link Class} in the root package
   * @param packages   the scanning {@link String} package names
   * @throws Exception when any exceptions occurred
   */
  public void run(Class<?> entryClass, String... packages) throws Exception {
    boolean hasExtApplicationAnnotation = entryClass.isAnnotationPresent(Bootstrap.class);

    if (hasExtApplicationAnnotation) {
      start(entryClass, packages);
      bootstrapHandler = injector.getBean(BootstrapHandler.class);
      bootstrapHandler.createReversedClassesMap(injector.getClassesMap());
      bootstrapHandler.setClassBeansMap(injector.getClassBeansMap());
      bootstrapHandler.setServletMap(injector.getServletBeansMap());
      bootstrapHandler.setSystemCommandManager(injector.getSystemCommandManager());
      bootstrapHandler.setClientCommandManager(injector.getClientCommandManager());
    }
  }

  private void start(Class<?> entryClass, String... packages) {
    try {
      synchronized (Bootstrapper.class) {
        injector.scanPackages(entryClass, packages);
      }
    } catch (Exception exception) {
      if (isErrorEnabled()) {
        error(exception);
      }
    }
  }

  /**
   * Retrieves an instance of bootstrap handler.
   *
   * @return a {@link BootstrapHandler} instance
   */
  public BootstrapHandler getBootstrapHandler() {
    return bootstrapHandler;
  }
}
