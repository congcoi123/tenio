package com.tenio.core.network.security.policy;

public interface IPacketQueuePolicy {
	void applyPolicy(IPacketQueue var1, IPacket var2) throws PacketQueueWarning;
}
