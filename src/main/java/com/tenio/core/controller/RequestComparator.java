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

import com.tenio.core.network.define.RequestPriority;
import com.tenio.core.network.entity.protocol.Request;
import java.util.Comparator;
import java.util.Objects;
import org.apache.logging.log4j.core.tools.picocli.CommandLine.InitializationException;

/**
 * A comparator implementation for sorting requests in the controller's request queue
 * based on their priority levels. This class ensures that high-priority requests
 * are processed before lower-priority ones.
 *
 * <p>Key features:
 * <ul>
 *   <li>Priority-based request ordering</li>
 *   <li>Support for all {@link RequestPriority} levels</li>
 *   <li>Null-safe comparison</li>
 *   <li>Consistent ordering for equal priorities</li>
 * </ul>
 *
 * <p>Thread safety: This class is thread-safe as it is stateless and
 * implements a pure comparison function.
 *
 * @see RequestPriority
 * @see AbstractController
 * @see Request
 * @see Comparator
 */
public final class RequestComparator implements Comparator<Request> {

  private static final RequestComparator instance = new RequestComparator();

  private RequestComparator() {
    if (Objects.nonNull(instance)) {
      throw new InitializationException("Could not recreate this class' instance");
    }
  }

  /**
   * Creates a new instance.
   *
   * @return a {@link RequestComparator} instance
   */
  public static RequestComparator newInstance() {
    return instance;
  }

  @Override
  public int compare(Request request1, Request request2) {
    int result;

    if (request1.getPriority().getValue() < request2.getPriority().getValue()) {
      result = -1;
    } else if (request1.getPriority() == request2.getPriority()) {
      result = Long.compare(request1.getCreatedTimestamp(), request2.getCreatedTimestamp());
    } else {
      result = 1;
    }
    return result;
  }
}
