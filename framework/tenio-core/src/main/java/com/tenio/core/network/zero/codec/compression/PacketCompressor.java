package com.tenio.core.network.zero.codec.compression;

public interface PacketCompressor {

	byte[] compress(byte[] binary) throws Exception;

	byte[] uncompress(byte[] binary) throws Exception;

}
