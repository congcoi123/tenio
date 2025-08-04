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
import com.tenio.common.data.msgpack.element.MsgPackMap;
import com.tenio.common.data.zero.ZeroArray;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.network.entity.session.Session;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The default implementation of DatagramPacketPolicy.
 *
 * @see DatagramPacketPolicy
 * @since 0.6.7
 */
public final class DefaultDatagramPacketPolicy implements DatagramPacketPolicy {

  @Override
  public Pair<Integer, DataCollection> applyPolicy(DataCollection dataCollection) {
    int udpConvey = Session.EMPTY_DATAGRAM_CONVEY_ID;
    DataCollection content = null;
    if (dataCollection instanceof ZeroMap zeroMap) {
      if (zeroMap.containsKey(CoreConstant.DEFAULT_KEY_UDP_CONVEY_ID)) {
        udpConvey = zeroMap.getInteger(CoreConstant.DEFAULT_KEY_UDP_CONVEY_ID);
      }
      if (zeroMap.containsKey(CoreConstant.DEFAULT_KEY_UDP_MESSAGE_DATA)) {
        content = zeroMap.getDataCollection(CoreConstant.DEFAULT_KEY_UDP_MESSAGE_DATA);
      }
    } else if (dataCollection instanceof ZeroArray zeroArray) {
      int length = zeroArray.size();
      if (length > 0) {
        udpConvey = zeroArray.getInteger(length - 1);
        zeroArray.removeElementAt(length - 1);
        content = zeroArray;
      }
    } else if (dataCollection instanceof MsgPackMap msgPackMap) {
      if (msgPackMap.contains(CoreConstant.DEFAULT_KEY_UDP_CONVEY_ID)) {
        udpConvey = msgPackMap.getInteger(CoreConstant.DEFAULT_KEY_UDP_CONVEY_ID);
      }
      if (msgPackMap.containsKey(CoreConstant.DEFAULT_KEY_UDP_MESSAGE_DATA)) {
        content = msgPackMap.getMsgPackMap(CoreConstant.DEFAULT_KEY_UDP_MESSAGE_DATA);
      }
    }
    return Pair.of(udpConvey, content);
  }
}
