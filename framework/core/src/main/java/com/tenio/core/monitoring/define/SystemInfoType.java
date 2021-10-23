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

package com.tenio.core.monitoring.define;

/**
 * All the OS system information types.
 */
public enum SystemInfoType {

  OS_NAME("os.name"),
  OS_ARCH("os.arch"),
  OS_VERSION("os.version"),
  JAVA_VERSION("java.version"),
  JAVA_VENDOR("java.vendor"),
  JAVA_VENDOR_URL("java.vendor.url"),
  JAVA_VM_SPEC_VERSION("java.vm.specification.version"),
  JAVA_VM_VERSION("java.vm.version"),
  JAVA_VM_VENDOR("java.vm.vendor"),
  JAVA_VM_NAME("java.vm.name"),
  JAVA_IO_TMPDIR("java.io.tmpdir");

  private final String value;

  SystemInfoType(final String value) {
    this.value = value;
  }

  public final String getValue() {
    return this.value;
  }

  @Override
  public String toString() {
    return this.name();
  }
}
