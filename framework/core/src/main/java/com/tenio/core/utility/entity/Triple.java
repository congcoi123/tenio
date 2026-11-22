/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.core.utility.entity;

import java.util.Objects;

/**
 * Keeps references to three objects.
 *
 * @param first  the first object
 * @param second the second object
 * @param third  the third object
 * @param <F>    the first class template
 * @param <S>    the second class template
 * @param <T>    the third class template
 * @since 0.6.7
 */
public record Triple<F, S, T>(F first, S second, T third) {

  @Override
  public String toString() {
    return "Triple[first=" + describe(first) + ", second=" + describe(second)
        + ", third=" + describe(third) + "]";
  }

  private static String describe(Object value) {
    return switch (value) {
      case null -> "null";
      case String text -> text;
      case Boolean flag -> flag.toString();
      case Character character -> character.toString();
      case Byte number -> number.toString();
      case Short number -> number.toString();
      case Integer number -> number.toString();
      case Long number -> number.toString();
      case Float number -> number.toString();
      case Double number -> number.toString();
      case Enum<?> constant -> constant.name();
      default -> Objects.toIdentityString(value);
    };
  }
}
