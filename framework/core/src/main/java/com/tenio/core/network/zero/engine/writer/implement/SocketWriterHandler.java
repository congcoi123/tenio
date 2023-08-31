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

package com.tenio.core.network.zero.engine.writer.implement;

import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.session.Session;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Objects;

/**
 * The Socket writing handler.
 */
public final class SocketWriterHandler extends AbstractWriterHandler {

  private SocketWriterHandler() {
  }

  /**
   * Creates a new instance of the socket writer handler.
   *
   * @return a new instance of {@link SocketWriterHandler}
   */
  public static SocketWriterHandler newInstance() {
    return new SocketWriterHandler();
  }

  @Override
  public void send(PacketQueue packetQueue, Session session, Packet packet) {
    var channel = session.getSocketChannel();

    // this channel can be deactivated by some reasons, no need to throw an
    // exception here
    if (Objects.isNull(channel)) {
      if (isDebugEnabled()) {
        debug("SOCKET CHANNEL SEND", "Skipping this packet, found null socket for session ",
            session);
      }
      return;
    }

    // clear the buffer first
    getBuffer().clear();

    // set priority for packet left unsent data (fragment)
    byte[] sendingData = packet.isFragmented() ? packet.getFragmentBuffer() : packet.getData();

    // buffer size is not enough, need to be allocated more bytes
    if (getBuffer().capacity() < sendingData.length) {
      if (isDebugEnabled()) {
        debug("SOCKET CHANNEL SEND", "Allocate new buffer from ", getBuffer().capacity(), " to ",
            sendingData.length, " bytes");
      }
      allocateBuffer(sendingData.length);
    }

    // start to read data to buffer
    getBuffer().put(sendingData);

    // ready to write on socket
    getBuffer().flip();

    // expect to write all data in buffer
    int expectedWritingBytes = getBuffer().remaining();

    // but it's up to the channel, so it's possible to get left unsent bytes
    try {
      int realWrittenBytes = channel.write(getBuffer());

      /*
      if (realWrittenBytes == 0) {
        if (isErrorEnabled()) {
          error("{SOCKET CHANNEL SEND} ", "Socket writes 0 byte in session: ", session);
        }
      }
      */

      // update statistic data
      getNetworkWriterStatistic().updateWrittenBytes(realWrittenBytes);

      // update statistic data for the session too
      session.addWrittenBytes(realWrittenBytes);

      // the left unwritten bytes should be remained to the queue for next process
      if (realWrittenBytes < expectedWritingBytes) {
        // create new bytes array to hold the left unsent bytes
        byte[] leftUnwrittenBytes = new byte[getBuffer().remaining()];

        // get bytes array value from buffer
        getBuffer().get(leftUnwrittenBytes);

        // save those bytes to the packet for next manipulation
        packet.setFragmentBuffer(leftUnwrittenBytes);
      } else {
        // update the statistic data
        getNetworkWriterStatistic().updateWrittenPackets(1);

        // now the packet can be safely removed
        packetQueue.take();

        // in case this packet is the last one, it closes the session
        if (packet.isMarkedAsLast()) {
          packetQueue.clear();
          session.close(ConnectionDisconnectMode.DEFAULT, PlayerDisconnectMode.DEFAULT);
          return;
        }

        // if the packet queue still contains more packets, then put the session back to
        // the tickets queue
        if (!packetQueue.isEmpty()) {
          getSessionTicketsQueue().add(session);
        }
      }

      // want to know when the socket can write, which should be noticed on isWritable() method
      // when that event occurs, try to re-add the session to the tickets queue
      var selectionKey = session.getSelectionKey();
      if (Objects.nonNull(selectionKey) && selectionKey.isValid()) {
        selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
      } else {
        if (isErrorEnabled()) {
          error("{SOCKET CHANNEL SEND} ", "Something went wrong with OP_WRITE key for session: ",
              session);
        }
      }
    } catch (IOException exception) {
      if (isErrorEnabled()) {
        error(exception, "Error occurred in writing on session: ", session.toString());
      }
    }
  }
}
