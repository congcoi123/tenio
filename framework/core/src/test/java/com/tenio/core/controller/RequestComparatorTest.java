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

package com.tenio.core.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.core.network.define.RequestPriority;
import com.tenio.core.network.entity.protocol.Request;
import org.junit.jupiter.api.Test;

class RequestComparatorTest {
  @Test
  void testNewInstance() {
    RequestComparator actualNewInstanceResult = RequestComparator.newInstance();
    Request request = mock(Request.class);
    when(request.getCreatedTimestamp()).thenReturn(10L);
    when(request.getPriority()).thenReturn(RequestPriority.LOWEST);
    Request request1 = mock(Request.class);
    when(request1.getCreatedTimestamp()).thenReturn(10L);
    when(request1.getPriority()).thenReturn(RequestPriority.LOWEST);
    int actualCompareResult = actualNewInstanceResult.compare(request, request1);
    verify(request, atLeast(1)).getPriority();
    verify(request, atLeast(1)).getCreatedTimestamp();
    verify(request1, atLeast(1)).getPriority();
    verify(request1, atLeast(1)).getCreatedTimestamp();
    assertEquals(0, actualCompareResult);
  }

  @Test
  void testNewInstance2() {
    RequestComparator actualNewInstanceResult = RequestComparator.newInstance();
    Request request = mock(Request.class);
    when(request.getCreatedTimestamp()).thenReturn(0L);
    when(request.getPriority()).thenReturn(RequestPriority.LOWEST);
    Request request1 = mock(Request.class);
    when(request1.getCreatedTimestamp()).thenReturn(10L);
    when(request1.getPriority()).thenReturn(RequestPriority.LOWEST);
    int actualCompareResult = actualNewInstanceResult.compare(request, request1);
    verify(request, atLeast(1)).getPriority();
    verify(request).getCreatedTimestamp();
    verify(request1, atLeast(1)).getPriority();
    verify(request1).getCreatedTimestamp();
    assertEquals(-1, actualCompareResult);
  }

  @Test
  void testNewInstance3() {
    RequestComparator actualNewInstanceResult = RequestComparator.newInstance();
    Request request = mock(Request.class);
    when(request.getCreatedTimestamp()).thenReturn(Long.MAX_VALUE);
    when(request.getPriority()).thenReturn(RequestPriority.LOWEST);
    Request request1 = mock(Request.class);
    when(request1.getCreatedTimestamp()).thenReturn(10L);
    when(request1.getPriority()).thenReturn(RequestPriority.LOWEST);
    int actualCompareResult = actualNewInstanceResult.compare(request, request1);
    verify(request, atLeast(1)).getPriority();
    verify(request, atLeast(1)).getCreatedTimestamp();
    verify(request1, atLeast(1)).getPriority();
    verify(request1, atLeast(1)).getCreatedTimestamp();
    assertEquals(1, actualCompareResult);
  }

  @Test
  void testNewInstance4() {
    RequestComparator actualNewInstanceResult = RequestComparator.newInstance();
    Request request = mock(Request.class);
    when(request.getCreatedTimestamp()).thenReturn(10L);
    when(request.getPriority()).thenReturn(RequestPriority.LOW);
    Request request1 = mock(Request.class);
    when(request1.getCreatedTimestamp()).thenReturn(10L);
    when(request1.getPriority()).thenReturn(RequestPriority.LOWEST);
    int actualCompareResult = actualNewInstanceResult.compare(request, request1);
    verify(request, atLeast(1)).getPriority();
    verify(request1, atLeast(1)).getPriority();
    assertEquals(1, actualCompareResult);
  }

  @Test
  void testNewInstance5() {
    RequestComparator actualNewInstanceResult = RequestComparator.newInstance();
    Request request = mock(Request.class);
    when(request.getCreatedTimestamp()).thenReturn(10L);
    when(request.getPriority()).thenReturn(RequestPriority.LOWEST);
    Request request1 = mock(Request.class);
    when(request1.getCreatedTimestamp()).thenReturn(10L);
    when(request1.getPriority()).thenReturn(RequestPriority.LOW);
    int actualCompareResult = actualNewInstanceResult.compare(request, request1);
    verify(request).getPriority();
    verify(request1).getPriority();
    assertEquals(-1, actualCompareResult);
  }
}
