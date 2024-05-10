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

package com.tenio.core.network.configuration;

import kcp.ChannelConfig;

/**
 * KCP Configuration <a href="https://github.com/skywind3000/kcp/blob/master/README.en.md">Manual</a>.
 */
public final class KcpConfiguration {

  public static final int DEFAULT_TIME_OUT_IN_MILLISECONDS = 3_600_000; // 1 hour

  public static ChannelConfig inTurboMode() {
    ChannelConfig channelConfig = new ChannelConfig();
    channelConfig.setTimeoutMillis(KcpConfiguration.DEFAULT_TIME_OUT_IN_MILLISECONDS);
    // Turbo Mode
    channelConfig.nodelay(true, 10, 2, true);
    channelConfig.setSndwnd(1024);
    channelConfig.setRcvwnd(1024);
    channelConfig.setMtu(1400);
    channelConfig.setAckNoDelay(true);
    channelConfig.setUseConvChannel(true);
    channelConfig.setCrc32Check(true);
    return channelConfig;
  }

  public static ChannelConfig inNormalMode() {
    ChannelConfig channelConfig = new ChannelConfig();
    channelConfig.setTimeoutMillis(KcpConfiguration.DEFAULT_TIME_OUT_IN_MILLISECONDS);
    // Normal Mode
    channelConfig.nodelay(false, 40, 0, false);
    channelConfig.setSndwnd(1024);
    channelConfig.setRcvwnd(1024);
    channelConfig.setMtu(1400);
    channelConfig.setAckNoDelay(true);
    channelConfig.setUseConvChannel(true);
    channelConfig.setCrc32Check(true);
    return channelConfig;
  }

  private KcpConfiguration() {
    throw new UnsupportedOperationException();
  }
}
