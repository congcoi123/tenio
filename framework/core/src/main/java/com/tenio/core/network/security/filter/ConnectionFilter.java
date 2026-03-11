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

import com.tenio.core.exception.RefusedConnectionAddressException;

/**
 * Defines the interface for connection filtering and security management.
 * This interface provides mechanisms to control and validate incoming connections
 * based on IP addresses, connection limits, and banned address lists.
 *
 * <p>Key features:
 * <ul>
 *   <li>IP address validation and tracking</li>
 *   <li>Connection limit enforcement per IP</li>
 *   <li>Banned address management</li>
 *   <li>Verified address tracking</li>
 *   <li>Configurable connection limits</li>
 * </ul>
 *
 * <p>Note: This interface is crucial for implementing security measures
 * and preventing abuse of the server's resources. It should be properly
 * configured to balance security with accessibility.
 *
 * @see DefaultConnectionFilter
 * @see RefusedConnectionAddressException
 * @since 0.3.0
 */
public interface ConnectionFilter {

  /**
   * Default maximum number of connections per ip.
   */
  int DEFAULT_MAX_CONNECTIONS_PER_IP = 10;

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
   * @return an array of {@link String} banned IP addresses
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
   * Sets the maximum number of connection allowed in a particular IP address.
   *
   * @param maxConnections the maximum number of connection ({@code integer} value) allowed
   *                       in a particular IP address
   */
  void configureMaxConnectionsPerIp(int maxConnections);
}
