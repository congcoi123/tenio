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

package com.tenio.core.configuration.kcp;

/**
 * The profile mechanism allows configuring KCP behaviors by groups.
 * @since 0.3.0
 */
public enum KcpProfile {

  /**
   * The normal mode, like TCP.
   */
  NORMAL_MODE {
  },

  /**
   * The highly efficient transport.
   */
  BOOSTER_MODE {
    @Override
    public int getNoDelay() {
      return 1;
    }

    @Override
    public int getUpdateInterval() {
      return 10;
    }

    @Override
    public int getFastResend() {
      return 2;
    }

    @Override
    public int getCongestionControl() {
      return 1;
    }
  };

  /**
   * Whether enable {@code nodelay} mode.
   *
   * @return {@code 0} if the mode is disabled, otherwise returns {@code 1}
   */
  public int getNoDelay() {
    return 0;
  }

  /**
   * The internal interval in milliseconds.
   *
   * @return the internal interval in milliseconds
   */
  public int getUpdateInterval() {
    return 40;
  }

  /**
   * Whether enable fast retransmit mode.
   *
   * @return {@code 0} when the mode is disabled, returning {@code 2} means it retransmits when
   * missed in 2 ACK
   */
  public int getFastResend() {
    return 0;
  }

  /**
   * Whether disable the flow control.
   *
   * @return {@code 0} when enabled, otherwise returns {@code 1}
   */
  public int getCongestionControl() {
    return 0;
  }

  /**
   * Setups the max sending window size in packets, it is similar to TCP {@code SO_SNDBUF}, but
   * the TCP one is in bytes, while the Window Size is in packets.
   *
   * @return the max sending window size in packets
   */
  public int getSendWindowSize() {
    return 32;
  }

  /**
   * Setups the max receiving window size in packets, it is similar to TCP {@code SO_RECVBUF}, but
   * the TCP one is in bytes, while the Window Size is in packets.
   *
   * @return the max receiving window size in packets
   */
  public int getReceiveWindowSize() {
    return 32;
  }

  /**
   * The MTU (Maximum Transmission Unit).
   *
   * @return the Maximum Transmission Unit value in bytes
   */
  public int getMtu() {
    return 1400;
  }
}
