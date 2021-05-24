package com.tenio.core.network;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.tenio.core.network.defines.data.PathConfig;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.service.Service;

public interface NetworkService extends Service {

	void setHttpPort(int port);

	void setHttpPathConfigs(List<PathConfig> pathConfigs);

	void setConnectionFilterClass(Class<? extends ConnectionFilter> clazz)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException;

	void setWebsocketConsumerWorkers(int workerSize);

	void setWebsocketProducerWorkers(int workerSize);

	void setWebsocketSenderBufferSize(int bufferSize);

	void setWebsocketReceiverBufferSize(int bufferSize);

	void setWebsocketConfig(SocketConfig socketConfig);

	void setWebsocketUsingSSL(boolean usingSSL);

	void setSocketAcceptorWorkers(int workerSize);

	void setSocketReaderWorkers(int workerSize);

	void setSocketWriterWorkers(int workerSize);

	void setSocketAcceptorBufferSize(int bufferSize);

	void setSocketReaderBufferSize(int bufferSize);

	void setSocketWriterBufferSize(int bufferSize);
	
	void setSocketConfigs(List<SocketConfig> socketConfigs);

	void write(Packet packet);

}
