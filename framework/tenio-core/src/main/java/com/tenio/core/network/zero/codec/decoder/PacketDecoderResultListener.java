package com.tenio.core.network.zero.codec.decoder;

public interface PacketDecoderResultListener {

	void resultFrame(byte[] data);

	void updateDroppedPackets(long numberPackets);

	void updateReadPacketes(long numberPackets);

}
