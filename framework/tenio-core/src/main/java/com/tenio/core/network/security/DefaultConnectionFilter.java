package com.tenio.core.network.security;

import java.util.ArrayList;
import java.util.List;

import com.tenio.core.exception.RefusedAddressException;

public class DefaultConnectionFilter implements IConnectionFilter {

	private final List<String> __bannedAddresses;

	public DefaultConnectionFilter() {
		__bannedAddresses = new ArrayList<String>();
	}

	@Override
	public void addBannedAddress(String ipAddress) {
		synchronized (__bannedAddresses) {
			__bannedAddresses.add(ipAddress);
		}
	}

	@Override
	public void removeBannedAddress(String ipAddress) {
		synchronized (__bannedAddresses) {
			__bannedAddresses.remove(ipAddress);
		}
	}

	@Override
	public List<String> getBannedAddresses() {
		synchronized (__bannedAddresses) {
			return __bannedAddresses;
		}
	}

	@Override
	public void validateAndAddAddress(String ipAddress) throws RefusedAddressException {
		if (__isAddressBanned(ipAddress)) {
			throw new RefusedAddressException(ipAddress);
		}
	}

	private boolean __isAddressBanned(String ipAddress) {
		synchronized (__bannedAddresses) {
			return __bannedAddresses.contains(ipAddress);
		}
	}

}
