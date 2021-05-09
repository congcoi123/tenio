package com.tenio.core.network.zero.codec.packet;

import java.nio.ByteBuffer;

public final class PendingPacket {
	private PacketHeader header;
	private ByteBuffer buffer;
	private int expectedLength;

	public PacketHeader getPacketHeader() {
		return this.header;
	}

	public void setPacketHeader(PacketHeader header) {
		this.header = header;
	}

	public ByteBuffer getBuffer() {
		return this.buffer;
	}

	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}
	
	public int getExpectedLength() {
		return expectedLength;
	}
	
	public void setExpectedLength(int expectedLength) {
		this.expectedLength = expectedLength;
	}

}
