package com.tenio.core.network.zero.codec.decoder;

import java.nio.ByteBuffer;

import com.tenio.common.utility.ByteUtility;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.zero.codec.CodecUtility;
import com.tenio.core.network.zero.codec.compression.PacketCompressor;
import com.tenio.core.network.zero.codec.encryption.PacketEncrypter;
import com.tenio.core.network.zero.codec.packet.PacketReadState;
import com.tenio.core.network.zero.codec.packet.ProcessedPacket;

public final class DefaultBinaryPacketDecoder implements PacketDecoder {

	private PacketCompressor __compressor;
	private PacketEncrypter __encrypter;
	private PacketDecoderResultListener __resultListener;

	@Override
	public void decode(Session session, byte[] data) {

		var readState = session.getPacketReadState();

		try {
			while (data.length > 0) {
				var process = session.getProcessedPacket();
				if (readState == PacketReadState.WAIT_NEW_PACKET) {
					process = this.handleNewPacket(session, data);
					readState = process.getPacketReadState();
					data = process.getData();
				}

				if (readState == PacketReadState.WAIT_DATA_SIZE) {
					process = this.handleDataSize(session, data);
					readState = process.getPacketReadState();
					data = process.getData();
				}

				if (readState == PacketReadState.WAIT_DATA_SIZE_FRAGMENT) {
					process = this.handleDataSizeFragment(session, data);
					readState = process.getPacketReadState();
					data = process.getData();
				}

				if (readState == PacketReadState.WAIT_DATA) {
					process = this.handlePacketData(session, data);
					readState = process.getPacketReadState();
					data = process.getData();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			readState = PacketReadState.WAIT_NEW_PACKET;
		}

		session.setPacketReadState(readState);
	}

	@Override
	public void setResultListener(PacketDecoderResultListener resultListener) {
		__resultListener = resultListener;
	}

	@Override
	public void setCompressor(PacketCompressor compressor) {
		__compressor = compressor;
	}

	@Override
	public void setEncrypter(PacketEncrypter encrypter) {
		__encrypter = encrypter;
	}

	private ProcessedPacket handleNewPacket(Session session, byte[] data) {
		var packetHeader = CodecUtility.decodeFirstHeaderByte(data[0]);
		session.getPendingPacket().setPacketHeader(packetHeader);
		data = ByteUtility.resizeBytesArray(data, 1, data.length - 1);

		var processedPacket = session.getProcessedPacket();
		processedPacket.setPacketReadState(PacketReadState.WAIT_DATA_SIZE);
		processedPacket.setData(data);

		return processedPacket;
	}

	private ProcessedPacket handleDataSize(Session session, byte[] data) {
		var packetReadState = PacketReadState.WAIT_DATA;

		var pendingPacket = session.getPendingPacket();
		int dataSize = -1;
		// default header bytes are Short.BYTES, we consider it's big size on the next
		// step
		int headerBytes = Short.BYTES;

		if (pendingPacket.getPacketHeader().isBigSized()) {
			if (data.length >= Integer.BYTES) {
				dataSize = ByteUtility.bytesToInt(data);
			}
			headerBytes = Integer.BYTES;
		} else {
			if (data.length >= Short.BYTES) {
				dataSize = ByteUtility.bytesToShort(data);
			}
		}

		// got data size, can wait to collect packet data bytes
		if (dataSize != -1) {
			// this.validateIncomingDataSize(session, dataSize);
			pendingPacket.setExpectedLength(dataSize);
			// we allocate an enough size of bytes for the buffer to handle packet data
			// later
			pendingPacket.setBuffer(ByteBuffer.allocate(dataSize));
			data = ByteUtility.resizeBytesArray(data, headerBytes, data.length - headerBytes);
		} else {
			// still need to wait to know the length of packet data
			packetReadState = PacketReadState.WAIT_DATA_SIZE_FRAGMENT;
			// put the current data bytes to the pending packet to use later
			var headerBytesBuffer = ByteBuffer.allocate(headerBytes);
			headerBytesBuffer.put(data);
			pendingPacket.setBuffer(headerBytesBuffer);
			// now we should create an empty array to prevent processing later
			// we need to wait until the socket reads more bytes
			data = new byte[0];
		}

		var processedPacket = session.getProcessedPacket();
		processedPacket.setPacketReadState(packetReadState);
		processedPacket.setData(data);

		return processedPacket;
	}

	private ProcessedPacket handleDataSizeFragment(Session session, byte[] data) {
		var packetReadState = PacketReadState.WAIT_DATA_SIZE_FRAGMENT;
		var pendingPacket = session.getPendingPacket();
		var headerBytesBuffer = pendingPacket.getBuffer();

		int remainingBytesNeedForHeader = pendingPacket.getPacketHeader().isBigSized()
				? Integer.BYTES - headerBytesBuffer.position()
				: Short.BYTES - headerBytesBuffer.position();

		// can retrieve left necessary bytes to form a headerBytes
		if (data.length >= remainingBytesNeedForHeader) {
			// put more bytes
			headerBytesBuffer.put(data, 0, remainingBytesNeedForHeader);
			// now can get bytes array from buffer
			headerBytesBuffer.flip();

			// we now have exactly the number of bytes in need, no need to use bit wise
			// method here, just feel free to use utility functions of ByteBuffer
			int dataSize = pendingPacket.getPacketHeader().isBigSized() ? headerBytesBuffer.getInt()
					: headerBytesBuffer.getShort();

			// got data size, can wait to collect packet data bytes
			// this.validateIncomingDataSize(session, dataSize);
			pendingPacket.setExpectedLength(dataSize);
			// we allocate an enough size of bytes for the buffer to handle packet data
			// later
			pendingPacket.setBuffer(ByteBuffer.allocate(dataSize));
			packetReadState = PacketReadState.WAIT_DATA;

			// the left bytes, that no need for the header bytes must be saved for the next
			// process
			if (data.length > remainingBytesNeedForHeader) {
				data = ByteUtility.resizeBytesArray(data, remainingBytesNeedForHeader,
						data.length - remainingBytesNeedForHeader);
			} else {
				data = new byte[0];
			}
		} else {
			// still need to wait more bytes for forming headerBytes
			headerBytesBuffer.put(data);
			// ignore the current data of this method, wait for more bytes come from socket
			data = new byte[0];
		}

		var processedPacket = session.getProcessedPacket();
		processedPacket.setPacketReadState(packetReadState);
		processedPacket.setData(data);

		return processedPacket;
	}

	private ProcessedPacket handlePacketData(Session session, byte[] data) throws Exception {
		var packetReadState = PacketReadState.WAIT_DATA;
		var pendingPacket = session.getPendingPacket();
		var dataBuffer = pendingPacket.getBuffer();

		int inNeedPacketLength = dataBuffer.remaining();
		boolean isThereMore = data.length > inNeedPacketLength;

		// when we receive enough bytes to create packet data
		if (data.length >= inNeedPacketLength) {
			// put bytes data to buffer, from position 0 with the length of packet data
			dataBuffer.put(data, 0, inNeedPacketLength);

			// something went wrong here
			if (pendingPacket.getExpectedLength() != dataBuffer.capacity()) {
				throw new IllegalStateException("Expected data size differs from the buffer capacity! Expected: "
						+ pendingPacket.getExpectedLength() + ", Buffer size: " + dataBuffer.capacity());
			}

			// now the packet data is completely collected, we can do some kind of
			// uncompression or decryption
			// check if data needs to be uncompressed
			if (pendingPacket.getPacketHeader().isCompressed()) {
				byte[] compressedData = dataBuffer.array();
				byte[] decompressedData = __compressor.uncompress(compressedData);
				dataBuffer = ByteBuffer.wrap(decompressedData);
			}

			// check if data needs to be unencrypted
			if (pendingPacket.getPacketHeader().isEncrypted()) {
				byte[] encryptedData = dataBuffer.array();
				byte[] decryptedData = __encrypter.decrypt(encryptedData);
				dataBuffer = ByteBuffer.wrap(decryptedData);
			}

			// result a framed packet data
			__resultListener.resultFrame(session, dataBuffer.array());

			// counting read packets
			__resultListener.updateReadPackets(1);

			// change state for the next process, a new cycle
			packetReadState = PacketReadState.WAIT_NEW_PACKET;

		} else {
			// need to wait more data to generate packet data
			dataBuffer.put(data);
		}

		// in this case, we have more than bytes in need to construct packet data, so
		// save the left bytes for the next step (WAIT_NEW_PACKET)
		// those bytes should be used to produce header bytes for the next packets
		if (isThereMore) {
			data = ByteUtility.resizeBytesArray(data, inNeedPacketLength, data.length - inNeedPacketLength);
		} else {
			// reset data to wait more bytes from socket
			data = new byte[0];
		}

		var processedPacket = session.getProcessedPacket();
		processedPacket.setPacketReadState(packetReadState);
		processedPacket.setData(data);

		return processedPacket;
	}

}