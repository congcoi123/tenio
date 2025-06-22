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

package com.tenio.core.command.system.implement;

import com.tenio.core.bootstrap.annotation.SystemCommand;
import com.tenio.core.command.system.AbstractSystemCommandHandler;
import com.tenio.core.utility.CommandUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Show all supporting commands.
 *
 * @since 0.4.0
 */
@SystemCommand(label = "help", usage = {
    "[<command>,<command>,<command>]"
}, description = "Shows all supporting commands")
public final class HelpCommand extends AbstractSystemCommandHandler {

  private String createCommandDetails(AbstractSystemCommandHandler command) {
    return command.getLabel() +
        " - " +
        (command.getDescription().isBlank() ? "Non Description" : command.getDescription()) +
        "\n\t" +
        String.join("\n\t", command.getUsage());
  }

  @Override
  public void execute(List<String> arguments) {
    var commandManager = getCommandManager();
    var commands = new ArrayList<>();
    if (arguments.isEmpty()) {
      commandManager.getHandlers()
          .forEach((key, command) -> commands.add(createCommandDetails(command)));
    } else {
      var labels = arguments.remove(0).toLowerCase().trim().split(",");
      for (var label : labels) {
        var command = commandManager.getHandler(label);
        if (Objects.isNull(command)) {
          CommandUtility.INSTANCE.showConsoleMessage(
              "SystemCommand {" + label + "} does not exist.");
          return;
        } else {
          commands.add(createCommandDetails(command));
        }
      }
    }
    commands.forEach(System.out::println);
  }
}
