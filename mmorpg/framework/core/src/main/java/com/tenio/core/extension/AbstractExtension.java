/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
import com.tenio.common.data.ZeroObject;
import com.tenio.common.data.implement.ZeroArrayImpl;
import com.tenio.common.data.implement.ZeroObjectImpl;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.core.api.ServerApi;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.entity.protocol.implement.ResponseImpl;
import com.tenio.core.server.Server;
import com.tenio.core.server.ServerImpl;

/**
 * This class provides you all the necessary APIs for your own logic game
 * handling.
 */
public abstract class AbstractExtension extends AbstractLogger {

  private final Server server = ServerImpl.getInstance();

  /**
   * Retrieves the api object.
   *
   * @return the api object.
   */
  public final ServerApi api() {
    return server.getApi();
  }

  /**
   * Retrieves the response object.
   *
   * @return the response object
   */
  public final Response response() {
    return ResponseImpl.newInstance();
  }

  /**
   * Retrieves the zero array.
   *
   * @return the zero array
   */
  public final ZeroArray array() {
    return ZeroArrayImpl.newInstance();
  }

  /**
   * Retrieves the zero object.
   *
   * @return the zero object
   */
  public final ZeroObject object() {
    return ZeroObjectImpl.newInstance();
  }

  /**
   * Retrieves the initialized room setting.
   *
   * @return the initialized room setting
   */
  public InitialRoomSetting.Builder roomSetting() {
    return InitialRoomSetting.Builder.newInstance();
  }
}
