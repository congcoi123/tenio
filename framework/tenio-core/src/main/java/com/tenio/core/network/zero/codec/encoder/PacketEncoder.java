package com.tenio.core.network.zero.codec.encoder;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.zero.codec.compression.PacketCompressor;
import com.tenio.core.network.zero.codec.encryption.PacketEncrypter;

public interface PacketEncoder {

	Packet encode(Packet packet);

	void setCompressor(PacketCompressor compressor);

	void setEncrypter(PacketEncrypter encrypter);
	
	void setCompressionThresholdBytes(int numberBytes);

}
