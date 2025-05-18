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

package com.tenio.core.bootstrap.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.tenio.core.command.client.AbstractClientCommandHandler;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to mark classes that implement client command handlers.
 * Classes annotated with this annotation will be automatically registered with the
 * client command manager and can handle client-side commands.
 *
 * <p>Key features:
 * <ul>
 *   <li>Automatic command registration</li>
 *   <li>Integration with client command manager</li>
 *   <li>Command label specification</li>
 *   <li>Runtime retention for reflection-based processing</li>
 *   <li>Type-level annotation</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * &#64;ClientCommand(Constants.LOGIN)
 * public class LoginCommandHandler extends AbstractClientCommandHandler {
 *     &#64;Override
 *     public void execute(Player player, String[] args) {
 *         // Handle login command
 *         String username = args[0];
 *         String password = args[1];
 *         // Process login...
 *     }
 * }
 * </pre>
 *
 * <p>Note: Classes annotated with this annotation:
 * <ul>
 *   <li>Must extend {@link AbstractClientCommandHandler}</li>
 *   <li>Will be automatically registered with the client command manager</li>
 *   <li>Should provide a unique command value</li>
 *   <li>Must implement the command execution logic</li>
 *   <li>Should handle player-specific operations</li>
 * </ul>
 *
 * @see AbstractClientCommandHandler
 * @since 0.5.0
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface ClientCommand {

  /**
   * The value used to identify and invoke the client command.
   * This value must be unique across all registered client commands.
   *
   * @return the command value
   */
  short value();
}
