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

package com.tenio.core.network.zero.handler.frame;

import com.tenio.common.data.DataCollection;
import com.tenio.common.utility.ByteUtility;
import com.tenio.core.network.codec.CodecUtility;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.codec.packet.PacketHeader;
import com.tenio.core.network.codec.packet.PacketReadState;
import com.tenio.core.network.codec.packet.PendingPacket;
import com.tenio.core.network.codec.packet.ProcessedPacket;
import com.tenio.core.network.entity.session.Session;
import java.nio.ByteBuffer;

/**
 * Streaming packets must be processed in this framing steps.
 *
 * @since 0.6.7
 */
public final class BinaryPacketFramer {

  private BinaryPacketDecoder binaryPacketDecoder;
  private PacketFramingListener packetFramingListener;

  /**
   * Processes streaming binaries data sent from sessions.
   *
   * @param session  the {@link Session} sends data
   * @param binaries the binaries data is being sent. This might not be completed, so the process
   *                 will ensure it chunks or waits for the data to finally provide a full packet
   */
  public void framing(Session session, byte[] binaries) {
    PacketReadState readState = session.getPacketReadState();

    try {
      while (binaries.length > 0) {
        ProcessedPacket processedPacket;
        if (readState == PacketReadState.WAIT_NEW_PACKET) {
          processedPacket = handleNewPacket(session, binaries);
          readState = processedPacket.getPacketReadState();
          binaries = processedPacket.getData();
        }

        if (readState == PacketReadState.WAIT_DATA_SIZE) {
          processedPacket = handleDataSize(session, binaries);
          readState = processedPacket.getPacketReadState();
          binaries = processedPacket.getData();
        }

        if (readState == PacketReadState.WAIT_DATA_SIZE_FRAGMENT) {
          processedPacket = handleDataSizeFragment(session, binaries);
          readState = processedPacket.getPacketReadState();
          binaries = processedPacket.getData();
        }

        if (readState == PacketReadState.WAIT_DATA) {
          processedPacket = handlePacketData(session, binaries);
          readState = processedPacket.getPacketReadState();
          binaries = processedPacket.getData();
        }
      }
    } catch (Exception exception) {
      // swallows the exception as it's expected
      readState = PacketReadState.WAIT_NEW_PACKET;
    }

    session.setPacketReadState(readState);
  }

  /**
   * Retrieves a packet decoder.
   *
   * @return an instance of {@link BinaryPacketDecoder}
   */
  public BinaryPacketDecoder getBinaryPacketDecoder() {
    return binaryPacketDecoder;
  }

  /**
   * Sets the packet decoder.
   *
   * @param packetDecoder an instance of {@link BinaryPacketDecoder}
   */
  public void setBinaryPacketDecoder(BinaryPacketDecoder packetDecoder) {
    this.binaryPacketDecoder = packetDecoder;
  }

  /**
   * Sets the framing result listener.
   *
   * @param packetFramingListener the {@link PacketFramingListener}
   */
  public void setPacketFramingResult(PacketFramingListener packetFramingListener) {
    this.packetFramingListener = packetFramingListener;
  }

  private ProcessedPacket handleNewPacket(Session session, byte[] binaries) {
    PacketHeader packetHeader = CodecUtility.decodeFirstHeaderByte(binaries[0]);
    if (!packetHeader.needsCounting()) {
      throw new IllegalArgumentException("The packet must have data counting attached in the " +
          "header to process");
    }
    session.getPendingPacket().setPacketHeader(packetHeader);
    binaries = ByteUtility.resizeBytesArray(binaries, 1, binaries.length - 1);

    ProcessedPacket processedPacket = session.getProcessedPacket();
    processedPacket.setPacketReadState(PacketReadState.WAIT_DATA_SIZE);
    processedPacket.setData(binaries);

    return processedPacket;
  }

