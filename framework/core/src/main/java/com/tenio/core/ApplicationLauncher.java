/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.core;

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.bootstrap.Bootstrapper;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.configuration.constant.Trademark;
import com.tenio.core.monitoring.system.SystemInfo;
import com.tenio.core.server.ServerImpl;
import java.util.Arrays;
import java.util.Objects;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.apache.logging.log4j.util.Strings;

/**
 * The application will start from here.
 */
public final class ApplicationLauncher extends SystemLogger {

  private static final ApplicationLauncher instance = new ApplicationLauncher();

  private ApplicationLauncher() {
    if (Objects.nonNull(instance)) {
      throw new CommandLine.InitializationException("Could not recreate this class instance");
    }
  }

  /**
   * Run the application.
   *
   * @param entryClass the {@link Class} which is placed in the root package
   * @param params     the additional parameters
   */
  public static void run(Class<?> entryClass, String[] params) {
    var application = ApplicationLauncher.newInstance();
    application.start(entryClass, params);
  }

  private static ApplicationLauncher newInstance() {
    return instance;
  }

  /**
   * Start the game server with DI mechanism.
   *
   * @param entryClass the {@link Class} which is placed in the root package
   * @param params     the additional parameters
   */
  public void start(Class<?> entryClass, String[] params) {
    // print out the framework's preface
    var trademark =
        String.format("\n\n%s\n", Strings.join(Arrays.asList(Trademark.CONTENT), '\n'));
    info("HAPPY CODING", trademark);

    // show system information
    var systemInfo = new SystemInfo();
    systemInfo.logSystemInfo();
    systemInfo.logNetCardsInfo();
    systemInfo.logDiskInfo();

    Bootstrapper bootstrap = null;
    if (Objects.nonNull(entryClass)) {
      bootstrap = Bootstrapper.newInstance();
      try {
        bootstrap.run(entryClass, CoreConstant.DEFAULT_BOOTSTRAP_PACKAGE,
            CoreConstant.DEFAULT_EVENT_PACKAGE, CoreConstant.DEFAULT_COMMAND_PACKAGE,
            CoreConstant.DEFAULT_REST_CONTROLLER_PACKAGE);
      } catch (Exception exception) {
        error(exception, "The application started with exceptions occurred: ", exception.getMessage());
        System.exit(1);
      }
    }

    var server = ServerImpl.getInstance();
    try {
      assert bootstrap != null;
      server.start(bootstrap.getBootstrapHandler(), params);
    } catch (Exception exception) {
      error(exception, "The application started with exceptions occurred: ",
          exception.getMessage());
      server.shutdown();
      // exit with errors
      System.exit(1);
    }

    // Keep the main thread running
    try {
      var currentThread = Thread.currentThread();
      currentThread.setName("tenio-main-thread");
      currentThread.setUncaughtExceptionHandler((thread, cause) -> error(cause, thread.getName()));
      currentThread.join();
    } catch (InterruptedException exception) {
      error(exception);
    }

    // Suddenly shutdown
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      server.shutdown();
      System.exit(0);
    }));
  }
}
