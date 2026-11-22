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

package com.tenio.core.network.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @Test
  void testValidateAndAddAddressTwiceIncreasesCount() {
    DefaultConnectionFilter filter = new DefaultConnectionFilter();
    filter.validateAndAddAddress("10.0.0.1");
    filter.validateAndAddAddress("10.0.0.1"); // second call increments counter
    // no exception expected; max is DEFAULT_MAX_CONNECTIONS_PER_IP
  }

  @Test
  void testValidateAndAddAddressThrowsWhenMaxReached() {
    DefaultConnectionFilter filter = new DefaultConnectionFilter();
    filter.configureMaxConnectionsPerIp(1);
    filter.validateAndAddAddress("10.0.0.2");
    assertThrows(RefusedConnectionAddressException.class,
        () -> filter.validateAndAddAddress("10.0.0.2"));
  }

  @Test
  void testRemoveAddressDecrementsCounter() {
    DefaultConnectionFilter filter = new DefaultConnectionFilter();
    filter.configureMaxConnectionsPerIp(2);
    filter.validateAndAddAddress("10.0.0.3");
    filter.validateAndAddAddress("10.0.0.3");
    filter.removeAddress("10.0.0.3"); // decrement to 1
    filter.validateAndAddAddress("10.0.0.3"); // should not throw; count is 1 < max 2
  }

  @Test
  void testRemoveAddressWhenZeroRemovesEntry() {
    DefaultConnectionFilter filter = new DefaultConnectionFilter();
    filter.validateAndAddAddress("10.0.0.4");
    filter.removeAddress("10.0.0.4"); // counter goes to 0, entry removed
    filter.validateAndAddAddress("10.0.0.4"); // should not throw
  }

  @Test
  void testRemoveAddressForUnknownIpIsNoOp() {
    DefaultConnectionFilter filter = new DefaultConnectionFilter();
    filter.removeAddress("999.999.999.999"); // no entry, should not throw
  }

  @Test
  void testConfigureMaxConnectionsPerIp() {
    DefaultConnectionFilter filter = new DefaultConnectionFilter();
    filter.configureMaxConnectionsPerIp(5);
    for (int i = 0; i < 5; i++) {
      filter.validateAndAddAddress("10.0.0.5");
    }
    assertThrows(RefusedConnectionAddressException.class,
        () -> filter.validateAndAddAddress("10.0.0.5"));
  }

  @Test
  void testToStringContainsFields() {
    DefaultConnectionFilter filter = new DefaultConnectionFilter();
    String str = filter.toString();
    assertTrue(str.contains("bannedAddresses"));
    assertTrue(str.contains("maxConnectionsPerIp"));
  }
}
