/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.zero;

import com.tenio.core.network.define.data.SocketConfig;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.service.Service;
import java.util.List;

/**
 * The APIs designed for working with sockets.
 */
public interface ZeroSocketService extends Service {

  void setAcceptorBufferSize(int bufferSize);

  void setAcceptorWorkerSize(int workerSize);

  void setReaderBufferSize(int bufferSize);

  void setReaderWorkerSize(int workerSize);

  void setWriterBufferSize(int bufferSize);

  void setWriterWorkerSize(int workerSize);

  void setConnectionFilter(ConnectionFilter connectionFilter);

  void setSessionManager(SessionManager sessionManager);

  void setNetworkReaderStatistic(NetworkReaderStatistic readerStatistic);

  void setNetworkWriterStatistic(NetworkWriterStatistic writerStatistic);

  void setSocketConfigs(List<SocketConfig> socketConfigs);

  void setPacketEncoder(BinaryPacketEncoder packetEncoder);

  void setPacketDecoder(BinaryPacketDecoder packetDecoder);

  void write(Packet packet);
}
