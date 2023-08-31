/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.zero.codec.decoder;

import com.tenio.common.utility.ByteUtility;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.zero.codec.CodecUtility;
import com.tenio.core.network.zero.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.zero.codec.encryption.BinaryPacketEncryptor;
import com.tenio.core.network.zero.codec.packet.PacketReadState;
import com.tenio.core.network.zero.codec.packet.ProcessedPacket;
import java.nio.ByteBuffer;

/**
 * The default implementation for the binary packet decoding.
 */
public final class DefaultBinaryPacketDecoder implements BinaryPacketDecoder {

  private BinaryPacketCompressor compressor;
  private BinaryPacketEncryptor encryptor;
  private PacketDecoderResultListener decoderResultListener;

  @Override
  public void decode(Session session, byte[] data) {

    var readState = session.getPacketReadState();

    try {
      while (data.length > 0) {
        var processedPacket = session.getProcessedPacket();
        if (readState == PacketReadState.WAIT_NEW_PACKET) {
          processedPacket = handleNewPacket(session, data);
          readState = processedPacket.getPacketReadState();
          data = processedPacket.getData();
        }

        if (readState == PacketReadState.WAIT_DATA_SIZE) {
          processedPacket = handleDataSize(session, data);
          readState = processedPacket.getPacketReadState();
          data = processedPacket.getData();
        }

        if (readState == PacketReadState.WAIT_DATA_SIZE_FRAGMENT) {
          processedPacket = handleDataSizeFragment(session, data);
          readState = processedPacket.getPacketReadState();
          data = processedPacket.getData();
        }

        if (readState == PacketReadState.WAIT_DATA) {
          processedPacket = handlePacketData(session, data);
          readState = processedPacket.getPacketReadState();
          data = processedPacket.getData();
        }
      }
    } catch (Exception exception) {
      exception.printStackTrace();
      readState = PacketReadState.WAIT_NEW_PACKET;
    }

    session.setPacketReadState(readState);
  }

  @Override
  public void setResultListener(PacketDecoderResultListener resultListener) {
    decoderResultListener = resultListener;
  }

  @Override
  public void setCompressor(BinaryPacketCompressor compressor) {
    this.compressor = compressor;
  }

  @Override
  public void setEncryptor(BinaryPacketEncryptor encryptor) {
    this.encryptor = encryptor;
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

  private ProcessedPacket handlePacketData(Session session, byte[] data) {
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
        throw new IllegalStateException(
            "Expected data size differs from the buffer capacity! Expected: "
                + pendingPacket.getExpectedLength() + ", Buffer size: " + dataBuffer.capacity());
      }

      // now the packet data is completely collected, we can do some kind of
      // decompression or decryption
      // check if data needs to be uncompressed
      if (pendingPacket.getPacketHeader().isCompressed()) {
        byte[] compressedData = dataBuffer.array();
        byte[] decompressedData = compressor.uncompress(compressedData);
        dataBuffer = ByteBuffer.wrap(decompressedData);
      }

      // check if data needs to be unencrypted
      if (pendingPacket.getPacketHeader().isEncrypted()) {
        byte[] encryptedData = dataBuffer.array();
        byte[] decryptedData = encryptor.decrypt(encryptedData);
        dataBuffer = ByteBuffer.wrap(decryptedData);
      }

      byte[] result = dataBuffer.array();

      // result a framed packet data
      decoderResultListener.resultFrame(session, result);

      // counting read packets
      decoderResultListener.updateReadPackets(1);

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
      data =
          ByteUtility.resizeBytesArray(data, inNeedPacketLength, data.length - inNeedPacketLength);
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
