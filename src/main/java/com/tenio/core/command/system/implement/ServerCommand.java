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

package com.tenio.core.command.system.implement;

import com.tenio.core.bootstrap.annotation.SystemCommand;
import com.tenio.core.command.system.AbstractSystemCommandHandler;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.utility.CommandUtility;
import com.tenio.core.server.ServerImpl;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

/**
 * Allows stopping or restarting the server.
 *
 * @since 0.4.0
 */
@SystemCommand(label = "server", profile = CoreConstant.MONITOR_PROFILE,
    usage = {"stop", "restart"},
    description = "Stops the server or requests a restart from its supervisor")
public final class ServerCommand extends AbstractSystemCommandHandler {

  private static final int RESTART_EXIT_CODE = 75;
  private final AtomicBoolean stopping = new AtomicBoolean();
  private final Runnable shutdown;
  private final BooleanSupplier restartEnabled;
  private final IntConsumer exit;

  public ServerCommand() {
    this(() -> ServerImpl.getInstance().shutdown(),
        () -> Boolean.getBoolean("tenio.supervisedRestart"), System::exit);
  }

  ServerCommand(Runnable shutdown, BooleanSupplier restartEnabled, IntConsumer exit) {
    this.shutdown = shutdown;
    this.restartEnabled = restartEnabled;
    this.exit = exit;
  }

  @Override
  public void execute(List<String> arguments) {
    String action = arguments.size() == 1 ? arguments.getFirst().toLowerCase(Locale.ROOT) : "";
    if (!action.equals("stop") && !action.equals("restart")) {
      CommandUtility.INSTANCE.showConsoleMessage("Usage: server stop | server restart");
      return;
    }
    boolean restart = action.equals("restart");
    if (restart && !restartEnabled.getAsBoolean()) {
      CommandUtility.INSTANCE.showConsoleMessage(
          "Restart requires a supervisor configured for exit code 75 and -Dtenio.supervisedRestart=true.");
      return;
    }
    if (!stopping.compareAndSet(false, true)) {
      CommandUtility.INSTANCE.showConsoleMessage("Server shutdown is already in progress.");
      return;
    }
    CommandUtility.INSTANCE.showConsoleMessage(restart ? "Requesting restart." : "Stopping server.");
    // The lifecycle thread can close the command executor without waiting for itself.
    Thread.ofPlatform().daemon(false).name("tenio-admin-shutdown").start(() -> {
      shutdown.run();
      if (restart) {
        exit.accept(RESTART_EXIT_CODE);
      }
    });
  }
}
