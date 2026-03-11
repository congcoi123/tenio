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

package com.tenio.core.utility;

import java.util.Set;

/**
 * The class provides utility methods to work with exceptions.
 *
 * @since 0.6.8
 */
public final class ExceptionUtility {

  public static final Set<String> IGNORE_SOCKET_EXCEPTIONS = Set.of(
      "broken pipe",
      "connection reset"
  );

  private ExceptionUtility() {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if the given exception's message contains any of the provided keywords.
   *
   * @param exception the exception to check
   * @param keywords  a set of keywords to search for (case-insensitive)
   * @return {@code true} if the message contains any keyword, {@code false} otherwise
   */
  public static boolean messageContains(Exception exception, Set<String> keywords) {
    if (exception == null || exception.getMessage() == null || keywords == null ||
        keywords.isEmpty()) {
      return false;
    }
    String message = exception.getMessage().toLowerCase();
    for (String keyword : keywords) {
      if (keyword != null && message.contains(keyword.toLowerCase())) {
        return true;
      }
    }
    return false;
  }
}
