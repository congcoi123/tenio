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

package com.tenio.core.handler.event;

import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.controller.AbstractController;
import com.tenio.core.exception.PacketQueueFullException;
import com.tenio.core.exception.PacketQueuePolicyViolationException;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;

/**
 * Fetches the bandwidth information on the server.
 */
@FunctionalInterface
public interface EventFetchedBandwidthInfo {

  /**
   * Fetches the bandwidth information on the server. The information should be frequently
   * updated every interval time.
   *
   * @param readBytes                     {@code long} value, the current total number of
   *                                      read binary data from clients side
   * @param readPackets                   {@code long} value, the current total number of
   *                                      read packets from clients side
   * @param readDroppedPackets            {@code long} value, the current total number of
   *                                      dropped packets sent from clients side which violated
   *                                      request queue policies and could not be processed
   * @param writtenBytes                  {@code long} value, the current total number of
   *                                      sending binary data to clients side
   * @param writtenPackets                {@code long} value, the current total number of
   *                                      sending packets to clients side
   * @param writtenDroppedPacketsByPolicy {@code long} value, the current total number of
   *                                      dropped packets which could not sent to clients side
   *                                      because they violated the packet queue policies
   * @param writtenDroppedPacketsByFull   {@code long} value, the current total number of
   *                                      dropped packets which could not sent to clients side
   *                                      because packet queue is full and could not hold any
   *                                      written packet
   * @see CoreConfigurationType#INTERVAL_TRAFFIC_COUNTER
   * @see AbstractController
   * @see PacketQueue
   * @see PacketQueuePolicy
   * @see PacketQueuePolicyViolationException
   * @see PacketQueueFullException
   */
  void handle(long readBytes, long readPackets, long readDroppedPackets, long writtenBytes,
              long writtenPackets,
              long writtenDroppedPacketsByPolicy, long writtenDroppedPacketsByFull);
}
