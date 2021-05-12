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

import com.tenio.core.exceptions.RefusedAddressException;

@ThreadSafe
public final class DefaultConnectionFilter implements ConnectionFilter {

	private final Set<String> __bannedAddresses;
	private final Map addressMap = new HashMap<>();
	private volatile int maxConnectionsPerIp = 10;

	public DefaultConnectionFilter() {
		__bannedAddresses = new HashSet();
	}

	public void addBannedAddress(String ipAddress) {
		synchronized (this.__bannedAddresses) {
			this.__bannedAddresses.add(ipAddress);
		}
	}

	public String[] getBannedAddresses() {
		String[] set = (String[]) null;
		synchronized (this.__bannedAddresses) {
			set = new String[this.__bannedAddresses.size()];
			set = (String[]) ((String[]) this.__bannedAddresses.toArray(set));
			return set;
		}
	}

	public int getMaxConnectionsPerIp() {
		return this.maxConnectionsPerIp;
	}

	public void removeAddress(String ipAddress) {
		synchronized (this.addressMap) {
			AtomicInteger count = (AtomicInteger) this.addressMap.get(ipAddress);
			if (count != null) {
				int value = count.decrementAndGet();
				if (value == 0) {
					this.addressMap.remove(ipAddress);
				}
			}

		}
	}

	public void removeBannedAddress(String ipAddress) {
		synchronized (this.__bannedAddresses) {
			this.__bannedAddresses.remove(ipAddress);
		}
	}

	public void setMaxConnectionsPerIp(int max) {
		this.maxConnectionsPerIp = max;
	}

	public void validateAndAddAddress(String ipAddress) throws RefusedAddressException {

		if (this.isAddressBanned(ipAddress)) {
			throw new RefusedAddressException("Ip Address: " + ipAddress + " has banned.");
		} else {
			synchronized (this.addressMap) {
				AtomicInteger count = (AtomicInteger) this.addressMap.get(ipAddress);
				if (count != null && count.intValue() >= this.maxConnectionsPerIp) {
					throw new RefusedAddressException(
							"Ip Address: " + ipAddress + " has reached maximum allowed connections.");
				} else {
					if (count == null) {
						count = new AtomicInteger(1);
						this.addressMap.put(ipAddress, count);
					} else {
						count.incrementAndGet();
					}

				}
			}
		}
	}

	private boolean isAddressBanned(String ip) {
		boolean isBanned = false;
		synchronized (this.__bannedAddresses) {
			isBanned = this.__bannedAddresses.contains(ip);
			return isBanned;
		}
	}

}
