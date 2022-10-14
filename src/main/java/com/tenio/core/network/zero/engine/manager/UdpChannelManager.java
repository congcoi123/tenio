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

package com.tenio.core.network.zero.engine.manager;

import com.tenio.core.exception.EmptyUdpChannelsException;
import com.tenio.core.manager.Manager;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.GuardedBy;

/**
 * This class takes responsibility to provide an available UDP channel port when required.
 */
public class UdpChannelManager implements Manager {

  @GuardedBy("this")
  private final List<Integer> udpPorts;
  @GuardedBy("this")
  private int currentIndex;

  private UdpChannelManager() {
    udpPorts = new ArrayList<>();
    currentIndex = -1;
  }

  public static UdpChannelManager newInstance() {
    return new UdpChannelManager();
  }

  /**
   * Appends a new port into the list.
   *
   * @param udpPort a new {@code integer} value of available UDP channel
   */
  public synchronized void appendUdpPort(int udpPort) {
    udpPorts.add(udpPort);
  }

  /**
   * Retrieves the current available UDP port, applies the "round-robin" algorithm.
   *
   * @return an {@code integer} value of UDP port
   */
  public synchronized int getCurrentAvailableUdpPort() {
    int size = udpPorts.size();
    if (size == 0) {
      throw new EmptyUdpChannelsException();
    }
    currentIndex++;
    if (currentIndex >= size) {
      currentIndex = 0;
    }
    return udpPorts.get(currentIndex);
  }
}
