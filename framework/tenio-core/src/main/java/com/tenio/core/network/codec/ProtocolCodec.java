package com.tenio.core.network.codec;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.protocol.Response;

public interface ProtocolCodec {

	void onPacketRead(Packet var1);

	void onPacketWrite(Response var1);

}
