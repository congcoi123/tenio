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
package com.tenio.core.network.security.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.core.exceptions.RefusedConnectionAddressException;

@ThreadSafe
public final class DefaultConnectionFilter implements ConnectionFilter {

	private static final int DEFAULT_MAX_CONNECTIONS_PER_IP = 10;

	private final Set<String> __bannedAddresses;
	private final Map<String, AtomicInteger> __addressMap;
	private volatile int __maxConnectionsPerIp;

	public DefaultConnectionFilter() {
		__bannedAddresses = new HashSet<String>();
		__addressMap = new HashMap<String, AtomicInteger>();
		__maxConnectionsPerIp = DEFAULT_MAX_CONNECTIONS_PER_IP;
	}

	@Override
	public void addBannedAddress(String addressIp) {
		synchronized (__bannedAddresses) {
			__bannedAddresses.add(addressIp);
		}
	}

	@Override
	public void removeBannedAddress(String addressIp) {
		synchronized (__bannedAddresses) {
			__bannedAddresses.remove(addressIp);
		}
	}

	@Override
	public String[] getBannedAddresses() {
		String[] set = null;
		synchronized (__bannedAddresses) {
			set = new String[__bannedAddresses.size()];
			set = __bannedAddresses.toArray(set);
			return set;
		}
	}

	@Override
	public void validateAndAddAddress(String addressIp) {
		if (__isAddressBanned(addressIp)) {
			throw new RefusedConnectionAddressException("The IP address has banned", addressIp);
		}

		synchronized (__addressMap) {
			AtomicInteger counter = __addressMap.get(addressIp);
			if (counter != null && counter.intValue() >= __maxConnectionsPerIp) {
				throw new RefusedConnectionAddressException(
						String.format("The IP address has reached maximum (%d) allowed connection", counter.intValue()),
						addressIp);
			}

			if (counter == null) {
				counter = new AtomicInteger(1);
				__addressMap.put(addressIp, counter);
			} else {
				counter.incrementAndGet();
			}
		}
	}

	@Override
	public void removeAddress(String addressIp) {
		synchronized (__addressMap) {
			AtomicInteger counter = __addressMap.get(addressIp);
			if (counter != null) {
				int value = counter.decrementAndGet();
				if (value == 0) {
					__addressMap.remove(addressIp);
				}
			}
		}
	}

	@Override
	public int getMaxConnectionsPerIp() {
		return __maxConnectionsPerIp;
	}

	@Override
	public void setMaxConnectionsPerIp(int maxConnections) {
		__maxConnectionsPerIp = maxConnections;
	}

	private boolean __isAddressBanned(String addressIp) {
		synchronized (__bannedAddresses) {
			return __bannedAddresses.contains(addressIp);
		}
	}

}
