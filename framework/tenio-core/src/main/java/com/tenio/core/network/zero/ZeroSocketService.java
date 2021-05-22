package com.tenio.core.network.zero;

import java.util.List;

import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;
import com.tenio.core.service.Service;

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

	void write(Packet packet);

}
