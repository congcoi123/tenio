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

package com.tenio.core.monitoring.define;

/**
 * All OS system and Java environment information flags.
 */
public enum SystemInfoType {

  /**
   * The OS name.
   */
  OS_NAME("os.name"),
  /**
   * The OS architecture.
   */
  OS_ARCH("os.arch"),
  /**
   * The OS version.
   */
  OS_VERSION("os.version"),
  /**
   * The Java version.
   */
  JAVA_VERSION("java.version"),
  /**
   * The Java vendor.
   */
  JAVA_VENDOR("java.vendor"),
  /**
   * The Java vendor URL.
   */
  JAVA_VENDOR_URL("java.vendor.url"),
  /**
   * The Java Virtual Machine specification version.
   */
  JAVA_VM_SPEC_VERSION("java.vm.specification.version"),
  /**
   * The Java Virtual Machine version.
   */
  JAVA_VM_VERSION("java.vm.version"),
  /**
   * The Java Virtual Machine vendor.
   */
  JAVA_VM_VENDOR("java.vm.vendor"),
  /**
   * The Java Virtual Machine name.
   */
  JAVA_VM_NAME("java.vm.name"),
  /**
   * The Java IO temporary directory.
   */
  JAVA_IO_TMPDIR("java.io.tmpdir");

  private final String value;

  SystemInfoType(final String value) {
    this.value = value;
  }

  /**
   * Retrieves flag's name.
   *
   * @return the {@link String} flag's name
   */
  public final String getValue() {
    return this.value;
  }

  @Override
  public String toString() {
    return this.name();
  }
}
