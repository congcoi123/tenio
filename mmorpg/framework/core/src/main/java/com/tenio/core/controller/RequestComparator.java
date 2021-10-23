/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

import com.tenio.core.network.entity.protocol.Request;
import java.util.Comparator;
import org.apache.logging.log4j.core.tools.picocli.CommandLine.InitializationException;

/**
 * This class provides the comparator using for sort the requests bases on their priorities in
 * the controller requests queue.
 */
public final class RequestComparator implements Comparator<Request> {

  private static final RequestComparator instance = new RequestComparator();

  private RequestComparator() {
    if (instance != null) {
      throw new InitializationException("Could not recreate this class' instance");
    }
  }

  public static RequestComparator newInstance() {
    return instance;
  }

  @Override
  public int compare(Request request1, Request request2) {
    int result = 0;

    if (request1.getPriority().getValue() < request2.getPriority().getValue()) {
      result = -1;
    } else if (request1.getPriority() == request2.getPriority()) {
      if (request1.getTimestamp() < request2.getTimestamp()) {
        result = -1;
      } else if (request1.getTimestamp() > request2.getTimestamp()) {
        result = 1;
      } else {
        result = 0;
      }
    } else {
      result = 1;
    }
    return result;
  }
}
