package com.tenio.core.network;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.defines.TransportType;
import com.tenio.core.network.defines.data.PathConfig;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.packet.implement.PacketImpl;
import com.tenio.core.network.entities.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entities.protocols.Response;
import com.tenio.core.network.entities.session.Session;
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
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;

public final class NetworkServiceImpl implements NetworkService {

	private JettyHttpService __httpService;
	private NettyWebSocketService __websocketService;
	private ZeroSocketService __socketService;
	private SessionManager __sessionManager;
	private NetworkReaderStatistic __networkReaderStatistic;
	private NetworkWriterStatistic __networkWriterStatistic;

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
		
		__websocketService.setSessionManager(__sessionManager);
		
		__socketService.setSessionManager(__sessionManager);
		__socketService.setNetworkReaderStatistic(__networkReaderStatistic);
		__socketService.setNetworkWriterStatistic(__networkWriterStatistic);
	}

	@Override
	public void initialize() {
		__httpService.initialize();
		__websocketService.initialize();
		__socketService.initialize();
	}

	@Override
	public void start() {
		__httpService.start();
		__websocketService.start();
		__socketService.start();
	}

	@Override
	public void resume() {
		__httpService.resume();
		__websocketService.resume();
		__socketService.resume();
	}

	@Override
	public void pause() {
		__httpService.pause();
		__websocketService.pause();
		__socketService.pause();
	}

	@Override
	public void halt() {
		__httpService.halt();
		__websocketService.halt();
		__socketService.halt();
	}

	@Override
	public void destroy() {
		__httpService.destroy();
		__websocketService.destroy();
		__socketService.destroy();

		__httpService = null;
		__websocketService = null;
		__socketService = null;

		__networkReaderStatistic = null;
		__networkWriterStatistic = null;
	}

	@Override
	public boolean isActivated() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return "network-service";
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHttpPort(int port) {
		__httpService.setPort(port);
	}

	@Override
	public void setHttpPathConfigs(List<PathConfig> pathConfigs) {
		__httpService.setPathConfigs(pathConfigs);
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
		__websocketService.setConsumerWorkerSize(workerSize);
	}

	@Override
	public void setWebsocketProducerWorkers(int workerSize) {
		__websocketService.setProducerWorkerSize(workerSize);
	}

	@Override
	public void setWebsocketSenderBufferSize(int bufferSize) {
		__websocketService.setSenderBufferSize(bufferSize);
	}

	@Override
	public void setWebsocketReceiverBufferSize(int bufferSize) {
		__websocketService.setReceiverBufferSize(bufferSize);
	}

	@Override
	public void setWebsocketUsingSSL(boolean usingSSL) {
		__websocketService.setUsingSSL(usingSSL);
	}

	@Override
	public void setSocketAcceptorWorkers(int workerSize) {
		__socketService.setAcceptorWorkerSize(workerSize);
	}

	@Override
	public void setSocketReaderWorkers(int workerSize) {
		__socketService.setReaderWorkerSize(workerSize);
	}

	@Override
	public void setSocketWriterWorkers(int workerSize) {
		__socketService.setWriterWorkerSize(workerSize);
	}

	@Override
	public void setSocketAcceptorBufferSize(int bufferSize) {
		__socketService.setAcceptorBufferSize(bufferSize);
	}

	@Override
	public void setSocketReaderBufferSize(int bufferSize) {
		__socketService.setReaderBufferSize(bufferSize);
	}

	@Override
	public void setSocketWriterBufferSize(int bufferSize) {
		__socketService.setWriterBufferSize(bufferSize);
	}

	@Override
	public void setSocketConfigs(List<SocketConfig> socketConfigs) {
		__socketService.setSocketConfigs(socketConfigs);
		__websocketService.setWebSocketConfig(socketConfigs.stream()
				.filter(socketConfig -> socketConfig.getType() == TransportType.WEB_SOCKET).findFirst().get());
	}

	@Override
	public void setPacketQueuePolicy(Class<? extends PacketQueuePolicy> clazz)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		__sessionManager.setPacketQueuePolicy(clazz);
	}

	@Override
	public void setPacketQueueSize(int queueSize) {
		__sessionManager.setPacketQueueSize(queueSize);
	}

	@Override
	public void setPacketEncoder(BinaryPacketEncoder packetEncoder) {
		__socketService.setPacketEncoder(packetEncoder);
	}

	@Override
	public void setPacketDecoder(BinaryPacketDecoder packetDecoder) {
		__socketService.setPacketDecoder(packetDecoder);
	}

	@Override
	public NetworkReaderStatistic getNetworkReaderStatistic() {
		return __networkReaderStatistic;
	}

	@Override
	public NetworkWriterStatistic getNetworkWriterStatistic() {
		return __networkWriterStatistic;
	}

	@Override
	public void write(Response response) {
		var socketSessions = response.getRecipientSocketSessions();
		var datagramSessions = response.getRecipientDatagramSessions();
		var websocketSessions = response.getRecipientWebSocketSessions();

		if (socketSessions != null) {
			Packet packet = __createPacket(response, socketSessions, TransportType.TCP);
			__socketService.write(packet);
		}

		if (datagramSessions != null) {
			Packet packet = __createPacket(response, datagramSessions, TransportType.UDP);
			__socketService.write(packet);
		}

		if (websocketSessions != null) {
			Packet packet = __createPacket(response, websocketSessions, TransportType.WEB_SOCKET);
			__websocketService.write(packet);
		}
	}

	private Packet __createPacket(Response response, Collection<Session> recipients, TransportType transportType) {
		Packet packet = PacketImpl.newInstance();
		packet.setData(response.getContent());
		packet.setEncrypted(response.isEncrypted());
		packet.setPriority(response.getPriority());
		packet.setRecipients(recipients);
		packet.setTransportType(transportType);

		return packet;
	}

}
