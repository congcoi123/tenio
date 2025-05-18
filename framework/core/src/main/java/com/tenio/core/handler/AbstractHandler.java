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

package com.tenio.core.handler;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.data.DataType;
import com.tenio.common.data.msgpack.element.MsgPackMap;
import com.tenio.common.data.zero.ZeroArray;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.data.zero.utility.ZeroUtility;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.api.ServerApi;
import com.tenio.core.command.client.ClientCommandManager;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.exception.UnsupportedDataTypeInUseException;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.entity.protocol.implement.ResponseImpl;
import com.tenio.core.server.Server;
import com.tenio.core.server.ServerImpl;

/**
 * Abstract base class that provides core functionality for game logic handling.
 * This class serves as a foundation for implementing game-specific handlers,
 * offering access to server configuration, API, and data manipulation utilities.
 *
 * <p>Key features:
 * <ul>
 *   <li>Server configuration and API access</li>
 *   <li>Client command management</li>
 *   <li>Response creation and data manipulation</li>
 *   <li>Data type-specific array and map creation</li>
 *   <li>Logging and error handling</li>
 *   <li>Event management integration</li>
 * </ul>
 *
 * <p>Thread safety: This class is not thread-safe by default. Implementations
 * should ensure thread safety if the handler is accessed from multiple threads.
 * The server configuration and API access methods are thread-safe.
 *
 * <p>Note: This class extends {@link SystemLogger} to provide logging capabilities
 * and maintains a reference to the server instance for accessing core functionality.
 * Handlers should be properly initialized before use.
 *
 * @see Server
 * @see ServerApi
 * @see Configuration
 * @see Response
 * @see DataType
 * @see SystemLogger
 * @since 0.3.0
 */
public abstract class AbstractHandler extends SystemLogger {

  private final Server server = ServerImpl.getInstance();

  /**
   * Retrieves the server configuration.
   * This method provides access to the server's configuration settings.
   *
   * @return the server's {@link Configuration} instance
   */
  protected final Configuration configuration() {
    return server.getConfiguration();
  }

  /**
   * Retrieves the server supported API responsible object.
   *
   * @return an instance of {@link ServerApi}.
   */
  protected final ServerApi api() {
    return server.getApi();
  }

  /**
   * Retrieves a management object of self-defined user commands.
   *
   * @return an instance of {@link ClientCommandManager}
   * @since 0.5.0
   */
  protected final ClientCommandManager clientCommand() {
    return server.getClientCommandManager();
  }

  /**
   * Retrieves a response object which is using to send responses to clients side.
   *
   * @return an instance of {@link Response}
   */
  protected final Response response() {
    return ResponseImpl.newInstance();
  }

  /**
   * Creates a new array based on the server's data type.
   * For ZERO data type, returns a {@link ZeroArray}.
   *
   * @return a new array instance appropriate for the server's data type
   * @throws UnsupportedDataTypeInUseException if the server's data type is not ZERO
   */
  protected final ZeroArray array() {
    if (server.getDataType() != DataType.ZERO) {
      throw new UnsupportedDataTypeInUseException(server.getDataType());
    }
    return ZeroUtility.newZeroArray();
  }

  /**
   * Creates a new map based on the server's data type.
   * For ZERO data type, returns a {@link ZeroMap}.
   *
   * @return a new map instance appropriate for the server's data type
   * @throws UnsupportedDataTypeInUseException if the server's data type is not ZERO
   */
  protected final ZeroMap map() {
    if (server.getDataType() != DataType.ZERO) {
      throw new UnsupportedDataTypeInUseException(server.getDataType());
    }
    return ZeroUtility.newZeroMap();
  }

  /**
   * Retrieves a msgpack map instance when the {@link DataType} in use is {@link DataType#MSG_PACK}.
   *
   * @return an instance of {@link MsgPackMap}
   */
  protected final MsgPackMap msgmap() {
    if (server.getDataType() != DataType.MSG_PACK) {
      throw new UnsupportedDataTypeInUseException(server.getDataType());
    }
    return MsgPackMap.newInstance();
  }

  /**
   * Retrieves an initialized room setting.
   *
   * @return an instance of {@link InitialRoomSetting.Builder}
   */
  protected InitialRoomSetting.Builder roomSetting() {
    return InitialRoomSetting.Builder.newInstance();
  }
}
