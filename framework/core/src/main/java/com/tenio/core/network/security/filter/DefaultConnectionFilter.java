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

import com.tenio.core.exception.RefusedConnectionAddressException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The default implementation for the connection filter.
 */
public class DefaultConnectionFilter implements ConnectionFilter {

  private final Set<String> bannedAddresses;
  private final Map<String, Integer> addressMap;
  private final Object addressLock;
  private volatile int maxConnectionsPerIp;

  /**
   * Initialization.
   */
  public DefaultConnectionFilter() {
    bannedAddresses = new HashSet<>();
    addressMap = new HashMap<>();
    addressLock = new Object();
    maxConnectionsPerIp = DEFAULT_MAX_CONNECTIONS_PER_IP;
  }

  @Override
  public void addBannedAddress(String addressIp) {
    synchronized (addressLock) {
      bannedAddresses.add(addressIp);
    }
  }

  @Override
  public void removeBannedAddress(String addressIp) {
    synchronized (addressLock) {
      bannedAddresses.remove(addressIp);
    }
  }

  @Override
  public String[] getBannedAddresses() {
    String[] set;
    synchronized (addressLock) {
      set = new String[bannedAddresses.size()];
      set = bannedAddresses.toArray(set);
      return set;
    }
  }

  @Override
  public void validateAndAddAddress(String addressIp) {
    synchronized (addressLock) {
      if (bannedAddresses.contains(addressIp)) {
        throw new RefusedConnectionAddressException("The IP address has banned", addressIp);
      }
      int counter = addressMap.getOrDefault(addressIp, 0);
      if (counter >= maxConnectionsPerIp) {
        throw new RefusedConnectionAddressException(
            String.format("The IP address has reached maximum (%d) allowed connection",
                counter),
            addressIp);
      }
      addressMap.put(addressIp, counter + 1);
    }
  }

  @Override
  public void removeAddress(String addressIp) {
    synchronized (addressLock) {
      Integer counter = addressMap.get(addressIp);
      if (counter != null) {
        if (counter <= 1) {
          addressMap.remove(addressIp);
        } else {
          addressMap.put(addressIp, counter - 1);
        }
      }
    }
  }

  @Override
  public void configureMaxConnectionsPerIp(int maxConnections) {
    maxConnectionsPerIp = maxConnections;
  }

  @Override
  public String toString() {
    synchronized (addressLock) {
      return "DefaultConnectionFilter{" +
          "bannedAddresses=" + bannedAddresses +
          ", addressMap=" + addressMap +
          ", maxConnectionsPerIp=" + maxConnectionsPerIp +
          '}';
    }
  }
}
