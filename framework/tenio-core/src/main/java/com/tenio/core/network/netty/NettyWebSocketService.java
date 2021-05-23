package com.tenio.core.network.netty;

import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;
import com.tenio.core.service.Service;

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

	void setUsingSSL(boolean usingSSL);

	void write(Packet packet);

}
