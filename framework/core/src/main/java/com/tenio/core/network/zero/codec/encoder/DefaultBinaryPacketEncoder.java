/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.zero.codec.encoder;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.zero.codec.CodecUtility;
import com.tenio.core.network.zero.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.zero.codec.encryption.BinaryPacketEncrypter;
import com.tenio.core.network.zero.codec.packet.PacketHeader;
import java.nio.ByteBuffer;

/**
 * The default implementation for the binary packet encoding.
 *
 * @see BinaryPacketEncoder
 */
public final class DefaultBinaryPacketEncoder implements BinaryPacketEncoder {

  private static final int DEFAULT_COMPRESSION_THRESHOLD_BYTES = 3000;
  private static final int MAX_BYTES_FOR_NORMAL_SIZE = Short.MAX_VALUE * 2 + 1;

  private BinaryPacketCompressor compressor;
  private BinaryPacketEncrypter encrypter;
  private int compressionThresholdBytes;

  public DefaultBinaryPacketEncoder() {
    compressionThresholdBytes = DEFAULT_COMPRESSION_THRESHOLD_BYTES;
  }

  @Override
  public Packet encode(Packet packet) {
    // retrieve the packet data first
    byte[] binary = packet.getData();

    // check if the data needs to be encrypted
    boolean isEncrypted = packet.isEncrypted();
    if (isEncrypted) {
      try {
        binary = encrypter.encrypt(binary);
        isEncrypted = true;
      } catch (Exception e) {
        isEncrypted = false;
      }
    }

    // check if the data needs to be compressed
    boolean isCompressed = false;
    if (binary.length > compressionThresholdBytes) {
      try {
        binary = compressor.compress(binary);
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
    var packetHeader =
        PacketHeader.newInstance(true, isCompressed, headerSize > Short.BYTES, isEncrypted);
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
  public void setCompressor(BinaryPacketCompressor compressor) {
    this.compressor = compressor;
  }

  @Override
  public void setEncrypter(BinaryPacketEncrypter encrypter) {
    this.encrypter = encrypter;
  }

  @Override
  public void setCompressionThresholdBytes(int numberBytes) {
    compressionThresholdBytes = numberBytes;
  }
}
