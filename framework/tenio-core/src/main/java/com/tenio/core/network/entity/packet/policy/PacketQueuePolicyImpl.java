package com.tenio.core.network.entity.packet.policy;

import com.tenio.core.exception.PacketQueuePolicyViolationException;
import com.tenio.core.network.define.MessagePriority;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;

public final class PacketQueuePolicyImpl implements PacketQueuePolicy {

	private static final float THREE_QUARTERS_FULL = 75.0f;
	private static final float NINETY_PERCENT_FULL = 90.0f;

	public static PacketQueuePolicyImpl newInstance() {
		return new PacketQueuePolicyImpl();
	}

	private PacketQueuePolicyImpl() {

	}

	@Override
	public void applyPolicy(PacketQueue packetQueue, Packet packet) {
		float percentageUsed = packetQueue.getPercentageUsed();

		if (percentageUsed >= THREE_QUARTERS_FULL && percentageUsed < NINETY_PERCENT_FULL) {
			if (packet.getPriority().getValue() < MessagePriority.NORMAL.getValue()) {
				__warning(packet, percentageUsed);
			}
		} else if (percentageUsed >= NINETY_PERCENT_FULL) {
			if (packet.getPriority().getValue() < MessagePriority.GUARANTEED.getValue()) {
				__warning(packet, percentageUsed);
			}
		}

	}

	private void __warning(Packet packet, float percentageUsed) {
		throw new PacketQueuePolicyViolationException(String
				.format("Need to drop packet %s, current packet queue usage: %f%%", packet.toString(), percentageUsed));
	}

}
