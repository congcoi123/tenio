/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.examples.client;

import com.tenio.common.data.DataCollection;
import com.tenio.core.network.codec.CodecUtility;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoderImpl;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoderImpl;
import com.tenio.core.network.entity.outbound.packet.implement.PacketImpl;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

/** Shared wire codec. Each transport owns its encoder and decoder. */
final class ClientCodec {

  static final int MAX_TCP_PAYLOAD = 4 * 1024 * 1024;

  private final BinaryPacketEncoderImpl encoder = new BinaryPacketEncoderImpl();
  private final BinaryPacketDecoderImpl decoder = new BinaryPacketDecoderImpl();

  ClientCodec() {
    // Separate codec instances: compression/encryption implementations may hold mutable state.
    encoder.setCompressor(new DefaultBinaryPacketCompressor());
    encoder.setEncryptor(new DefaultBinaryPacketEncryptor());
    decoder.setCompressor(new DefaultBinaryPacketCompressor());
    decoder.setEncryptor(new DefaultBinaryPacketEncryptor());
  }

  byte[] encode(DataCollection message, boolean stream) {
    var packet = PacketImpl.newInstance();
    packet.setDataType(message.getType());
    packet.setData(message.toBinaries());
    packet.hasLengthPrefixed(stream);
    if (stream && packet.getData().length > MAX_TCP_PAYLOAD) {
      throw new IllegalArgumentException("TCP payload exceeds " + MAX_TCP_PAYLOAD);
    }
    return encoder.encode(packet).getData();
  }

  DataCollection read(DataInputStream input) throws IOException {
    int indicator = input.read();
    if (indicator == -1) {
      return null;
    }
    var header = CodecUtility.decodeFirstHeaderByte((byte) indicator);
    if (!header.hasLengthPrefixed()) {
      throw new IOException("TCP packet must be length-prefixed");
    }
    int size = header.isBigSized() ? input.readInt() : input.readUnsignedShort();
    if (size <= 0 || size > MAX_TCP_PAYLOAD) {
      throw new IOException("Invalid TCP payload length: " + size);
    }
    byte[] payload = new byte[size];
    input.readFully(payload); // Handles fragmented headers/bodies and coalesced TCP packets.
    var message = decoder.decode(header, payload);
    if (message == null) {
      throw new EOFException("Empty decoded TCP packet");
    }
    return message;
  }

  DataCollection decode(byte[] data) {
    if (data.length == 0 || CodecUtility.decodeFirstHeaderByte(data[0]).hasLengthPrefixed()) {
      throw new IllegalArgumentException("Expected an unprefixed UDP packet");
    }
    return decoder.decode(data);
  }
}
