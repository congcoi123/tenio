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
package com.tenio.core.network;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.tenio.core.network.defines.data.PathConfig;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.entities.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entities.protocols.Response;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.service.Service;

public interface NetworkService extends Service {

	void setHttpPort(int port);

	void setHttpPathConfigs(List<PathConfig> pathConfigs);

	void setConnectionFilterClass(Class<? extends ConnectionFilter> clazz, int maxConnectionsPerIp)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException;

	void setWebsocketConsumerWorkers(int workerSize);

	void setWebsocketProducerWorkers(int workerSize);

	void setWebsocketSenderBufferSize(int bufferSize);

	void setWebsocketReceiverBufferSize(int bufferSize);

	void setWebsocketUsingSSL(boolean usingSSL);

	void setSocketAcceptorWorkers(int workerSize);

	void setSocketReaderWorkers(int workerSize);

	void setSocketWriterWorkers(int workerSize);

	void setSocketAcceptorBufferSize(int bufferSize);

	void setSocketReaderBufferSize(int bufferSize);

	void setSocketWriterBufferSize(int bufferSize);

	void setSocketConfigs(List<SocketConfig> socketConfigs);

	void setPacketQueuePolicy(Class<? extends PacketQueuePolicy> clazz)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException;

	void setPacketQueueSize(int queueSize);

	void setPacketEncoder(BinaryPacketEncoder packetEncoder);

	void setPacketDecoder(BinaryPacketDecoder packetDecoder);

	NetworkReaderStatistic getNetworkReaderStatistic();

	NetworkWriterStatistic getNetworkWriterStatistic();

	void write(Response response);

}
