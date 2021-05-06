package com.tenio.core.network.entity.packet;

import com.tenio.core.exception.PacketQueueFullException;
import com.tenio.core.exception.PacketQueuePolicyViolationException;
import com.tenio.core.network.define.MessagePriority;
import com.tenio.core.network.entity.packet.implement.PacketImpl;
import com.tenio.core.network.entity.packet.implement.PacketQueueImpl;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicyImpl;

public final class TestPackets {

	public static void main(String[] args) throws PacketQueuePolicyViolationException, PacketQueueFullException {
		
		Packet packet1 = PacketImpl.newInstance();
		packet1.setPriority(MessagePriority.NON_GUARANTEED);
		
		Packet packet2 = PacketImpl.newInstance();
		packet2.setPriority(MessagePriority.GUARANTEED_QUICKEST);
		
		Packet packet3 = PacketImpl.newInstance();
		packet3.setPriority(MessagePriority.NORMAL);
		
		Packet packet4 = PacketImpl.newInstance();
		packet4.setPriority(MessagePriority.GUARANTEED);
		
		Packet packet5 = PacketImpl.newInstance();
		packet5.setPriority(MessagePriority.NON_GUARANTEED);
		
		Packet packet6 = PacketImpl.newInstance();
		packet6.setPriority(MessagePriority.NORMAL);
		
		PacketQueuePolicy policy = PacketQueuePolicyImpl.newInstance();
		
		PacketQueue queue = PacketQueueImpl.newInstance(policy, 10);
		queue.put(packet1);
		queue.put(packet2);
		queue.put(packet3);
		queue.put(packet4);
		queue.put(packet5);
		
		System.out.println(queue);
		
		System.err.println(queue.peek());
		
		System.out.println(queue);
		
		System.err.println(queue.take());
		
		System.out.println(queue);
		
		queue.put(packet6);
		
		System.out.println(queue);
		
		System.err.println(queue.take());
		
		System.out.println(queue);
		
	}

}
