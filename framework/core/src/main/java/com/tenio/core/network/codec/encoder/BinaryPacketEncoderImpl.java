/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.codec.encoder;

import com.tenio.common.data.DataType;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.network.codec.CodecUtility;
import com.tenio.core.network.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.codec.encryption.BinaryPacketEncryptor;
import com.tenio.core.network.codec.packet.PacketHeader;
import com.tenio.core.network.entity.packet.Packet;
import java.nio.ByteBuffer;

/**
 * The default implementation for the binary packet encoding.
 *
 * @see BinaryPacketEncoder
 */
public final class BinaryPacketEncoderImpl extends SystemLogger implements BinaryPacketEncoder {

  private BinaryPacketCompressor compressor;
  private BinaryPacketEncryptor encryptor;
  private int compressionThresholdBytes;

  /**
   * Initialization.
   */
  public BinaryPacketEncoderImpl() {
    compressionThresholdBytes = DEFAULT_COMPRESSION_THRESHOLD_BYTES;
  }

  @Override
  public Packet encode(Packet packet) {
    // retrieve the packet original data first
    byte[] binaries = packet.getData();
    if (binaries == null || binaries.length == 0) {
      throw new IllegalArgumentException("Th packet has empty data to encode");
    }

    // Order: encryption -> compression (It must be reversed in Decoder)
    // 1. check if the data needs to be encrypted
    boolean needsEncrypted = packet.needsEncrypted();
    if (needsEncrypted) {
      if (encryptor != null) {
        try {
          binaries = encryptor.encrypt(binaries);
        } catch (Exception exception) {
          error(exception);
          needsEncrypted = false;
        }
      } else {
        throw new IllegalStateException("Expected the interface BinaryPacketEncryptor was " +
            "implemented, but it is null");
      }
    }

    // 2. check if the data needs to be compressed
    boolean needsCompressed = false;
    if (compressionThresholdBytes > 0 && binaries.length >= compressionThresholdBytes) {
      if (compressor != null) {
        try {
          binaries = compressor.compress(binaries);
          needsCompressed = true;
        } catch (Exception exception) {
          error(exception);
        }
      } else {
        throw new IllegalStateException("Expected the interface BinaryPacketCompressor was " +
            "implemented due to the packet-compression-threshold-bytes configuration, but it is" +
            " null");
      }
    }

    // in default, there is no header size
    int headerSize = 0;
    // in case of stream-oriented type
    if (packet.needsDataCounting()) {
      // if the original size of data exceeded threshold, it needs to be resized the
      // header bytes value
      headerSize = Short.BYTES;
      if (binaries.length > MAX_BYTES_FOR_NORMAL_SIZE) {
        headerSize = Integer.BYTES;
      }
    }

    // create new packet header and encode the first indicated byte
    PacketHeader packetHeader =
        PacketHeader.newInstance(packet.needsDataCounting(), needsCompressed,
            headerSize > Short.BYTES, needsEncrypted, packet.getDataType() == DataType.ZERO,
            packet.getDataType() == DataType.MSG_PACK);
    byte headerByte = CodecUtility.encodeFirstHeaderByte(packetHeader);

    // allocate bytes for the new data and put all value to form a new packet
    var packetBuffer = ByteBuffer.allocate(Byte.BYTES + headerSize + binaries.length);

    // 1. put header byte indicator
    packetBuffer.put(headerByte);

    // 2. put original data size for header bases on its length (in case of stream-oriented type)
    if (packetHeader.needsCounting()) {
      if (headerSize > Short.BYTES) {
        packetBuffer.putInt(binaries.length);
      } else {
        packetBuffer.putShort((short) binaries.length);
      }
    }

    // 3. put original data
    packetBuffer.put(binaries);

    // form new data for the packet
    packet.setData(packetBuffer.array());

    return packet;
  }

  @Override
  public void setCompressor(BinaryPacketCompressor compressor) {
    this.compressor = compressor;
  }

  @Override
  public void setEncryptor(BinaryPacketEncryptor encryptor) {
    this.encryptor = encryptor;
  }

  @Override
  public void setCompressionThresholdBytes(int numberBytes) {
    compressionThresholdBytes = numberBytes;
  }
}
