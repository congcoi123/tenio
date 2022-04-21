package com.tenio.core.network.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tenio.core.exception.RefusedConnectionAddressException;
import org.junit.jupiter.api.Test;

class DefaultConnectionFilterTest {
  @Test
  void testConstructor() {
    DefaultConnectionFilter actualDefaultConnectionFilter = new DefaultConnectionFilter();
    actualDefaultConnectionFilter.setMaxConnectionsPerIp(3);
    assertEquals(3, actualDefaultConnectionFilter.getMaxConnectionsPerIp());
  }

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
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by validateAndAddAddress(String)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

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
  void testRemoveAddress() {
    DefaultConnectionFilter defaultConnectionFilter = new DefaultConnectionFilter();
    defaultConnectionFilter.removeAddress("42 Main St");
    assertEquals(10, defaultConnectionFilter.getMaxConnectionsPerIp());
  }
}

