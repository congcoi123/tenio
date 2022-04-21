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

package com.tenio.core.extension;

import com.tenio.common.data.ZeroArray;
import com.tenio.common.data.ZeroMap;
import com.tenio.common.data.utility.ZeroUtility;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.core.api.ServerApi;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.entity.protocol.implement.ResponseImpl;
import com.tenio.core.server.Server;
import com.tenio.core.server.ServerImpl;

/**
 * This class provides all the necessary APIs for a logic game handling.
 */
public abstract class AbstractExtension extends AbstractLogger {

  private final Server server = ServerImpl.getInstance();

  /**
   * Retrieves the server supported API responsible object.
   *
   * @return an instance of {@link ServerApi}.
   */
  public final ServerApi api() {
    return server.getApi();
  }

  /**
   * Retrieves a response object which is using to send responses to clients side.
   *
   * @return an instance of {@link Response}
   */
  public final Response response() {
    return ResponseImpl.newInstance();
  }

  /**
   * Retrieves a zero array instance.
   *
   * @return an instance of {@link ZeroArray}
   */
  public final ZeroArray array() {
    return ZeroUtility.newZeroArray();
  }

  /**
   * Retrieves an zero map instance.
   *
   * @return an instance of {@link ZeroMap}
   */
  public final ZeroMap object() {
    return ZeroUtility.newZeroMap();
  }

  /**
   * Retrieves an initialized room setting.
   *
   * @return an instance of {@link InitialRoomSetting.Builder}
   */
  public InitialRoomSetting.Builder roomSetting() {
    return InitialRoomSetting.Builder.newInstance();
  }
}
