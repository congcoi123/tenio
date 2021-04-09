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
package com.tenio.core.message.packet;

import java.util.LinkedList;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.core.exception.MessagePacketQueueFullException;

/**
 * UNDER CONSTRUCTION
 * 
 * @author kong
 */
@ThreadSafe
public final class DefaultPacketQueue implements IPacketQueue {

	private final LinkedList<byte[]> __packetQueue;
	private volatile int __size;

	public DefaultPacketQueue(int size) {
		__size = size;
		__packetQueue = new LinkedList<byte[]>();
	}

	@Override
	public byte[] peek() {
		synchronized (__packetQueue) {
			if (!__packetQueue.isEmpty()) {
				return __packetQueue.getFirst();
			}
			return null;
		}
	}

	@Override
	public byte[] take() {
		synchronized (__packetQueue) {
			if (!__packetQueue.isEmpty()) {
				return __packetQueue.removeFirst();
			}
			return null;
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized (__packetQueue) {
			return __packetQueue.isEmpty();
		}
	}

	@Override
	public boolean isFull() {
		synchronized (__packetQueue) {
			return __packetQueue.size() >= __size;
		}
	}

	@Override
	public int count() {
		synchronized (__packetQueue) {
			return __packetQueue.size();
		}
	}

	@Override
	public int getSize() {
		return __size;
	}

	@Override
	public void setSize(int size) {
		__size = size;
	}

	@Override
	public void put(byte[] packet) throws MessagePacketQueueFullException {
		synchronized (__packetQueue) {
			if (__packetQueue.size() >= __size) {
				throw new MessagePacketQueueFullException();
			} else {
				__packetQueue.addLast(packet);
			}
		}
	}

	@Override
	public float getPercentageUsed() {
		synchronized (__packetQueue) {
			return __size == 0 ? 0.0F : (float) (__packetQueue.size() * 100) / (float) __size;
		}
	}

	@Override
	public void clear() {
		synchronized (__packetQueue) {
			__packetQueue.clear();
		}
	}

}
