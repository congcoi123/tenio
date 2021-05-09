package com.tenio.core.network.zero.codec.packet;

public final class ProcessedPacket {
	private byte[] data;
	private PacketReadState state;

	public byte[] getData() {
		return this.data;
	}

	public PacketReadState getState() {
		return this.state;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setState(PacketReadState state) {
		this.state = state;
	}
}
