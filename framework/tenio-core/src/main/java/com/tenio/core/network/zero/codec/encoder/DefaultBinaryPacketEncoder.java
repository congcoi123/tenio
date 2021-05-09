package com.tenio.core.network.zero.codec.encoder;

import java.nio.ByteBuffer;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.zero.codec.CodecUtility;
import com.tenio.core.network.zero.codec.compression.PacketCompressor;
import com.tenio.core.network.zero.codec.encryption.PacketEncrypter;
import com.tenio.core.network.zero.codec.packet.PacketHeader;

public final class DefaultBinaryPacketEncoder implements PacketEncoder {

	private static final int DEFAULT_COMPRESSION_THRESHOLD_BYTES = 3000;
	private static final int MAX_BYTES_FOR_NORMAL_SIZE = Short.MAX_VALUE * 2 + 1;

	private PacketCompressor __compressor;
	private PacketEncrypter __encrypter;
	private int __compressionThresholdBytes;

	public DefaultBinaryPacketEncoder() {
		__compressionThresholdBytes = DEFAULT_COMPRESSION_THRESHOLD_BYTES;
	}

	@Override
	public Packet encode(Packet packet) {
		// retrieve the packet data first
		byte[] binary = packet.getData();

		// check if the data needs to be encrypted
		boolean isEncrypted = packet.isEncrypted();
		if (isEncrypted) {
			try {
				binary = __encrypter.encrypt(binary);
				isEncrypted = true;
			} catch (Exception e) {
				isEncrypted = false;
			}
		}

		// check if the data needs to be compressed
		boolean isCompressed = false;
		if (binary.length > __compressionThresholdBytes) {
			try {
				binary = __compressor.compress(binary);
				isCompressed = true;
			} catch (Exception e) {
				isCompressed = false;
			}
		}

		// if the original size of data exceeded threshold, it needs to be resize the
		// header bytes value
		int headerSize = Short.BYTES;
		if (binary.length > MAX_BYTES_FOR_NORMAL_SIZE) {
			headerSize = Integer.BYTES;
		}

		// create new packet header and encode the first indicated byte
		var packetHeader = PacketHeader.newInstance(true, isCompressed, headerSize > Short.BYTES, isEncrypted);
		byte headerByte = CodecUtility.encodeFirstHeaderByte(packetHeader);

		// allocate bytes for the new data and put all value to form a new packet
		var packetBuffer = ByteBuffer.allocate(Byte.BYTES + headerSize + binary.length);

		// put header byte indicator
		packetBuffer.put(headerByte);

		// put original data size for header bases on its length
		if (headerSize > Short.BYTES) {
			packetBuffer.putInt(binary.length);
		} else {
			packetBuffer.putShort((short) binary.length);
		}

		// put original data
		packetBuffer.put(binary);

		// form new data for the packet
		packet.setData(packetBuffer.array());

		return packet;
	}

	@Override
	public void setCompressor(PacketCompressor compressor) {
		__compressor = compressor;
	}

	@Override
	public void setEncrypter(PacketEncrypter encrypter) {
		__encrypter = encrypter;
	}

	@Override
	public void setCompressionThresholdBytes(int numberBytes) {
		__compressionThresholdBytes = numberBytes;
	}

}