  private ProcessedPacket handleDataSize(Session session, byte[] binaries) {
    PacketReadState packetReadState = PacketReadState.WAIT_DATA;

    PendingPacket pendingPacket = session.getPendingPacket();
    int dataSize = -1;
    // default header bytes are Short.BYTES, we consider it's big size on the next
    // step
    int headerBytes = Short.BYTES;

    if (pendingPacket.getPacketHeader().isBigSized()) {
      if (binaries.length >= Integer.BYTES) {
        dataSize = ByteUtility.bytesToInt(binaries);
      }
      headerBytes = Integer.BYTES;
    } else {
      if (binaries.length >= Short.BYTES) {
        dataSize = ByteUtility.bytesToShort(binaries);
      }
    }

    // got data size, can wait to collect packet data bytes
    if (dataSize != -1) {
      // this.validateIncomingDataSize(session, dataSize);
      pendingPacket.setExpectedLength(dataSize);
      // we allocate an enough size of bytes for the buffer to handle packet data
      // later
      pendingPacket.setBuffer(ByteBuffer.allocate(dataSize));
      binaries = ByteUtility.resizeBytesArray(binaries, headerBytes, binaries.length - headerBytes);
    } else {
      // still need to wait to know the length of packet data
      packetReadState = PacketReadState.WAIT_DATA_SIZE_FRAGMENT;
      // put the current data bytes to the pending packet to use later
      ByteBuffer headerBytesBuffer = ByteBuffer.allocate(headerBytes);
      headerBytesBuffer.put(binaries);
      pendingPacket.setBuffer(headerBytesBuffer);
      // now we should create an empty array to prevent processing later
      // we need to wait until the socket reads more bytes
      binaries = new byte[0];
    }

    ProcessedPacket processedPacket = session.getProcessedPacket();
    processedPacket.setPacketReadState(packetReadState);
    processedPacket.setData(binaries);

    return processedPacket;
  }

  private ProcessedPacket handleDataSizeFragment(Session session, byte[] binaries) {
    PacketReadState packetReadState = PacketReadState.WAIT_DATA_SIZE_FRAGMENT;
    PendingPacket pendingPacket = session.getPendingPacket();
    ByteBuffer headerBytesBuffer = pendingPacket.getBuffer();

    int remainingBytesNeedForHeader = pendingPacket.getPacketHeader().isBigSized()
        ? Integer.BYTES - headerBytesBuffer.position()
        : Short.BYTES - headerBytesBuffer.position();

    // can retrieve left necessary bytes to form a headerBytes
    if (binaries.length >= remainingBytesNeedForHeader) {
      // put more bytes
      headerBytesBuffer.put(binaries, 0, remainingBytesNeedForHeader);
      // now can get bytes array from buffer
      headerBytesBuffer.flip();

      // we now have exactly the number of bytes in need, no need to use bitwise
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
      if (binaries.length > remainingBytesNeedForHeader) {
        binaries = ByteUtility.resizeBytesArray(binaries, remainingBytesNeedForHeader,
            binaries.length - remainingBytesNeedForHeader);
      } else {
        binaries = new byte[0];
      }
    } else {
      // still need to wait more bytes for forming headerBytes
      headerBytesBuffer.put(binaries);
      // ignore the current data of this method, wait for more bytes come from socket
      binaries = new byte[0];
    }

    ProcessedPacket processedPacket = session.getProcessedPacket();
    processedPacket.setPacketReadState(packetReadState);
    processedPacket.setData(binaries);

    return processedPacket;
  }

  private ProcessedPacket handlePacketData(Session session, byte[] binaries) {
    PacketReadState packetReadState = PacketReadState.WAIT_DATA;
    PendingPacket pendingPacket = session.getPendingPacket();
    PacketHeader packetHeader = pendingPacket.getPacketHeader();
    ByteBuffer dataBuffer = pendingPacket.getBuffer();

    int inNeedPacketLength = dataBuffer.remaining();
    boolean isThereMore = binaries.length > inNeedPacketLength;

    // when we receive enough bytes to create packet data
    if (binaries.length >= inNeedPacketLength) {
      // put bytes data to buffer, from position 0 with the length of packet data
      dataBuffer.put(binaries, 0, inNeedPacketLength);

      // something went wrong here
      if (pendingPacket.getExpectedLength() != dataBuffer.capacity()) {
        throw new IllegalStateException(
            "Expected data size differs from the buffer capacity! Expected: "
                + pendingPacket.getExpectedLength() + ", Buffer size: " + dataBuffer.capacity());
      }

      // now the packet data is completely collected
      DataCollection dataCollection = binaryPacketDecoder.decode(packetHeader, binaries);

      // result a framed packet data
      packetFramingListener.onFramedResult(session, dataCollection);

      // change state for the next process, a new cycle
      packetReadState = PacketReadState.WAIT_NEW_PACKET;

    } else {
      // need to wait more data to generate packet data
      dataBuffer.put(binaries);
    }

    // in this case, we have more than bytes in need to construct packet data, so
    // save the left bytes for the next step (WAIT_NEW_PACKET)
    // those bytes should be used to produce header bytes for the next packets
    if (isThereMore) {
      binaries =
          ByteUtility.resizeBytesArray(binaries, inNeedPacketLength,
              binaries.length - inNeedPacketLength);
    } else {
      // reset data to wait more bytes from socket
      binaries = new byte[0];
    }

    ProcessedPacket processedPacket = session.getProcessedPacket();
    processedPacket.setPacketReadState(packetReadState);
    processedPacket.setData(binaries);

    return processedPacket;
  }
}
