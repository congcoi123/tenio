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

package com.tenio.core.command.system;

import java.util.function.Consumer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.bootstrap.annotation.SystemCommand;
import com.tenio.core.exception.AddedDuplicatedCommandException;
import com.tenio.core.utility.CommandUtility;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The commands' management class.
 *
 * @since 0.4.0
 */
public final class SystemCommandManager extends SystemLogger {

  private final Map<String, AbstractSystemCommandHandler> commands = new HashMap<>();
  private final Map<String, SystemCommand> annotations = new HashMap<>();
  private final ExecutorService executors;
  private final AtomicBoolean stopping;

  /**
   * Constructor.
   */
  public SystemCommandManager() {
    var threadFactoryWorker = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("system-command-worker-%d").build();
    executors = Executors.newCachedThreadPool(threadFactoryWorker);
    stopping = new AtomicBoolean(false);
  }

  /**
   * Registers a command handler.
   *
   * @param label   The command label
   * @param command The command handler
   */
  public void registerCommand(String label, AbstractSystemCommandHandler command) {
    label = label.toLowerCase();

    // checks availability
    if (commands.containsKey(label)) {
      throw new AddedDuplicatedCommandException(label, commands.get(label));
    }

    // gets command data
    var annotation = command.getClass().getAnnotation(SystemCommand.class);
    annotations.put(label, annotation);
    commands.put(label, command);
  }

  /**
   * Retrieves the annotations list.
   *
   * @return a {@link List} of annotations
   * @see SystemCommand
   */
  public List<SystemCommand> getAnnotationsAsList() {
    return new LinkedList<>(annotations.values());
  }

  /**
   * Retrieves the annotations map.
   *
   * @return a {@link Map} of annotations
   * @see SystemCommand
   */
  public Map<String, SystemCommand> getAnnotations() {
    return new LinkedHashMap<>(annotations);
  }

  /**
   * Returns a list of all registered commands.
   *
   * @return all command handlers as a list
   */
  public List<AbstractSystemCommandHandler> getHandlersAsList() {
    return new LinkedList<>(commands.values());
  }

  /**
   * Retrieves all handlers.
   *
   * @return a {@link Map} of all handlers which are managed
   */
  public Map<String, AbstractSystemCommandHandler> getHandlers() {
    return commands;
  }

  /**
   * Returns a handler by its label
   *
   * @param label The command label
   * @return the command handler
   */
  public AbstractSystemCommandHandler getHandler(String label) {
    return commands.get(label);
  }

  /**
   * Invokes a command handler with given arguments.
   *
   * @param rawMessage The messaged used to invoke the command
   */
  public void invoke(String rawMessage) {
    invoke(rawMessage, null);
  }

  public void invoke(String rawMessage, Consumer<String> output) {
    if (stopping.get()) {
      return;
    }

    rawMessage = rawMessage.trim();
    if (rawMessage.isBlank()) {
      return;
    }

    // parses message
    var split = rawMessage.split("\\s+");
    var args = new LinkedList<>(Arrays.asList(split));
    var label = args.remove(0).toLowerCase(Locale.ROOT);

    // gets command handler
    var handler = getHandler(label);

    // checks if the handler is null
    if (handler == null) {
      return;
    }

    // gets the command's annotation
    var annotation = annotations.get(label);

    // invokes execute method for handler
    Runnable runnable = output == null ? () -> handler.execute(args)
        : () -> CommandUtility.INSTANCE.withOutput(output, () -> {
          try {
            handler.execute(args);
          } catch (RuntimeException failure) {
            output.accept(failure.toString());
          }
        });
    if (annotation.isBackgroundRunning()) {
      executors.execute(runnable);
      if (output == null) {
        CommandUtility.INSTANCE.showConsoleMessage("The process is running in background.");
      } else {
        output.accept("The process is running in background.");
      }
    } else {
      runnable.run();
    }
  }

  /**
   * Stops background command execution and waits briefly for running commands to finish.
   */
  public void shutdown() {
    if (!stopping.compareAndSet(false, true)) {
      return;
    }

    executors.shutdown();
    try {
      if (!executors.awaitTermination(10, TimeUnit.SECONDS)) {
        executors.shutdownNow();
      }
    } catch (InterruptedException exception) {
      executors.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Clear all settings.
   */
  public void clear() {
    commands.clear();
    annotations.clear();
  }
}
