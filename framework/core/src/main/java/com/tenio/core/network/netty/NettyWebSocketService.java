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

package com.tenio.core.network.netty;

import com.tenio.core.network.define.data.SocketConfig;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.service.Service;

/**
 * The websockets handler is provided by the netty library.
 */
public interface NettyWebSocketService extends Service {

  void setSenderBufferSize(int bufferSize);

  void setReceiverBufferSize(int bufferSize);

  void setProducerWorkerSize(int workerSize);

  void setConsumerWorkerSize(int workerSize);

  void setConnectionFilter(ConnectionFilter connectionFilter);

  void setSessionManager(SessionManager sessionManager);

  void setNetworkReaderStatistic(NetworkReaderStatistic readerStatistic);

  void setNetworkWriterStatistic(NetworkWriterStatistic writerStatistic);

  void setWebSocketConfig(SocketConfig socketConfig);

  void setUsingSsl(boolean usingSsl);

  void write(Packet packet);
}
