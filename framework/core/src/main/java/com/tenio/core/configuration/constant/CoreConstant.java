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

package com.tenio.core.configuration.constant;

/**
 * Defines core constants used throughout the server application.
 * This class provides centralized access to commonly used constant values,
 * including character encodings, content types, default package names,
 * and configuration settings.
 *
 * <p>Key features:
 * <ul>
 *   <li>Character encoding constants</li>
 *   <li>Content type definitions</li>
 *   <li>Default package paths</li>
 *   <li>Configuration defaults</li>
 *   <li>Network protocol constants</li>
 * </ul>
 *
 * <p>Note: This class contains only static final fields and should not be instantiated.
 * All constants are immutable and thread-safe.
 *
 * @since 0.3.0
 */
public final class CoreConstant {

  /**
   * UTF-8 character encoding constant.
   * Used for string encoding and decoding operations.
   */
  public static final String UTF_8 = "UTF-8";

  /**
   * JSON content type constant.
   * Used for HTTP responses and content negotiation.
   */
  public static final String CONTENT_TYPE_JSON = "application/json";

  /**
   * HTML content type constant.
   * Used for HTTP responses serving HTML content.
   */
  public static final String CONTENT_TYPE_TEXT = "text/html";

  /**
   * Default configuration file name.
   * Used when no specific configuration file is provided.
   */
  public static final String DEFAULT_CONFIGURATION_FILE = "configuration.xml";

  /**
   * Default package path for bootstrap components.
   * Used for component scanning and initialization.
   */
  public static final String DEFAULT_BOOTSTRAP_PACKAGE = "com.tenio.core.bootstrap";

  /**
   * Default package path for event handling components.
   * Used for event subscriber scanning and registration.
   */
  public static final String DEFAULT_EVENT_PACKAGE = "com.tenio.core.event";

  /**
   * Default package path for command handling components.
   * Used for command handler scanning and registration.
   */
  public static final String DEFAULT_COMMAND_PACKAGE = "com.tenio.core.command";

  /**
   * Default package path for REST controller components.
   * Used for REST endpoint scanning and registration.
   */
  public static final String DEFAULT_REST_CONTROLLER_PACKAGE = "com.tenio.core.network.jetty" +
      ".controller";

  /**
   * Default key for UDP convey ID in packet data.
   * Used for UDP packet identification and routing.
   */
  public static final String DEFAULT_KEY_UDP_CONVEY_ID = "u";

  /**
   * Default key for UDP message data in packet data.
   * Used for UDP packet payload identification.
   */
  public static final String DEFAULT_KEY_UDP_MESSAGE_DATA = "d";

  /**
   * Null port value constant.
   * Used to indicate an unassigned or invalid port number.
   */
  public static final int NULL_PORT_VALUE = -1;

  /**
   * Private constructor to prevent instantiation.
   * This class should not be instantiated as it contains only constants.
   */
  private CoreConstant() {
    throw new UnsupportedOperationException("This class should not be instantiated");
  }
}
