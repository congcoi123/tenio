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

package com.tenio.examples.client;

import java.security.SecureRandom;

/**
 * The utility class for client.
 */
public final class ClientUtility {

  private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
  private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
  private static final String NUMBER = "0123456789";
  private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
  private static final int SECONDS_IN_HOUR = 3600;
  private static final int SECONDS_IN_MINUTE = 60;
  private static final SecureRandom RANDOM = new SecureRandom();

  private ClientUtility() {
    throw new UnsupportedOperationException();
  }

  public static String generateRandomString(int length) {
    if (length < 1) {
      throw new IllegalArgumentException();
    }

    var sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      // 0-62 (exclusive), random returns 0-61
      int rndCharAt = RANDOM.nextInt(DATA_FOR_RANDOM_STRING.length() - 1);
      char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

      sb.append(rndChar);
    }

    return sb.toString();
  }

  public static String getTimeFormat(long seconds) {
    long hours = seconds / SECONDS_IN_HOUR;
    long minutes = (seconds % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE;
    long secs = seconds % SECONDS_IN_MINUTE;

    return String.format("%02d:%02d:%02d", hours, minutes, secs);
  }
}
