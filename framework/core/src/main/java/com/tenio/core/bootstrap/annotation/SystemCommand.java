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

package com.tenio.core.bootstrap.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.tenio.core.command.system.AbstractSystemCommandHandler;
import com.tenio.core.command.system.SystemCommandManager;
import com.tenio.core.exception.AddedDuplicatedCommandException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to mark classes that implement system command handlers. Classes annotated with
 * this annotation will be automatically registered with the system command manager and can handle
 * system-level commands.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Automatic command registration
 *   <li>Integration with system command manager
 *   <li>Command label specification
 *   <li>Runtime retention for reflection-based processing
 *   <li>Type-level annotation
 * </ul>
 *
 * <p>Usage example:
 *
 * <pre>
 * {@code @SystemCommand("shutdown")
 * public class ShutdownCommandHandler extends AbstractSystemCommandHandler {
 *     @Override
 *     public void execute(String[] args) {
 *         // Handle shutdown command
 *         System.exit(0);
 *     }
 * }
 * }
 * </pre>
 *
 * <p>Note: Classes annotated with this annotation:
 *
 * <ul>
 *   <li>Must extend {@link AbstractSystemCommandHandler}
 *   <li>Will be automatically registered with {@link SystemCommandManager}
 *   <li>Should provide a unique command label
 *   <li>Must implement the command execution logic
 * </ul>
 *
 * @see AbstractSystemCommandHandler
 * @see SystemCommandManager
 * @see AddedDuplicatedCommandException
 * @since 0.4.0
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface SystemCommand {

  /**
   * Startup profiles allowing this annotated element to participate. An empty array or blank
   * restriction is unrestricted. Values are trimmed, case-sensitive OR alternatives; each entry may
   * also contain comma-separated names. Java supports a single string shorthand or an array, but
   * annotation defaults cannot be null. Selection precedes construction, factory invocation,
   * registration and injection and does not replace bean qualifiers.
   *
   * @return accepted startup names, or an empty array for every profile
   */
  String[] profile() default {};

  /**
   * The label used to identify and invoke the system command. This label must be unique across all
   * registered system commands.
   *
   * @return the command label
   */
  String label() default "";

  /**
   * Retrieves a list of usage for the command.
   *
   * @return an array of {@link String} instructions that the command is supporting
   */
  String[] usage() default {""};

  /**
   * Retrieves the command's description.
   *
   * @return the {@link String} command's description
   */
  String description() default "";

  /**
   * Whether the command will be run in background.
   *
   * @return {@code true} when the command will be run in a separated thread, otherwise, returns {@code false}
   */
  boolean isBackgroundRunning() default false;
}
