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

package com.tenio.core.command.client;

import com.tenio.core.bootstrap.annotation.ClientCommand;
import com.tenio.common.data.DataCollection;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.entity.Player;
import com.tenio.core.exception.AddedDuplicatedClientCommandException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The commands' management class.
 *
 * @since 0.5.0
 */
public final class ClientCommandManager extends SystemLogger {

  private final Map<Short, AbstractClientCommandHandler<Player, DataCollection>> commands = new HashMap<>();
  private final Map<Short, CommandMetadata> metadata = new HashMap<>();
  private volatile CommandLimits limits;

  private record CommandMetadata(Class<?> handlerClass, String name, String description) {
  }

  /**
   * Registers a command handler.
   *
   * @param code    The command code
   * @param command The command handler
   */
  public void registerCommand(Short code, AbstractClientCommandHandler<Player, DataCollection> command) {
    // checks availability
    if (commands.containsKey(code)) {
      throw new AddedDuplicatedClientCommandException(code, commands.get(code));
    }

    // gets command data
    var binding = command.getClass().getAnnotation(ClientCommand.class);
    if (binding != null) {
      int rate = binding.requestsPerSecond();
      int burst = binding.burst();
      if (rate < 0 || burst < 0) {
        throw new IllegalArgumentException("Command rate and burst cannot be negative");
      }
      if (rate > 0) {
        getLimits().configure(code, rate, burst == 0 ? rate : burst);
      }
    }
    var display = commandMetadata(command, binding);
    commands.put(code, command);
    metadata.put(code, display);
  }

  private static CommandMetadata commandMetadata(AbstractClientCommandHandler<Player, DataCollection> handler, ClientCommand binding) {
    var type = handler.getClass();
    String name = binding == null ? "" : binding.name().strip();
    if (name.isEmpty()) {
      name = type.getSimpleName();
      if (name.isEmpty()) {
        name = type.getName();
      }
    }
    String description = binding == null ? "" : binding.description().strip();
    return new CommandMetadata(type, name.replaceAll("\\s+", " "), description.replaceAll("\\s+", " "));
  }

  /**
   * Formats a command code and its display metadata for diagnostics.
   *
   * @param code the command code
   * @param includeDescription whether to include the optional description
   * @return the code and available display metadata
   */
  public String describeCommand(Short code, boolean includeDescription) {
    var handler = commands.get(code);
    if (handler == null) {
      return String.valueOf(code);
    }

    var display = metadata.get(code);
    if (display == null || display.handlerClass() != handler.getClass()) {
      // The live handler map can be updated directly by application code.
      display = commandMetadata(handler, handler.getClass().getAnnotation(ClientCommand.class));
    }
    String label = code + " | " + display.name();
    return includeDescription && !display.description().isEmpty()
        ? label + " | " + display.description() : label;
  }

  /**
   * Returns a list of all registered commands.
   *
   * @return all command handlers as a list
   */
  public List<AbstractClientCommandHandler<Player, DataCollection>> getHandlersAsList() {
    return new LinkedList<>(commands.values());
  }

  /**
   * Retrieves a map of all registered commands.
   *
   * @return a {@link Map} of all registered commands
   */
  public Map<Short, AbstractClientCommandHandler<Player, DataCollection>> getHandlers() {
    return commands;
  }

  /**
   * Returns a handler by its code
   *
   * @param code The command code
   * @return the command handler
   */
  public AbstractClientCommandHandler<Player, DataCollection> getHandler(Short code) {
    return commands.get(code);
  }

  /**
   * Returns an immutable snapshot of accepted and rejected requests by command code.
   *
   * @return counts for configured commands, or an empty map if no limits are configured
   */
  public Map<Short, CommandLimits.Counts> limitCounts() {
    var active = limits;
    return active == null ? Map.of() : active.counts();
  }

  /**
   * Checks whether any client command has a configured rate limit.
   *
   * @return {@code true} if at least one rate limit is configured
   */
  public boolean hasLimits() {
    var active = limits;
    return active != null && active.enabled();
  }

  /**
   * Removes the session-scoped rate-limit state when a session closes.
   *
   * @param sessionId the ID used by {@link CommandLimits#allowSession(short, long)}
   */
  public void releaseSession(long sessionId) {
    var active = limits;
    if (active != null && active.enabled()) {
      active.releaseSession(sessionId);
    }
  }

  /**
   * Returns the rate limiter, creating it on first access.
   * Configure application-defined rules during startup through
   * {@link CommandLimits#configure(short, int, int)}.
   *
   * @return the rate limiter owned by this command manager
   */
  public synchronized CommandLimits getLimits() {
    if (limits == null) {
      limits = new CommandLimits();
    }
    return limits;
  }

  /**
   * Invokes a command handler with given arguments.
   *
   * @param code    The messaged used to invoke the command
   * @param player  The receiver which gets command from its client
   * @param message The message as command
   */
  public void invoke(Short code, Player player, DataCollection message) {
    // gets command handler
    var handler = getHandler(code);

    // checks if the handler is null
    if (handler == null) {
      return;
    }

    // invokes execute method for handler
    var activeLimits = limits;
    if (activeLimits == null || !activeLimits.enabled()
        || activeLimits.allow(code, player == null ? null : player.getIdentity())) {
      handler.execute(player, message);
    }
  }

  /**
   * Clears all the list of commands.
   */
  public void clear() {
    commands.clear();
    metadata.clear();
    if (limits != null) {
      limits.clear();
      limits = null;
    }
  }
}
