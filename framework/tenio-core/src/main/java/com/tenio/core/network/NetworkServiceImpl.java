package com.tenio.core.network;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.tenio.core.events.EventManager;
import com.tenio.core.network.defines.data.PathConfig;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.entities.session.implement.SessionManagerImpl;
import com.tenio.core.network.jetty.JettyHttpService;
import com.tenio.core.network.netty.NettyWebSocketService;
import com.tenio.core.network.netty.NettyWebSocketServiceImpl;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;
import com.tenio.core.network.zero.ZeroSocketService;
import com.tenio.core.network.zero.ZeroSocketServiceImpl;

public final class NetworkServiceImpl implements NetworkService {

	private final JettyHttpService __httpService;
	private final NettyWebSocketService __websocketService;
	private final ZeroSocketService __socketService;
	private final SessionManager __sessionManager;
	private final NetworkReaderStatistic __networkReaderStatistic;
	private final NetworkWriterStatistic __networkWriterStatistic;

	public static NetworkService newInstance(EventManager eventManager) {
		return new NetworkServiceImpl(eventManager);
	}

	private NetworkServiceImpl(EventManager eventManager) {
		__httpService = JettyHttpService.newInstance(eventManager);
		__websocketService = NettyWebSocketServiceImpl.newInstance(eventManager);
		__socketService = ZeroSocketServiceImpl.newInstance(eventManager);

		__sessionManager = SessionManagerImpl.newInstance(eventManager);
		__networkReaderStatistic = NetworkReaderStatistic.newInstannce();
		__networkWriterStatistic = NetworkWriterStatistic.newInstance();
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		__httpService.start();
		__websocketService.start();
		__socketService.start();
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void halt() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isActivated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHttpPort(int port) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHttpPathConfigs(List<PathConfig> pathConfigs) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConnectionFilterClass(Class<? extends ConnectionFilter> clazz)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		var connectionFilter = clazz.getDeclaredConstructor().newInstance();

		__websocketService.setConnectionFilter(connectionFilter);
		__socketService.setConnectionFilter(connectionFilter);
	}

	@Override
	public void setWebsocketConsumerWorkers(int workerSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWebsocketProducerWorkers(int workerSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWebsocketSenderBufferSize(int bufferSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWebsocketReceiverBufferSize(int bufferSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWebsocketConfig(SocketConfig socketConfig) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWebsocketUsingSSL(boolean usingSSL) {
		__websocketService.setUsingSSL(usingSSL);
	}

	@Override
	public void setSocketAcceptorWorkers(int workerSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSocketReaderWorkers(int workerSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSocketWriterWorkers(int workerSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSocketAcceptorBufferSize(int bufferSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSocketReaderBufferSize(int bufferSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSocketWriterBufferSize(int bufferSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSocketConfigs(List<SocketConfig> socketConfigs) {

	}

	@Override
	public void write(Packet packet) {
		if (packet.isWebSocket()) {
			__websocketService.write(packet);
		} else {
			__socketService.write(packet);
		}
	}

}
