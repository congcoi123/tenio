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

package com.tenio.core.command.client;

import com.tenio.common.data.DataCollection;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.entity.Player;
import com.tenio.core.exception.AddedDuplicatedClientCommandException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The commands' management class.
 *
 * @since 0.5.0
 */
@Component
public final class ClientCommandManager extends SystemLogger {

  private final Map<Short, AbstractClientCommandHandler<Player>> commands = new HashMap<>();

  /**
   * Registers a command handler.
   *
   * @param code    The command code
   * @param command The command handler
   */
  public synchronized void registerCommand(Short code,
                                           AbstractClientCommandHandler<Player> command) {
    debug("CLIENT_COMMAND", "Registered command > ", code);

    // checks availability
    if (commands.containsKey(code)) {
      throw new AddedDuplicatedClientCommandException(code, commands.get(code));
    }

    // gets command data
    commands.put(code, command);
  }

  /**
   * Removes a registered command handler.
   *
   * @param code The command code
   */
  public synchronized void unregisterCommand(Short code) {
    debug("CLIENT_COMMAND", "Unregistered command > ", code);

    commands.remove(code);
  }

  /**
   * Returns a list of all registered commands.
   *
   * @return all command handlers as a list
   */
  public synchronized List<AbstractClientCommandHandler<Player>> getHandlersAsList() {
    return new LinkedList<>(commands.values());
  }

  /**
   * Retrieves a map of all registered commands.
   *
   * @return a {@link Map} of all registered commands
   */
  public synchronized Map<Short, AbstractClientCommandHandler<Player>> getHandlers() {
    return commands;
  }

  /**
   * Returns a handler by its code
   *
   * @param code The command code
   * @return the command handler
   */
  public synchronized AbstractClientCommandHandler<Player> getHandler(Short code) {
    return commands.get(code);
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
    if (Objects.isNull(handler)) {
      return;
    }

    // invokes execute method for handler
    Runnable runnable = () -> handler.execute(player, message);
    runnable.run();
  }

  /**
   * Clears all the list of commands.
   */
  public synchronized void clear() {
    commands.clear();
  }
}
