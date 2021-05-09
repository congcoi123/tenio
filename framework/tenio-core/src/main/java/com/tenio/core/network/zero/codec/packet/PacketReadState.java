package com.tenio.core.network.zero.codec.packet;

public enum PacketReadState {
	WAIT_NEW_PACKET, WAIT_DATA_SIZE, WAIT_DATA_SIZE_FRAGMENT, WAIT_DATA;
}
