package com.tenio.core.exceptions;

import com.tenio.core.network.entities.packet.Packet;

public final class PacketQueuePolicyViolationException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1620230030870946508L;

	public PacketQueuePolicyViolationException(Packet packet, float percentageUsed) {
		super(String.format("Dropped packet: [%s], current packet queue usage: %f%%", packet.toString(),
				percentageUsed));
	}

}
