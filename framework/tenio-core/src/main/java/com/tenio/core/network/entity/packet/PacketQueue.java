package com.tenio.core.network.entity.packet;

import com.tenio.core.exception.PacketQueueFullException;
import com.tenio.core.exception.PacketQueuePolicyViolationException;

public interface PacketQueue {

	Packet peek();

	Packet take();

	boolean isEmpty();

	boolean isFull();

	int getSize();

	int getMaxSize();

	float getPercentageUsed();

	void put(Packet packet) throws PacketQueueFullException, PacketQueuePolicyViolationException;

	void clear();
	
}
