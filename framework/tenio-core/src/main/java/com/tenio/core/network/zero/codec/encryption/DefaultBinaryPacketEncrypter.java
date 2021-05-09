package com.tenio.core.network.zero.codec.encryption;

public final class DefaultBinaryPacketEncrypter implements PacketEncrypter {

	@Override
	public byte[] decrypt(byte[] binary) {
		return binary;
	}

	@Override
	public byte[] encrypt(byte[] binary) {
		return binary;
	}

}
