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

package com.tenio.core.utility;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * The utility class for server.
 *
 * @since 0.6.7
 */
public final class ZeroUtility {

  private static final int CONVERT_TO_MB = 1024 * 1024;

  private ZeroUtility() {
    throw new UnsupportedOperationException();
  }

  /**
   * Converts from Bytes to Megabytes.
   *
   * @param bytes value in Bytes
   * @return value in Megabytes
   */
  public static float convertBytesToMB(long bytes) {
    return (float) bytes / CONVERT_TO_MB;
  }

  /**
   * Formats a numeric value using underscore ({@code _}) as a thousand separator.
   * <p>
   * This method converts the given {@code long} into a human-readable string
   * with digit grouping every three digits. For example:
   * <ul>
   *   <li>{@code 1000 -> "1_000"}</li>
   *   <li>{@code 1000000 -> "1_000_000"}</li>
   * </ul>
   * <p>
   * Internally, this method uses standard number formatting with grouping
   * separators and replaces commas with underscores.
   *
   * @param value the numeric value to format
   * @return a string representation of the value with underscore-separated digit groups
   * @since 0.7.0
   */
  public static String formatWithUnderscores(long value) {
    NumberFormat nf = NumberFormat.getInstance(Locale.US);
    nf.setGroupingUsed(true);
    return nf.format(value).replace(',', '_');
  }
}
