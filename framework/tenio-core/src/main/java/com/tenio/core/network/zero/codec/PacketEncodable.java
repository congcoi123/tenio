package com.tenio.core.network.zero.codec;

import com.tenio.core.network.entity.packet.Packet;

public interface PacketEncodable {

	Packet handle(Packet packet);

}
