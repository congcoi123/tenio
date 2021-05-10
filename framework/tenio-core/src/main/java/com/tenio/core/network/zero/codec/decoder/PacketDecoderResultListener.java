package com.tenio.core.network.zero.codec.decoder;

import com.tenio.core.network.entity.session.Session;

public interface PacketDecoderResultListener {

	void resultFrame(Session session, byte[] data);

	void updateDroppedPackets(long numberPackets);

	void updateReadPackets(long numberPackets);

}
