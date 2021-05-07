package com.tenio.core.network.zero.codec;

import com.tenio.core.network.entity.packet.Packet;

public interface PacketEncoder {

	Packet encode(Packet packet);

}
