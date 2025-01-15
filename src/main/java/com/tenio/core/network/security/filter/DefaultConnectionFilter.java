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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The default implementation for the connection filter.
 */
@ThreadSafe
public final class DefaultConnectionFilter implements ConnectionFilter {

  private final Set<String> bannedAddresses;
  private final Map<String, AtomicInteger> addressMap;
  private int maxConnectionsPerIp;

  /**
   * Initialization.
   */
  public DefaultConnectionFilter() {
    bannedAddresses = new HashSet<>();
    addressMap = new HashMap<>();
    maxConnectionsPerIp = DEFAULT_MAX_CONNECTIONS_PER_IP;
  }

  @Override
  public void addBannedAddress(String addressIp) {
    synchronized (bannedAddresses) {
      bannedAddresses.add(addressIp);
    }
  }

  @Override
  public void removeBannedAddress(String addressIp) {
    synchronized (bannedAddresses) {
      bannedAddresses.remove(addressIp);
    }
  }

  @Override
  public String[] getBannedAddresses() {
    String[] set;
    synchronized (bannedAddresses) {
      set = new String[bannedAddresses.size()];
      set = bannedAddresses.toArray(set);
      return set;
    }
  }

  @Override
  public void validateAndAddAddress(String addressIp) {
    if (isAddressBanned(addressIp)) {
      throw new RefusedConnectionAddressException("The IP address has banned", addressIp);
    }

    synchronized (addressMap) {
      var counter = addressMap.get(addressIp);
      if (Objects.nonNull(counter) && counter.intValue() >= maxConnectionsPerIp) {
        throw new RefusedConnectionAddressException(
            String.format("The IP address has reached maximum (%d) allowed connection",
                counter.intValue()),
            addressIp);
      }

      if (Objects.isNull(counter)) {
        counter = new AtomicInteger(1);
        addressMap.put(addressIp, counter);
      } else {
        counter.incrementAndGet();
      }
    }
  }

  @Override
  public void removeAddress(String addressIp) {
    synchronized (addressMap) {
      var counter = addressMap.get(addressIp);
      if (Objects.nonNull(counter)) {
        int value = counter.decrementAndGet();
        if (value == 0) {
          addressMap.remove(addressIp);
        }
      }
    }
  }

  @Override
  public void configureMaxConnectionsPerIp(int maxConnections) {
    maxConnectionsPerIp = maxConnections;
  }

  private boolean isAddressBanned(String addressIp) {
    synchronized (bannedAddresses) {
      return bannedAddresses.contains(addressIp);
    }
  }

  @Override
  public String toString() {
    return "DefaultConnectionFilter{" +
        "bannedAddresses=" + bannedAddresses +
        ", addressMap=" + addressMap +
        ", maxConnectionsPerIp=" + maxConnectionsPerIp +
        '}';
  }
}
