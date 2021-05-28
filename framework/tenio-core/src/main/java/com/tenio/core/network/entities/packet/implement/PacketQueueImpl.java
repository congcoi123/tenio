/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.core.network.entities.packet.implement;

import java.util.TreeSet;

import com.tenio.core.exceptions.PacketQueueFullException;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.packet.PacketQueue;
import com.tenio.core.network.entities.packet.policy.PacketQueuePolicy;

public final class PacketQueueImpl implements PacketQueue {

	private final TreeSet<Packet> __queue;
	private PacketQueuePolicy __packetQueuePolicy;
	private int __maxSize;
	private volatile int __size;

	public static PacketQueueImpl newInstance() {
		return new PacketQueueImpl();
	}

	private PacketQueueImpl() {
		__queue = new TreeSet<Packet>();
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
	public void setMaxSize(int maxSize) {
		__maxSize = maxSize;
	}

	@Override
	public void setPacketQueuePolicy(PacketQueuePolicy packetQueuePolicy) {
		__packetQueuePolicy = packetQueuePolicy;
	}

	@Override
	public float getPercentageUsed() {
		return __maxSize == 0 ? 0.0f : (float) (__size * 100) / (float) __maxSize;
	}

	@Override
	public void put(Packet packet) {
		if (isFull()) {
			throw new PacketQueueFullException(__queue.size());
		}
		__packetQueuePolicy.applyPolicy(this, packet);
		synchronized (__queue) {
			__queue.add(packet);
			__size = __queue.size();
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
