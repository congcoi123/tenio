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

package com.tenio.core.network.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tenio.core.exception.RefusedConnectionAddressException;
import org.junit.jupiter.api.Test;

class DefaultConnectionFilterTest {

  @Test
  void testAddBannedAddress() {
    DefaultConnectionFilter defaultConnectionFilter = new DefaultConnectionFilter();
    defaultConnectionFilter.addBannedAddress("42 Main St");
    assertEquals(1, defaultConnectionFilter.getBannedAddresses().length);
  }

  @Test
  void testRemoveBannedAddress() {
    DefaultConnectionFilter defaultConnectionFilter = new DefaultConnectionFilter();
    defaultConnectionFilter.removeBannedAddress("42 Main St");
    assertEquals(0, defaultConnectionFilter.getBannedAddresses().length);
  }

  @Test
  void testGetBannedAddresses() {
    assertEquals(0, (new DefaultConnectionFilter()).getBannedAddresses().length);
  }

  @Test
  void testValidateAndAddAddress() {
    (new DefaultConnectionFilter()).validateAndAddAddress("42 Main St");
  }

  @Test
  void testValidateAndAddAddress2() {
    DefaultConnectionFilter defaultConnectionFilter = new DefaultConnectionFilter();
    defaultConnectionFilter.addBannedAddress("42 Main St");
    assertThrows(RefusedConnectionAddressException.class,
        () -> defaultConnectionFilter.validateAndAddAddress("42 Main St"));
  }
}
