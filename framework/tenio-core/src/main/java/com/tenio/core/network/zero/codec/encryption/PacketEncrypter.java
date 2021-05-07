package com.tenio.core.network.zero.codec.encryption;

public interface PacketEncrypter {

	byte[] encrypt(byte[] binary) throws Exception;

	byte[] decrypt(byte[] binary) throws Exception;

}
