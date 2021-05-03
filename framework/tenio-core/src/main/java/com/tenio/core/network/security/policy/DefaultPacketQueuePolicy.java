package com.tenio.core.network.security.policy;

import com.tenio.core.network.define.MessagePriority;
import com.tenio.core.network.entity.packet.IPacket;
import com.tenio.core.network.entity.packet.IPacketQueue;

public class DefaultPacketQueuePolicy implements IPacketQueuePolicy {
    private static final int THREE_QUARTERS_FULL = 75;
    private static final int NINETY_PERCENT_FULL = 90;

    public void applyPolicy(IPacketQueue packetQueue, IPacket packet) throws PacketQueueWarning {
         int percentageFree = packetQueue.getSize() * 100 / packetQueue.getMaxSize();
         if (percentageFree >= 75 && percentageFree < 90) {
              if (packet.getPriority().getValue() < MessagePriority.NORMAL.getValue()) {
                   this.fireDropMessageError(packet, percentageFree, packetQueue.getSize());
              }
         } else if (percentageFree >= 90 && packet.getPriority().getValue() < MessagePriority.HIGH.getValue()) {
              this.fireDropMessageError(packet, percentageFree, packetQueue.getSize());
         }

    }

    private void fireDropMessageError(IPacket packet, int percentageFree, int size) {
         throw new PacketQueueWarning("Dropping packet: " + packet + ", Free queue: " + percentageFree + "%");
    }
}
