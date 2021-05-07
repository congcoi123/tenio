package com.tenio.core.network.zero.codec.encryption;

public final class BinaryPacketEncrypter implements PacketEncrypter {

	@Override
	public byte[] decrypt(byte[] binary) {
		return binary;
	}

	@Override
	public byte[] encrypt(byte[] binary) {
		return binary;
	}

}
