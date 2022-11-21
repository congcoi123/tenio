/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.core.command;

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.bootstrap.annotation.Command;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.exception.AddedDuplicatedCommandException;
import com.tenio.core.utility.CommandUtility;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.reflections.Reflections;

/**
 * The commands' management class.
 *
 * @since 0.4.0
 */
@Component
public final class CommandManager extends SystemLogger {

  private final Map<String, AbstractCommandHandler> commands = new TreeMap<>();
  private final Map<String, Command> annotations = new TreeMap<>();

  /**
   * Registers a command handler.
   *
   * @param label   The command label
   * @param command The command handler
   */
  public void registerCommand(String label, AbstractCommandHandler command) {
    debug("COMMAND", "Registered command > " + label);
    label = label.toLowerCase();

    // checks availability
    if (commands.containsKey(label)) {
      throw new AddedDuplicatedCommandException(label, commands.get(label));
    }

    // gets command data
    var annotation = command.getClass().getAnnotation(Command.class);
    annotations.put(label, annotation);
    commands.put(label, command);
  }

  /**
   * Removes a registered command handler.
   *
   * @param label The command label
   */
  public void unregisterCommand(String label) {
    debug("COMMAND", "Unregistered command: " + label);

    annotations.remove(label);
    commands.remove(label);
  }

  public List<Command> getAnnotationsAsList() {
    return new LinkedList<>(annotations.values());
  }

  public Map<String, Command> getAnnotations() {
    return new LinkedHashMap<>(annotations);
  }

  /**
   * Returns a list of all registered commands.
   *
   * @return all command handlers as a list
   */
  public List<AbstractCommandHandler> getHandlersAsList() {
    return new LinkedList<>(commands.values());
  }

  public Map<String, AbstractCommandHandler> getHandlers() {
    return commands;
  }

  /**
   * Returns a handler by its label
   *
   * @param label The command label
   * @return the command handler
   */
  public AbstractCommandHandler getHandler(String label) {
    return commands.get(label);
  }

  /**
   * Invokes a command handler with given arguments.
   *
   * @param rawMessage The messaged used to invoke the command
   */
  public void invoke(String rawMessage) {
    rawMessage = rawMessage.trim();
    if (rawMessage.isBlank()) {
      return;
    }

    // parses message
    var split = rawMessage.split(" ");
    var args = new LinkedList<>(Arrays.asList(split));
    var label = args.remove(0).toLowerCase();

    // gets command handler
    var handler = getHandler(label);

    // checks if the handler is null
    if (Objects.isNull(handler)) {
      return;
    }

    // gets the command's annotation
    var annotation = annotations.get(label);

    // invokes execute method for handler
    Runnable runnable = () -> handler.execute(args);
    if (annotation.isBackgroundRunning()) {
      new Thread(runnable).start();
      CommandUtility.INSTANCE.showConsoleMessage("The process is running in background.");
    } else {
      runnable.run();
    }
  }

  /**
   * Scans for all classes annotated with {@link Command} and registers them.
   *
   * @param entryClass the root class which should be located in the parent package of other
   *                   class' packages
   * @param packages   a list of packages' names. It allows to define the scanning packages by
   *                   their names
   * @throws IllegalArgumentException it is related to the illegal argument exception
   * @throws SecurityException        it is related to the security exception
   */
  public void scanPackages(Class<?> entryClass, String... packages)
      throws IllegalArgumentException, SecurityException {

    // clean maps data first
    commands.clear();
    annotations.clear();

    // start scanning
    var setPackageNames = new HashSet<String>();

    if (Objects.nonNull(entryClass)) {
      setPackageNames.add(entryClass.getPackage().getName());
    }

    if (Objects.nonNull(packages)) {
      setPackageNames.addAll(Arrays.asList(packages));
    }

    // declares a reflection object based on the package of root class
    var reflections = new Reflections();
    for (var packageName : setPackageNames) {
      var reflectionPackage = new Reflections(packageName);
      reflections.merge(reflectionPackage);
    }

    var classes = reflections.getTypesAnnotatedWith(Command.class);
    classes.forEach(annotated -> {
      try {
        var commandData = annotated.getAnnotation(Command.class);
        var object = annotated.getDeclaredConstructor().newInstance();
        if (object instanceof AbstractCommandHandler) {
          var command = (AbstractCommandHandler) object;
          command.setCommandManager(this);
          registerCommand(commandData.label(), command);
        } else {
          error(new IllegalArgumentException("Class " + annotated.getName() + " is not a " +
              "AbstractCommandHandler"));
        }
      } catch (Exception exception) {
        error(exception, "Failed to register command handler for " + annotated.getSimpleName());
      }
    });
  }
}
