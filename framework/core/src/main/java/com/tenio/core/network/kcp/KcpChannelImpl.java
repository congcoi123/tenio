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

package com.tenio.core.network.kcp;

import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.ServiceRuntimeException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.configuration.KcpConfiguration;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.kcp.handler.KcpHandler;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import kcp.KcpServer;

/**
 * The implementation for the KCP service.
 */
public class KcpChannelImpl extends AbstractManager implements KcpChannel {

  private SessionManager sessionManager;
  private BinaryPacketEncoder binaryPacketEncoder;
  private BinaryPacketDecoder binaryPacketDecoder;
  private NetworkReaderStatistic networkReaderStatistic;
  private NetworkWriterStatistic networkWriterStatistic;
  private SocketConfiguration socketConfiguration;
  private KcpServer kcpServer;

  private boolean initialized;

  private KcpChannelImpl(EventManager eventManager) {
    super(eventManager);
    initialized = false;
  }

  /**
   * Constructor.
   *
   * @param eventManager {@link EventManager} object
   * @return an instance of {@link KcpChannel}
   */
  public static KcpChannel newInstance(EventManager eventManager) {
    return new KcpChannelImpl(eventManager);
  }

  @Override
  public void initialize() throws ServiceRuntimeException {
    initialized = true;
  }

  @Override
  public void start() throws ServiceRuntimeException {
    if (!initialized) {
      return;
    }

    kcpServer = new KcpServer();

    if (isInfoEnabled()) {
      info("KCP CHANNEL", buildgen("Started at port: ", socketConfiguration.port()));
    }
  }

  @Override
  public void shutdown() {
    if (!initialized) {
      return;
    }

    kcpServer.stop();
  }

  @Override
  public void activate() {
    if (!initialized) {
      return;
    }

    kcpServer.init(new KcpHandler(eventManager, sessionManager, binaryPacketDecoder,
        networkReaderStatistic), KcpConfiguration.inTurboMode(), socketConfiguration.port());
  }

  @Override
  public boolean isActivated() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return "kcp-channel";
  }

  @Override
  public void setName(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getMaximumStartingTimeInMilliseconds() {
    return 0;
  }

  @Override
  public void setSessionManager(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @Override
  public void setPacketEncoder(BinaryPacketEncoder packetEncoder) {
    this.binaryPacketEncoder = packetEncoder;
  }

  @Override
  public void setPacketDecoder(BinaryPacketDecoder packetDecoder) {
    this.binaryPacketDecoder = packetDecoder;
  }

  @Override
  public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
    this.networkReaderStatistic = networkReaderStatistic;
  }

  @Override
  public void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic) {
    this.networkWriterStatistic = networkWriterStatistic;
  }

  @Override
  public void setKcpSocketConfiguration(SocketConfiguration socketConfiguration) {
    this.socketConfiguration = socketConfiguration;
  }

  @Override
  public void write(Packet packet) {
    var iterator = packet.getRecipients().iterator();
    while (iterator.hasNext()) {
      var session = iterator.next();
      if (packet.isMarkedAsLast()) {
        try {
          if (session.isActivated()) {
            session.close(ConnectionDisconnectMode.CLIENT_REQUEST,
                PlayerDisconnectMode.CLIENT_REQUEST);
          }
        } catch (IOException exception) {
          if (isErrorEnabled()) {
            error(exception, session.toString());
          }
        }
        return;
      }
      if (session.isActivated()) {
        packet = binaryPacketEncoder.encode(packet);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(packet.getData());
        session.getKcpChannel().write(byteBuf);
        byteBuf.release();
        session.addWrittenBytes(packet.getOriginalSize());
        networkWriterStatistic.updateWrittenBytes(packet.getOriginalSize());
        networkWriterStatistic.updateWrittenPackets(1);
      } else {
        if (isDebugEnabled()) {
          debug("WRITE KCP CHANNEL", "Session is inactivated: ", session.toString());
        }
      }
    }
  }
}
