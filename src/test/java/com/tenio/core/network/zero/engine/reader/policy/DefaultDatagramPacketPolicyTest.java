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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.msgpack.element.MsgPackMap;
import com.tenio.common.data.zero.ZeroArray;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.data.zero.utility.ZeroUtility;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.network.entity.session.Session;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For DefaultDatagramPacketPolicy")
class DefaultDatagramPacketPolicyTest {

  @Test
  @DisplayName("Test applying policy with ZeroMap")
  void testApplyPolicyWithZeroMap() {
    ZeroMap zeroMap = ZeroUtility.newZeroMap();
    zeroMap.putInteger(CoreConstant.DEFAULT_KEY_UDP_CONVEY_ID, 42);
    ZeroMap messageData = ZeroUtility.newZeroMap();
    messageData.putString("key", "value");
    zeroMap.putZeroMap(CoreConstant.DEFAULT_KEY_UDP_MESSAGE_DATA, messageData);

    DefaultDatagramPacketPolicy policy = new DefaultDatagramPacketPolicy();
    Pair<Integer, DataCollection> result = policy.applyPolicy(zeroMap);

    assertEquals(42, result.getLeft());
    assertEquals(messageData, result.getRight());
  }

  @Test
  @DisplayName("Test applying policy with ZeroArray")
  void testApplyPolicyWithZeroArray() {
    ZeroArray zeroArray = ZeroUtility.newZeroArray();
    zeroArray.addString("data1");
    zeroArray.addInteger(99); // UDP convey ID at the end

    DefaultDatagramPacketPolicy policy = new DefaultDatagramPacketPolicy();
    Pair<Integer, DataCollection> result = policy.applyPolicy(zeroArray);

    assertEquals(99, result.getLeft());
    assertEquals(zeroArray, result.getRight()); // The last element should be removed
    assertEquals(1, result.getRight().size());
    assertEquals("data1", ((ZeroArray) result.getRight()).getString(0));
  }

  @Test
  @DisplayName("Test applying policy with MsgPackMap")
  void testApplyPolicyWithMsgPackMap() {
    MsgPackMap msgPackMap = MsgPackMap.newInstance();
    msgPackMap.putInteger(CoreConstant.DEFAULT_KEY_UDP_CONVEY_ID, 7);
    MsgPackMap messageData = MsgPackMap.newInstance();
    messageData.putString("foo", "bar");
    msgPackMap.putMsgPackMap(CoreConstant.DEFAULT_KEY_UDP_MESSAGE_DATA, messageData);

    DefaultDatagramPacketPolicy policy = new DefaultDatagramPacketPolicy();
    Pair<Integer, DataCollection> result = policy.applyPolicy(msgPackMap);

    assertEquals(7, result.getLeft());
    assertEquals(messageData, result.getRight());
  }

  @Test
  @DisplayName("Test applying policy with empty data")
  void testApplyPolicyWithEmptyData() {
    DefaultDatagramPacketPolicy policy = new DefaultDatagramPacketPolicy();
    Pair<Integer, DataCollection> result = policy.applyPolicy(ZeroUtility.newZeroMap());
    assertEquals(Session.EMPTY_DATAGRAM_CONVEY_ID, result.getLeft());
    assertNull(result.getRight());
  }
}
