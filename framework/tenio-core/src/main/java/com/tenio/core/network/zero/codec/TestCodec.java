package com.tenio.core.network.zero.codec;

import com.tenio.core.network.entity.packet.PacketHeader;

public final class TestCodec {

	public static void main(String[] args) {

		var packetHeader1 = PacketHeader.newInstance(false, true, true, false);
		System.out.println(packetHeader1);

		var headerByte1 = CodecUtility.encodeFirstHeaderByte(packetHeader1);
		System.err.println(headerByte1);

		var packetHeaderDecode1 = CodecUtility.decodeFirstHeaderByte(headerByte1);
		System.out.println(packetHeaderDecode1);

	}

}
