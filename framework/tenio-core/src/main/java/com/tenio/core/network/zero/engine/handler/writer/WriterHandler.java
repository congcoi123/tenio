package com.tenio.core.network.zero.engine.handler.writer;

import com.tenio.core.network.entity.connection.Session;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;

public interface WriterHandler {

	void send(PacketQueue packetQueue, Session session, Packet packet) throws Exception;

}
