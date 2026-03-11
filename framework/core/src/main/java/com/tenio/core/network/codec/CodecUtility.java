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

package com.tenio.core.network.codec;

import com.tenio.common.data.DataType;
import com.tenio.core.network.codec.packet.PacketHeader;
import com.tenio.core.network.codec.packet.PacketHeaderType;

/**
 * The utility class provides methods to work with packet and binary data.
 */
public final class CodecUtility {

  /**
   * The last 2 bits are reserved to encode {@link DataType} instance.
   */
  private static final byte DATA_TYPE_MASK = 0b00000011;

  private CodecUtility() {
    throw new UnsupportedOperationException("This class does not support to create new instance");
  }

  /**
   * Decoding the header byte to find the packet header setting.
   *
   * @param headerByte the first read {@code byte}
   * @return the new instance of {@link PacketHeader}
   */
  public static PacketHeader decodeFirstHeaderByte(byte headerByte) {
    // The mask will ensure we are only using the last 2 bits for DataType (Which is expected to
    // have values from 0 to 3). In the future, if there are changes, for example, new DataType
    // (5) is introduced:
    // 00000101
    // AND
    // 00000011
    //--------
    // 00000001
    // With masking, it won't break the header byte layout
    byte dataTypeValue = (byte) (headerByte & DATA_TYPE_MASK);
    DataType dataType = DataType.getByValue(dataTypeValue);
    if (dataType == null) {
      throw new IllegalArgumentException("Unsupported data type value in header: " + dataTypeValue);
    }
    return PacketHeader.newInstance(
        (headerByte & PacketHeaderType.LENGTH_PREFIXED.getValue()) != 0,
        (headerByte & PacketHeaderType.COMPRESSION.getValue()) != 0,
        (headerByte & PacketHeaderType.BIG_SIZE.getValue()) != 0,
        (headerByte & PacketHeaderType.ENCRYPTION.getValue()) != 0,
        dataType
    );
  }

  /**
   * Encoding the packet header setting to a byte value.
   *
   * @param packetHeader the packet header instance
   * @return the encoded packet header in byte value
   */
  public static byte encodeFirstHeaderByte(PacketHeader packetHeader) {
    byte headerByte = 0;

    if (packetHeader.hasLengthPrefixed()) {
      headerByte |= PacketHeaderType.LENGTH_PREFIXED.getValue();
    }

    if (packetHeader.isCompressed()) {
      headerByte |= PacketHeaderType.COMPRESSION.getValue();
    }

    if (packetHeader.isBigSized()) {
      headerByte |= PacketHeaderType.BIG_SIZE.getValue();
    }

    if (packetHeader.isEncrypted()) {
      headerByte |= PacketHeaderType.ENCRYPTION.getValue();
    }

    // DataType encoding
    // The mask will ensure we are only using the last 2 bits for DataType (Which is expected to
    // have values from 0 to 3). In the future, if there are changes, for example, new DataType
    // (5) is introduced:
    // 00000101
    // AND
    // 00000011
    //--------
    // 00000001
    // With masking, it won't break the header byte layout
    headerByte |= (byte) (packetHeader.getDataType().getValue() & DATA_TYPE_MASK);


    return headerByte;
  }
}
