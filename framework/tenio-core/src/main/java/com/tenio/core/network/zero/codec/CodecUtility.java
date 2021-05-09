package com.tenio.core.network.zero.codec;

import com.tenio.core.network.zero.codec.packet.PacketHeader;
import com.tenio.core.network.zero.codec.packet.PacketHeaderType;

public final class CodecUtility {

	public static PacketHeader decodeFirstHeaderByte(byte headerByte) {
		return PacketHeader.newInstance((headerByte & PacketHeaderType.BINARY.getValue()) > 0,
				(headerByte & PacketHeaderType.COMPRESSION.getValue()) > 0,
				(headerByte & PacketHeaderType.BIG_SIZE.getValue()) > 0,
				(headerByte & PacketHeaderType.ENCRYPTION.getValue()) > 0);
	}

	public static byte encodeFirstHeaderByte(PacketHeader packetHeader) {
		byte headerByte = 0;

		if (packetHeader.isBinary()) {
			headerByte = (byte) (headerByte | PacketHeaderType.BINARY.getValue());
		}

		if (packetHeader.isCompressed()) {
			headerByte = (byte) (headerByte | PacketHeaderType.COMPRESSION.getValue());
		}

		if (packetHeader.isBigSized()) {
			headerByte = (byte) (headerByte | PacketHeaderType.BIG_SIZE.getValue());
		}

		if (packetHeader.isEncrypted()) {
			headerByte = (byte) (headerByte | PacketHeaderType.ENCRYPTION.getValue());
		}

		return headerByte;
	}

}
