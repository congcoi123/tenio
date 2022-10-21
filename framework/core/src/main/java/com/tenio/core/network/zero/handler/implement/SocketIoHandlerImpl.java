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

package com.tenio.core.network.zero.handler.implement;

import com.tenio.common.data.DataUtility;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.RefusedConnectionAddressException;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.decoder.PacketDecoderResultListener;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * The implementation for socket IO handler.
 */
public final class SocketIoHandlerImpl extends AbstractIoHandler
    implements SocketIoHandler, PacketDecoderResultListener {

  private BinaryPacketDecoder binaryPacketDecoder;

  private SocketIoHandlerImpl(EventManager eventManager) {
    super(eventManager);
  }

  public static SocketIoHandler newInstance(EventManager eventManager) {
    return new SocketIoHandlerImpl(eventManager);
  }

  @Override
  public void resultFrame(Session session, byte[] binary) {
    var data = DataUtility.binaryToCollection(dataType, binary);
    var message = ServerMessage.newInstance().setData(data);

    if (!session.isConnected()) {
      eventManager.emit(ServerEvent.SESSION_REQUEST_CONNECTION, session, message);
    } else {
      eventManager.emit(ServerEvent.SESSION_READ_MESSAGE, session, message);
    }
  }

  @Override
  public void updateReadDroppedPackets(long numberPackets) {
    networkReaderStatistic.updateReadDroppedPackets(numberPackets);
  }

  @Override
  public void updateReadPackets(long numberPackets) {
    networkReaderStatistic.updateReadPackets(numberPackets);
  }

  @Override
  public void channelActive(SocketChannel socketChannel, SelectionKey selectionKey) {
    var session = sessionManager.createSocketSession(socketChannel, selectionKey);
    eventManager.emit(ServerEvent.SESSION_CREATED, session);
  }

  @Override
  public void sessionRead(Session session, byte[] binary) {
    binaryPacketDecoder.decode(session, binary);
  }

  @Override
  public void channelInactive(SocketChannel socketChannel) {
    var session = sessionManager.getSessionBySocket(socketChannel);
    if (Objects.isNull(session)) {
      return;
    }

    try {
      session.close(ConnectionDisconnectMode.LOST, PlayerDisconnectMode.CONNECTION_LOST);
    } catch (IOException e) {
      error(e, "Session closed with error: ", session.toString());
      eventManager.emit(ServerEvent.SESSION_OCCURRED_EXCEPTION, session, e);
    }
  }

  @Override
  public void channelException(SocketChannel socketChannel, Exception exception) {
    // handle refused connection, it should send to the client the reason before closing connection
    if (exception instanceof RefusedConnectionAddressException) {
      eventManager.emit(ServerEvent.SOCKET_CONNECTION_REFUSED, socketChannel, exception);
    }
  }

  @Override
  public void sessionException(Session session, Exception exception) {
    eventManager.emit(ServerEvent.SESSION_OCCURRED_EXCEPTION, session, exception);
  }

  @Override
  public void setPacketDecoder(BinaryPacketDecoder packetDecoder) {
    binaryPacketDecoder = packetDecoder;
    binaryPacketDecoder.setResultListener(this);
  }
}
