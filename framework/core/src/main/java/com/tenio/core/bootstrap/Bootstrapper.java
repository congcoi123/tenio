/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.bootstrap.annotation.Bootstrap;
import com.tenio.core.bootstrap.injector.Injector;
import com.tenio.core.bootstrap.injector.ProfileSelection;

import java.util.Set;

/**
 * Handles the initialization and bootstrapping of the game server application. This class is
 * responsible for scanning and initializing components, managing dependency injection, and setting
 * up the application's core services.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Component scanning and initialization
 *   <li>Dependency injection management
 *   <li>Command handler registration
 *   <li>Servlet bean management
 *   <li>Independent bootstrap contexts
 * </ul>
 *
 * <p>Initialization process:
 *
 * <ol>
 *   <li>Scan specified packages for annotated components
 *   <li>Initialize dependency injection container
 *   <li>Register command handlers
 *   <li>Configure servlet beans
 *   <li>Set up bootstrap handler
 * </ol>
 *
 * <p>Each instance owns its injector, bean registrations, and startup profile selection.
 * Repeated runs rebuild that instance's registrations without changing another bootstrapper.
 *
 * @see Bootstrap
 * @see Injector
 * @see BootstrapHandler
 * @since 0.3.0
 */
public final class Bootstrapper extends SystemLogger {

  private final Injector injector;
  private BootstrapHandler bootstrapHandler;
  /** Immutable selection installed before any application constructor runs. */
  private ProfileSelection profileSelection;

  /** Creates a bootstrapper with its own injector and startup profile selection. */
  public Bootstrapper() {
    injector = new Injector();
    profileSelection = new ProfileSelection(Set.of());
  }

  /**
   * Start the bootstrapping.
   *
   * @param entryClass a {@link Class} in the root package
   * @param packages the scanning {@link String} package names
   * @throws Exception when any exceptions occurred
   */
  public void run(Class<?> entryClass, String... packages) throws Exception {
    profileSelection = new ProfileSelection(Set.of());
    injector.setProfiles(profileSelection.activeProfiles());
    runSelected(entryClass, packages);
  }

  private void runSelected(Class<?> entryClass, String... packages) throws Exception {
    bootstrapHandler = null;
    boolean hasExtApplicationAnnotation = entryClass.isAnnotationPresent(Bootstrap.class) && profileSelection.matches(entryClass);

    if (hasExtApplicationAnnotation) {
      start(entryClass, packages);
      bootstrapHandler = injector.getBean(BootstrapHandler.class);
      bootstrapHandler.createReversedClassesMap(injector.getClassesMap());
      bootstrapHandler.setActiveProfiles(profileSelection.activeProfiles());
      bootstrapHandler.setClassBeansMap(injector.getClassBeansMap());
      bootstrapHandler.setServletMap(injector.getServletBeansMap());
      bootstrapHandler.setScheduledJobs(injector.getScheduledJobs());
      bootstrapHandler.setSystemCommandManager(injector.getSystemCommandManager());
      bootstrapHandler.setClientCommandManager(injector.getClientCommandManager());
    } else {
      // An excluded entry point must not expose beans left by an earlier bootstrap generation.
      injector.scanPackages(null);
    }
  }

  /**
   * Starts discovery with the XML-derived selection, independently of injection qualifiers.
   *
   * @param entryClass application entry point
   * @param names active startup profile names
   * @param packages extra packages to scan
   * @throws Exception when discovery, construction or injection fails
   */
  public void runWithProfiles(Class<?> entryClass, java.util.Set<String> names, String... packages) throws Exception {
    profileSelection = new ProfileSelection(names);
    injector.setProfiles(profileSelection.activeProfiles());
    runSelected(entryClass, packages);
  }

  private void start(Class<?> entryClass, String... packages) throws Exception {
    synchronized (Bootstrapper.class) {
      injector.scanPackages(entryClass, packages);
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
