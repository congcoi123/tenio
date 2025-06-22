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

package com.tenio.core.command.system;

import com.tenio.core.bootstrap.annotation.SystemCommand;
import com.tenio.core.handler.AbstractHandler;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract base class for implementing system-level command handlers.
 * This class provides a foundation for handling system commands with support
 * for command metadata, background execution, and command management.
 *
 * <p>Key features:
 * <ul>
 *   <li>Command metadata management (label, usage, description)</li>
 *   <li>Background execution support</li>
 *   <li>Integration with system command manager</li>
 *   <li>Command execution lifecycle</li>
 *   <li>Error handling and logging</li>
 *   <li>Command argument processing</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * {@code @SystemCommand(
 *     label = "SHUTDOWN",
 *     description = "Shuts down the server gracefully",
 *     usage = {"SHUTDOWN [delay]", "SHUTDOWN -f"},
 *     isBackgroundRunning = false
 * )
 * public class ShutdownCommandHandler extends AbstractSystemCommandHandler {
 *     @Override
 *     public void execute(List<String> args) {
 *         try {
 *             // Parse command arguments
 *             boolean force = args.contains("-f");
 *             int delay = args.stream()
 *                 .filter(arg -> !arg.startsWith("-"))
 *                 .mapToInt(Integer::parseInt)
 *                 .findFirst()
 *                 .orElse(0);
 *
 *             // Implement shutdown logic
 *             getCommandManager().broadcast("Server is shutting down...");
 *             if (!force) {
 *                 Thread.sleep(delay * 1000);
 *             }
 *             System.exit(0);
 *         } catch (Exception e) {
 *             error("Failed to shutdown server: " + e.getMessage());
 *         }
 *     }
 * }
 * }
 * </pre>
 *
 * <p>Thread safety: This class is not thread-safe by default. Implementations
 * should ensure thread safety if the command handler is accessed from multiple
 * threads. The command manager reference is not thread-safe and should be
 * accessed with proper synchronization.
 *
 * <p>Note: Command handlers should be annotated with {@link SystemCommand}
 * to be properly registered with the system command manager. The command
 * manager must be set before the handler can be used.
 *
 * @see SystemCommand
 * @see SystemCommandManager
 * @see AbstractHandler
 * @since 0.4.0
 */
public abstract class AbstractSystemCommandHandler extends AbstractHandler {

  private SystemCommandManager systemCommandManager;

  /**
   * Retrieves the command label.
   * The label is used to identify and invoke the command.
   * This value is obtained from the {@link SystemCommand} annotation.
   *
   * @return a {@link String} value of the command label
   * @throws IllegalStateException if the class is not properly annotated
   */
  public String getLabel() {
    return getClass().getAnnotation(SystemCommand.class).label();
  }

  /**
   * Retrieves the command usage instructions.
   * These instructions provide guidance on how to use the command.
   * This value is obtained from the {@link SystemCommand} annotation.
   *
   * @return an array of instructions in {@link String} values
   * @throws IllegalStateException if the class is not properly annotated
   */
  public String[] getUsage() {
    return getClass().getAnnotation(SystemCommand.class).usage();
  }

  /**
   * Retrieves the command description.
   * The description provides a brief explanation of the command's purpose.
   * This value is obtained from the {@link SystemCommand} annotation.
   *
   * @return a {@link String} value of the command description
   * @throws IllegalStateException if the class is not properly annotated
   */
  public String getDescription() {
    return getClass().getAnnotation(SystemCommand.class).description();
  }

  /**
   * Checks whether the command should be running in background.
   * Background commands are executed in a separate thread to avoid
   * blocking the main command processing thread.
   * This value is obtained from the {@link SystemCommand} annotation.
   *
   * @return {@code true} if the command should be running in the background,
   * otherwise returns {@code false}
   * @throws IllegalStateException if the class is not properly annotated
   */
  public boolean isRunningBackground() {
    return getClass().getAnnotation(SystemCommand.class).isBackgroundRunning();
  }

  /**
   * Retrieves the system command manager.
   * The command manager provides access to system-wide command functionality.
   *
   * @return an instance of {@link SystemCommandManager}
   * @throws IllegalStateException if the command manager has not been set
   */
  public SystemCommandManager getCommandManager() {
    if (systemCommandManager == null) {
      throw new IllegalStateException("Command manager not initialized");
    }
    return systemCommandManager;
  }

  /**
   * Sets the system command manager.
   * This method is called during command handler initialization.
   * The command manager must be set before the handler can be used.
   *
   * @param systemCommandManager an instance of {@link SystemCommandManager}
   * @throws IllegalArgumentException if the command manager is null
   */
  public void setCommandManager(SystemCommandManager systemCommandManager) {
    if (systemCommandManager == null) {
      throw new IllegalArgumentException("Command manager cannot be null");
    }
    this.systemCommandManager = systemCommandManager;
  }

  /**
   * Executes the command with the provided arguments.
   *
   * @param arguments the command arguments as a list of {@code String} values
   * @throws IllegalArgumentException if the arguments are invalid
   * @throws IllegalStateException    if the command manager is not initialized
   */
  public abstract void execute(List<String> arguments);

  @Override
  public String toString() {
    return "CommandHandler{" +
        "class=" + getClass().getName() +
        ", label=" + getLabel() +
        ", description=" + getDescription() +
        ", usage=" + Arrays.toString(getUsage()) +
        "}";
  }
}
