package com.tenio.core.network.entity.packet.policy;

import com.tenio.core.exception.PacketQueuePolicyViolationException;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;

public interface PacketQueuePolicy {

	void applyPolicy(PacketQueue packetQueue, Packet packet) throws PacketQueuePolicyViolationException;

}
