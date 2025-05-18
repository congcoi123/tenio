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

package com.tenio.core.network.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tenio.core.network.define.TransportType;
import org.junit.jupiter.api.Test;

class SocketConfigurationTest {
  @Test
  void testConstructor() {
    SocketConfiguration
        actualSocketConfiguration = new SocketConfiguration("Name", TransportType.UNKNOWN, 8080
        , 0);

    assertEquals("Name", actualSocketConfiguration.name());
    assertEquals(8080, actualSocketConfiguration.port());
    assertEquals(TransportType.UNKNOWN, actualSocketConfiguration.type());
    assertEquals("SocketConfiguration[name=Name, type=UNKNOWN, port=8080, cacheSize=0]",
        actualSocketConfiguration.toString());
  }
}
