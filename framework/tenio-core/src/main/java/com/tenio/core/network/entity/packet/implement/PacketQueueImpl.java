package com.tenio.core.network.entity.packet.implement;

import java.util.TreeSet;

import com.tenio.core.exception.PacketQueueFullException;
import com.tenio.core.exception.PacketQueuePolicyViolationException;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;

public final class PacketQueueImpl implements PacketQueue {

	private final TreeSet<Packet> __queue;
	private final int __maxSize;
	private final PacketQueuePolicy __packetQueuePolicy;
	private volatile int __size;

	public static PacketQueueImpl newInstance(PacketQueuePolicy packetQueuePolicy, int maxSize) {
		return new PacketQueueImpl(packetQueuePolicy, maxSize);
	}
	
	private PacketQueueImpl(PacketQueuePolicy packetQueuePolicy, int maxSize) {
		__queue = new TreeSet<Packet>();
		__packetQueuePolicy = packetQueuePolicy;
		__maxSize = maxSize;
	}

	@Override
	public Packet peek() {
		synchronized (__queue) {
			if (!isEmpty()) {
				return __queue.last();
			}
		}

		return null;
	}

	@Override
	public Packet take() {
		synchronized (__queue) {
			if (!isEmpty()) {
				var packet = __queue.last();
				__queue.remove(packet);
				__size = __queue.size();
				return packet;
			}
		}

		return null;
	}

	@Override
	public boolean isEmpty() {
		return __size == 0;
	}

	@Override
	public boolean isFull() {
		return __size >= __maxSize;
	}

	@Override
	public int getSize() {
		return __size;
	}

	@Override
	public int getMaxSize() {
		return __maxSize;
	}

	@Override
	public float getPercentageUsed() {
		return __maxSize == 0 ? 0.0f : (float) (__size * 100) / (float) __maxSize;
	}

	@Override
	public void put(Packet packet) throws PacketQueueFullException, PacketQueuePolicyViolationException {
		if (isFull()) {
			throw new PacketQueueFullException();
		} else {
			__packetQueuePolicy.applyPolicy(this, packet);
			synchronized (__queue) {
				__queue.add(packet);
				__size = __queue.size();
			}
		}
	}

	@Override
	public void clear() {
		synchronized (__queue) {
			__queue.clear();
			__size = __queue.size();
		}
	}

	@Override
	public String toString() {
		return __queue.toString();
	}

}
