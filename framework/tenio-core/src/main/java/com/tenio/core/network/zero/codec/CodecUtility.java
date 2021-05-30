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
package com.tenio.core.network.zero.codec;

import com.tenio.core.network.zero.codec.packet.PacketHeader;
import com.tenio.core.network.zero.codec.packet.PacketHeaderType;

public final class CodecUtility {

	private CodecUtility() {

	}

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
