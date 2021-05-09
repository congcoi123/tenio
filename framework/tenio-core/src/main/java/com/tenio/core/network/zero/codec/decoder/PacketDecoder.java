package com.tenio.core.network.zero.codec.decoder;

import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.zero.codec.compression.PacketCompressor;
import com.tenio.core.network.zero.codec.encryption.PacketEncrypter;

public interface PacketDecoder {

	void decode(Session session, byte[] data);

	void setResultListener(PacketDecoderResultListener resultListener);
	
	void setCompressor(PacketCompressor compressor);

	void setEncrypter(PacketEncrypter encrypter);

}
