/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.common.utility;

import java.util.Locale;

/**
 * This class provides some utility methods to work with OS.
 */
public final class OsUtility {

  private OsUtility() {
    throw new UnsupportedOperationException("This class does not support to create a new instance");
  }

  /**
   * Detect the operating system from the os.name System property and cache the
   * result.
   *
   * @return The operating system detected
   */
  public static OsType getOperatingSystemType() {
    var osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    if ((osName.contains("mac")) || (osName.contains("darwin"))) {
      return OsType.MAC;
    } else if (osName.contains("win")) {
      return OsType.WINDOWS;
    } else if (osName.contains("nux")) {
      return OsType.LINUX;
    } else {
      return OsType.OTHER;
    }
  }

  /**
   * Types of Operating Systems.
   */
  public enum OsType {
    /**
     * Windows OS.
     */
    WINDOWS,
    /**
     * Mac OS.
     */
    MAC,
    /**
     * Unix type OS.
     */
    LINUX,
    /**
     * Unknown OS.
     */
    OTHER;

    @Override
    public String toString() {
      return name();
    }
  }
}
