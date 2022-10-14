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

package com.tenio.core.network.entity.kcp;

import com.tenio.common.utility.TimeUtility;
import com.tenio.core.configuration.kcp.KcpProfile;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.KcpWriter;
import com.tenio.core.network.zero.handler.KcpIoHandler;
import java.io.IOException;

/**
 * The KCP wrapper class, it is using for better adapting to the project's structure.
 */
public class Ukcp extends Kcp {

  private final Session session;
  private final KcpIoHandler kcpIoHandler;
  private final KcpWriter<?> kcpWriter;
  private final NetworkWriterStatistic networkWriterStatistic;
  private byte[] buffer;

  /**
   * Constructs a new KCP channel.
   *
   * @param conv                   a {@code long} value of conveying ID, in client side, the conveying ID must be
   *                               the same to make them to be able to communicate
   * @param profile                the KCP configuration defined in {@link KcpProfile}
   * @param session                the {@link Session} which is using KCP channel
   * @param kcpIoHandler           the KCP channel interfaces, they are declared in {@link KcpIoHandler}
   * @param kcpWriter              a {@link KcpWriter} using to send packets from the server to a client
   * @param networkWriterStatistic the {@link NetworkWriterStatistic} is in use of statistics
   */
  public Ukcp(long conv, KcpProfile profile, Session session, KcpIoHandler kcpIoHandler,
              KcpWriter<?> kcpWriter, NetworkWriterStatistic networkWriterStatistic) {
    super(conv);
    this.session = session;
    this.session.setUkcp(this);
    this.kcpIoHandler = kcpIoHandler;
    this.kcpWriter = kcpWriter;
    this.networkWriterStatistic = networkWriterStatistic;

    SetNoDelay(profile.getNoDelay(), profile.getUpdateInterval(), profile.getFastResend(),
        profile.getCongestionControl());
  }

  /**
   * Calls this method to put binaries data from the main channel into KCP.
   *
   * @param binaries the {@code byte} array of input data
   */
  public void input(byte[] binaries) {
    Input(binaries);
  }

  @Override
  protected void Output(byte[] buffer, int size) {
    try {
      int writtenBytes = kcpWriter.send(buffer, size);
      // update statistic data
      networkWriterStatistic.updateWrittenBytes(writtenBytes);
      networkWriterStatistic.updateWrittenPackets(1);

      // update statistic data for session
      session.addWrittenBytes(writtenBytes);
    } catch (IOException exception) {
      kcpIoHandler.sessionException(session, exception);
    }
  }

  /**
   * Calls this method to put binaries data from packets which needs to be sent. KCP will process
   * those internally and decides when they should be delivered in {@link #Output(byte[], int)}
   * method.
   *
   * @param binaries the {@code byte} array of sending data
   */
  public void send(byte[] binaries) {
    Send(binaries);
  }

  /**
   * KCP does it internal processing and returns result through {@code byte} buffer array.
   */
  public void receive() {
    int receive = Recv((buffer) -> this.buffer = buffer);
    if (receive > 0) {
      kcpIoHandler.sessionRead(session, buffer);
    }
  }

  /**
   * Every some time, such as 10ms, call this method to update the status of KCP.
   */
  public void update() {
    Update(TimeUtility.currentTimeMillis());
  }

  /**
   * Retrieves interfaces for KCP behaviors.
   *
   * @return an instance of {@link KcpIoHandler}
   */
  public KcpIoHandler getKcpIoHandler() {
    return kcpIoHandler;
  }

  @Override
  public String toString() {
    return "Ukcp{" +
        "session=" + session +
        ", kcpIoHandler=" + kcpIoHandler +
        ", kcpWriter=" + kcpWriter +
        '}';
  }
}
