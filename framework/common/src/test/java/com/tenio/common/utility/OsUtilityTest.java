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

package com.tenio.common.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

@DisplayName("Unit Test Cases For OS Utility")
class OsUtilityTest {

  @Test
  @EnabledOnOs(OS.MAC)
  @DisplayName("Fetch operation system type should return MAC")
  void getOperatingSystemTypeMac() {
    assertEquals(OsUtility.OsType.MAC, OsUtility.getOperatingSystemType());
  }

  @Test
  @EnabledIfSystemProperty(named = "os.name", matches = "win")
  @DisplayName("Fetch operation system type should return WINDOWS")
  void getOperatingSystemTypeWindows() {
    assertEquals(OsUtility.OsType.WINDOWS, OsUtility.getOperatingSystemType());
  }

  @Test
  @EnabledIfSystemProperty(named = "os.name", matches = "nux")
  @DisplayName("Fetch operation system type should return LINUX")
  void getOperatingSystemTypeLinux() {
    assertEquals(OsUtility.OsType.LINUX, OsUtility.getOperatingSystemType());
  }

  @Test
  @EnabledOnOs(OS.OTHER)
  @DisplayName("Fetch operation system type should return OTHER")
  void getOperatingSystemTypeOther() {
    assertEquals(OsUtility.OsType.OTHER, OsUtility.getOperatingSystemType());
  }
}
