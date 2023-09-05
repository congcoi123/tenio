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

package com.tenio.core.network.security.filter;

import com.tenio.core.exception.RefusedConnectionAddressException;

/**
 * Provides APIs for working with the connection filter.
 */
public interface ConnectionFilter {

  /**
   * Appends an IP address into the banned list.
   *
   * @param addressIp a {@link String} IP address appended into the banned list
   */
  void addBannedAddress(String addressIp);

  /**
   * Removes an IP address from the banned list.
   *
   * @param addressIp a {@link String} IP address removed from the banned list
   */
  void removeBannedAddress(String addressIp);

  /**
   * Retrieves all banned IP addresses.
   *
   * @return a list of {@link String} banned IP addresses
   */
  String[] getBannedAddresses();

  /**
   * Validates an IP address and adds it into the verified list if applicable.
   *
   * @param addressIp a checking {@link String} IP address
   * @throws RefusedConnectionAddressException if the IP address violates any filter condition
   */
  void validateAndAddAddress(String addressIp) throws RefusedConnectionAddressException;

  /**
   * Removes an IP address from the verified list.
   *
   * @param addressIp a {@link String} IP address removed from the verified list
   */
  void removeAddress(String addressIp);

  /**
   * Retrieves the maximum number of connection allowed in a particular IP address.
   *
   * @return the maximum number of connection allowed in a particular IP address
   */
  int getMaxConnectionsPerIp();

  /**
   * Sets the maximum number of connection allowed in a particular IP address.
   *
   * @param maxConnections the maximum number of connection ({@code integer} value) allowed
   *                       in a particular IP address
   */
  void setMaxConnectionsPerIp(int maxConnections);
}
