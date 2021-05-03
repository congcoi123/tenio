package com.tenio.core.network.entity.packet;

import java.util.LinkedList;
import java.util.TreeSet;

import com.tenio.core.network.security.policy.IPacketQueuePolicy;

public final class NonBlockingPacketQueue implements IPacketQueue {
    private final TreeSet<IPacket> queue = new TreeSet<IPacket>();
    private int maxSize;
    private IPacketQueuePolicy packetQueuePolicy;

    public NonBlockingPacketQueue(int maxSize) {
         this.maxSize = maxSize;
    }

    public void clear() {
         synchronized(this.queue) {
              this.queue.clear();
         }
    }

    public int getSize() {
         return this.queue.size();
    }

    public int getMaxSize() {
         return this.maxSize;
    }

    public boolean isEmpty() {
         return this.queue.size() == 0;
    }

    public boolean isFull() {
         return this.queue.size() >= this.maxSize;
    }

    public float getPercentageUsed() {
         return this.maxSize == 0 ? 0.0F : (float)(this.queue.size() * 100) / (float)this.maxSize;
    }

    public IPacket peek() {
         IPacket packet = null;
         synchronized(this.queue) {
              if (!this.isEmpty()) {
                   packet = (IPacket)this.queue.last();
              }

              return packet;
         }
    }

    public void put(IPacket packet) throws MessageQueueFullException, PacketQueueWarning {
         if (this.isFull()) {
              throw new MessageQueueFullException();
         } else {
              this.packetQueuePolicy.applyPolicy(this, packet);
              synchronized(this.queue) {
                   this.queue.add(packet);
              }
         }
    }

    public void setMaxSize(int size) {
         this.maxSize = size;
    }

    public IPacket take() {
         IPacket packet = null;
         synchronized(this.queue) {
              packet = (IPacket)this.queue.last();
              this.queue.remove(packet);
              return packet;
         }
    }

    public IPacketQueuePolicy getPacketQueuePolicy() {
         return this.packetQueuePolicy;
    }

    public void setPacketQueuePolicy(IPacketQueuePolicy packetQueuePolicy) {
         this.packetQueuePolicy = packetQueuePolicy;
    }
}
