/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.zero.engine.writer.implement;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.session.Session;
import java.io.IOException;
import java.util.Objects;

/**
 * The Datagram writing handler.
 */
public final class DatagramWriterHandler extends AbstractWriterHandler {

  private DatagramWriterHandler() {
  }

  /**
   * Retrieves a new instance of datagram writer handler.
   *
   * @return a new instance of {@link DatagramWriterHandler}
   */
  public static DatagramWriterHandler newInstance() {
    return new DatagramWriterHandler();
  }

  @Override
  public void send(PacketQueue packetQueue, Session session, Packet packet) {
    // the datagram channel will send data by packet, so no fragment using here
    byte[] sendingData = packet.getData();

    // retrieve the datagram channel instance from session
    var datagramChannel = session.getDatagramChannel();

    // the InetSocketAddress should be saved and updated when the datagram channel receive
    // messages from the client
    var remoteSocketAddress = session.getDatagramRemoteSocketAddress();

    // the datagram need to be declared first, something went wrong here, need to
    // log the exception content
    if (Objects.isNull(datagramChannel)) {
      if (isErrorEnabled()) {
        error("{DATAGRAM CHANNEL SEND} ", "UDP Packet cannot be sent to ", session.toString(),
            ", no DatagramChannel was set");
      }
      return;
    } else if (Objects.isNull(remoteSocketAddress)) {
      if (isErrorEnabled()) {
        error("{DATAGRAM CHANNEL SEND} ", "UDP Packet cannot be sent to ", session.toString(),
            ", no InetSocketAddress was set");
      }
      return;
    }

    // clear the buffer first
    getBuffer().clear();

    // send data to the client
    try {
      // buffer size is not enough, need to be allocated more bytes
      if (getBuffer().capacity() < sendingData.length) {
        if (isDebugEnabled()) {
          debug("DATAGRAM CHANNEL SEND", "Allocate new buffer from ", getBuffer().capacity(),
              " to ", sendingData.length, " bytes");
        }
        allocateBuffer(sendingData.length);
      }

      // put data to buffer
      getBuffer().put(sendingData);

      // ready to send
      getBuffer().flip();

      int writtenBytes = datagramChannel.send(getBuffer(), remoteSocketAddress);

        /*
        if (writtenBytes == 0) {
          if (isErrorEnabled()) {
            error("{DATAGRAM CHANNEL SEND} ", "Channel writes 0 byte in session: ", session);
          }
        }
        */

      // update statistic data
      getNetworkWriterStatistic().updateWrittenBytes(writtenBytes);
      getNetworkWriterStatistic().updateWrittenPackets(1);

      // update statistic data for session
      session.addWrittenBytes(writtenBytes);
    } catch (IOException exception) {
      if (isErrorEnabled()) {
        error(exception, "Error occurred in writing on session: ", session.toString());
      }
    }

    // it is always safe to remove the packet from queue hence it should be sent
    packetQueue.take();

    // if the packet queue still contains more packets, then put the session back to
    // the tickets queue
    if (!packetQueue.isEmpty()) {
      getSessionTicketsQueue().add(session);
    }
  }
}
