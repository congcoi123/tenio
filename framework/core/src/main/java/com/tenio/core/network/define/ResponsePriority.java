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

package com.tenio.core.network.define;

import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.packet.policy.DefaultPacketQueuePolicy;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;

/**
 * Definition the priority for a response from server.
 *
 * @see PacketQueue
 * @see PacketQueuePolicy
 */
public enum ResponsePriority {

  /**
   * The response may not be responded bases on the rule implementations logic.
   *
   * @see DefaultPacketQueuePolicy
   */
  NON_GUARANTEED(1),
  /**
   * The response is processed bases on the rule implementations logic.
   *
   * @see DefaultPacketQueuePolicy
   */
  NORMAL(2),
  /**
   * The response should be guaranteed to be responded bases on the rule implementations logic.
   *
   * @see DefaultPacketQueuePolicy
   */
  GUARANTEED(3),
  /**
   * The response should be guaranteed to be responded in the highest priority bases on the
   * rule implementations logic.
   *
   * @see DefaultPacketQueuePolicy
   */
  GUARANTEED_QUICKEST(4);

  private final int value;

  ResponsePriority(final int value) {
    this.value = value;
  }

  /**
   * Retrieves the response's priority in numeric value.
   *
   * @return the response's priority in {@code integer} numeric value
   */
  public final int getValue() {
    return value;
  }

  @Override
  public final String toString() {
    return name();
  }
}
