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

import com.tenio.core.command.client.AbstractClientCommandHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to mark classes that implement client command handlers. Classes annotated with
 * this annotation will be automatically registered with the client command manager and can handle
 * client-side commands.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Automatic command registration
 *   <li>Integration with client command manager
 *   <li>Command label specification
 *   <li>Runtime retention for reflection-based processing
 *   <li>Type-level annotation
 * </ul>
 *
 * <p>Usage example:
 *
 * <pre>
 * {@code @ClientCommand(Constants.LOGIN)
 * public class LoginCommandHandler extends AbstractClientCommandHandler {
 *     @Override
 *     public void execute(Player player, String[] args) {
 *         // Handle login command
 *         String username = args[0];
 *         String password = args[1];
 *         // Process login...
 *     }
 * }
 * }
 * </pre>
 *
 * <p>Note: Classes annotated with this annotation:
 *
 * <ul>
 *   <li>Must extend {@link AbstractClientCommandHandler}
 *   <li>Will be automatically registered with the client command manager
 *   <li>Should provide a unique command value
 *   <li>Must implement the command execution logic
 *   <li>Should handle player-specific operations
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
     * The value used to identify and invoke the client command. This value must be unique across all
     * registered client commands.
     *
     * @return the command value
     */
    short value();

    /**
     * Optional display name. The handler class name is used when this is blank.
     */
    String name() default "";

    /**
     * Optional description shown in the startup command list.
     */
    String description() default "";

    /**
     * Sustained requests per second per player. Zero disables limiting.
     */
    int requestsPerSecond() default 0;

    /**
     * Token bucket capacity. Zero uses the sustained rate when limiting is enabled.
     */
    int burst() default 0;
}
