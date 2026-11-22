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
import java.util.Arrays;
import java.util.Locale;
import java.util.List;

/**
 * Show all supporting commands.
 *
 * @since 0.4.0
 */
@SystemCommand(label = "help", profile = CoreConstant.MONITOR_PROFILE,
    usage = {"[name]", "all"},
    description = "Shows all supporting commands")
public final class HelpCommand extends AbstractSystemCommandHandler {

  private void showDetails(AbstractSystemCommandHandler command) {
    var details = new StringBuilder(command.getLabel()).append(" - ").append(command.getDescription());
    for (String usage : command.getUsage()) {
      if (!usage.isBlank()) {
        details.append("\n\t").append(usage);
      }
    }
    CommandUtility.INSTANCE.showConsoleMessage(details.toString());
  }

  @Override
  public void execute(List<String> arguments) {
    var manager = getCommandManager();
    boolean showAll = arguments.size() == 1 && arguments.getFirst().equalsIgnoreCase("all");
    if (arguments.isEmpty() || showAll) {
      int total = manager.getHandlers().size();
      int shown = showAll ? total : Math.min(total, 50);
      manager.getHandlers().keySet().stream().sorted().limit(shown).map(manager::getHandler)
          .forEach(this::showDetails);
      if (shown < total) {
        CommandUtility.INSTANCE.showConsoleMessage(
            "Showing " + shown + " of " + total + " system commands. Use help all for the full table.");
      }
      return;
    }
    arguments.stream().flatMap(argument -> Arrays.stream(argument.split(",")))
        .map(label -> label.strip().toLowerCase(Locale.ROOT))
        .filter(label -> !label.isEmpty()).distinct().forEach(label -> {
          var command = manager.getHandler(label);
          if (command == null) {
            CommandUtility.INSTANCE.showConsoleMessage("Unknown system command: " + label);
          } else {
            showDetails(command);
          }
        });
  }
}
