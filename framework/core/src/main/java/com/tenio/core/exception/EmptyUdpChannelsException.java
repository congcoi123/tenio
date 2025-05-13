/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.core.exception;

import com.tenio.core.configuration.define.CoreConfigurationType;
import java.io.Serial;

/**
 * When an available Udp channel port is requested, but the list is empty. It might be caused by
 * the size of {@link CoreConfigurationType#NETWORK_UDP} equals to {@code 0}, or there was some
 * exception occurred while establishing Udp channels.
 *
 * @since 0.3.0
 */
public final class EmptyUdpChannelsException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 6979513728417343122L;

  /**
   * Initialization.
   */
  public EmptyUdpChannelsException() {
    super("The list is empty, please check in configuration.xml file if value of udp-channel is " +
        "greater than 0, or make sure there is no exception while establishing udp channels");
  }
}
