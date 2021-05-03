package com.tenio.core.network.entity.packet;

public interface IPacketQueue {
	IPacket peek();

    IPacket take();

    boolean isEmpty();

    boolean isFull();

    int getSize();

    int getMaxSize();

    void setMaxSize(int var1);

    float getPercentageUsed();

    void clear();

    void put(IPacket var1) throws MessageQueueFullException;

    IPacketQueuePolicy getPacketQueuePolicy();

    void setPacketQueuePolicy(IPacketQueuePolicy var1);
}
