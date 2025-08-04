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

package com.tenio.core.network.codec.decoder;

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.DataType;
import com.tenio.common.data.DataUtility;
import com.tenio.common.utility.ByteUtility;
import com.tenio.core.network.codec.CodecUtility;
import com.tenio.core.network.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.codec.encryption.BinaryPacketEncryptor;
import com.tenio.core.network.codec.packet.PacketHeader;

/**
 * The default implementation for the binary packet decoding.
 */
public final class BinaryPacketDecoderImpl implements BinaryPacketDecoder {

  private BinaryPacketCompressor compressor;
  private BinaryPacketEncryptor encryptor;

  @Override
  public DataCollection decode(byte[] binaries) throws RuntimeException {
    var packetHeader = CodecUtility.decodeFirstHeaderByte(binaries[0]);
    binaries = ByteUtility.resizeBytesArray(binaries, 1, binaries.length - 1);

    return decode(packetHeader, binaries);
  }

  @Override
  public DataCollection decode(PacketHeader packetHeader, byte[] binaries) throws RuntimeException {
    if (binaries == null || binaries.length == 0) {
      return null;
    }

    // Order: uncompression -> decryption (It must be reversed in Encoder)
    // 1. checks if data needs to be uncompressed
    if (packetHeader.isCompressed()) {
      if (compressor != null) {
        binaries = compressor.uncompress(binaries);
      } else {
        throw new IllegalStateException("Expected the interface BinaryPacketCompressor was " +
            "implemented due to the packet-compression-threshold-bytes configuration, but it is" +
            " null");
      }
    }

    // 2. checks if data needs to be unencrypted
    if (packetHeader.isEncrypted()) {
      if (encryptor != null) {
        binaries = encryptor.decrypt(binaries);
      } else {
        throw new IllegalStateException("Expected the interface BinaryPacketEncryptor was " +
            "implemented, but it is null");
      }
    }

    // gets the data type
    DataType dataType = DataType.ZERO;
    if (packetHeader.isMsgpack()) {
      dataType = DataType.MSG_PACK;
    }

    return DataUtility.binariesToCollection(dataType, binaries);
  }

  @Override
  public void setCompressor(BinaryPacketCompressor compressor) {
    this.compressor = compressor;
  }

  @Override
  public void setEncryptor(BinaryPacketEncryptor encryptor) {
    this.encryptor = encryptor;
  }
}
