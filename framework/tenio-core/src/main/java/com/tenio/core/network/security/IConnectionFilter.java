package com.tenio.core.network.security;

import java.util.List;

import com.tenio.core.exception.RefusedAddressException;

public interface IConnectionFilter {

	void addBannedAddress(final String address);

	void removeBannedAddress(final String address);

	List<String> getBannedAddresses();

	void validateAndAddAddress(final String address) throws RefusedAddressException;

}
