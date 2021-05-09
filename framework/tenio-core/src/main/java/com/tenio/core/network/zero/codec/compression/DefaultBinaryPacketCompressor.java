package com.tenio.core.network.zero.codec.compression;

public final class DefaultBinaryPacketCompressor implements PacketCompressor {

	@Override
	public byte[] compress(byte[] binary) {
		return binary;
	}

	@Override
	public byte[] uncompress(byte[] binary) {
		return binary;
	}

}
