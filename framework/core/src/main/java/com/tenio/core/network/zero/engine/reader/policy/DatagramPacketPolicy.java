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

package com.tenio.core.network.zero.engine.reader.policy;

import com.tenio.common.data.DataCollection;
import com.tenio.core.handler.event.EventAccessDatagramChannelRequestValidationResult;
import com.tenio.core.network.zero.engine.reader.DatagramReaderHandler;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The datagram packet policy. When the system retrieves session by its datagram
 * channel, hence we are using only one datagram channel for all sessions, it uses incoming
 * request {@code convey ID} to distinguish them. This policy help extract info from packets sent
 * from clients.
 *
 * @see EventAccessDatagramChannelRequestValidationResult
 * @see DatagramReaderHandler
 * @since 0.6.7
 */
public interface DatagramPacketPolicy {

  /**
   * Extract data from the incoming packets of clients.
   *
   * @param dataCollection the incoming {@link DataCollection}
   * @return a {@link Pair} object that has the left value is {@code UDP convey ID}, and the
   * right value is the actual {@link DataCollection} content
   */
  Pair<Integer, DataCollection> applyPolicy(DataCollection dataCollection);
}
