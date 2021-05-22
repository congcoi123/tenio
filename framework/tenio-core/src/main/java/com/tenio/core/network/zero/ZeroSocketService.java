package com.tenio.core.network.zero;

import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.security.ConnectionFilter;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;
import com.tenio.core.service.Service;

public interface ZeroSocketService extends Service {

	int getAcceptorBufferSize();

	void setAcceptorBufferSize(int bufferSize);

	int getAcceptorWorkerSize();

	void setAcceptorWorkerSize(int workerSize);

	int getReaderBufferSize();

	void setReaderBufferSize(int bufferSize);

	int getReaderWorkerSize();

	void setReaderWorkerSize(int workerSize);

	int getWriterBufferSize();

	void setWriterBufferSize(int bufferSize);

	int getWriterWorkerSize();

	void setWriterWorkerSize(int workerSize);

	void setConnectionFilter(ConnectionFilter connectionFilter);

	void setSessionManager(SessionManager sessionManager);

	void setNetworkReaderStatistic(NetworkReaderStatistic readerStatistic);

	void setNetworkWriterStatistic(NetworkWriterStatistic writerStatistic);

}
