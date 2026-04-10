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

package com.tenio.core.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For DefaultCoreConfiguration")
class DefaultCoreConfigurationTest {

  @Test
  @DisplayName("DefaultCoreConfiguration is a CoreConfiguration")
  void testIsACoreConfiguration() {
    assertInstanceOf(CoreConfiguration.class, new DefaultCoreConfiguration());
  }

  @Test
  @DisplayName("load with null file path does not throw")
  void testLoadWithNullFileDoesNotThrow() {
    DefaultCoreConfiguration config = new DefaultCoreConfiguration();
    assertDoesNotThrow(() -> config.load(null));
  }

  @Test
  @DisplayName("Two instances are independent objects")
  void testTwoInstancesAreIndependent() {
    DefaultCoreConfiguration a = new DefaultCoreConfiguration();
    DefaultCoreConfiguration b = new DefaultCoreConfiguration();
    assertInstanceOf(DefaultCoreConfiguration.class, a);
    assertInstanceOf(DefaultCoreConfiguration.class, b);
  }
}
