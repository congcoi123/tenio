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
package com.tenio.core.server;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.configuration.constant.Trademark;
import com.tenio.common.loggers.SystemLogger;
import com.tenio.core.api.ServerApi;
import com.tenio.core.api.ServerApiImpl;
import com.tenio.core.bootstrap.BootstrapHandler;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.configuration.defines.CoreConfigurationType;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.managers.implement.PlayerManagerImpl;
import com.tenio.core.entities.managers.implement.RoomManagerImpl;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.monitoring.system.SystemInfo;
import com.tenio.core.network.NetworkService;
import com.tenio.core.network.NetworkServiceImpl;
import com.tenio.core.network.defines.data.HttpConfig;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.entities.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entities.protocols.Response;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.zero.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.zero.codec.encryption.BinaryPacketEncrypter;
import com.tenio.core.schedule.ScheduleService;
import com.tenio.core.schedule.ScheduleServiceImpl;
import com.tenio.core.server.services.InternalProcessorService;
import com.tenio.core.server.services.InternalProcessorServiceImpl;
import com.tenio.core.server.settings.ConfigurationAssessment;

/**
 * This class manages the workflow of the current server. The instruction's
 * orders are important, event subscribes must be set last and all configuration
 * values should be confirmed.
 */
@ThreadSafe
public final class ServerImpl extends SystemLogger implements Server {

	private static Server __instance;

	private ServerImpl() {

		__eventManager = EventManager.newInstance();
		__roomManager = RoomManagerImpl.newInstance(__eventManager);
		__playerManager = PlayerManagerImpl.newInstance(__eventManager);
		__networkService = NetworkServiceImpl.newInstance(__eventManager);
		__internalProcessorService = InternalProcessorServiceImpl.newInstance(__eventManager);
		__scheduleService = ScheduleServiceImpl.newInstance(__eventManager);
		__serverApi = ServerApiImpl.newInstance(this);

		// print out the framework's preface
		for (var line : Trademark.CONTENT) {
			info("", "", line);
		}
	} // prevent creation manually

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static Server getInstance() {
		if (__instance == null) {
			__instance = new ServerImpl();
		}
		return __instance;
	}

	private final EventManager __eventManager;
	private final RoomManager __roomManager;
	private final PlayerManager __playerManager;
	private final InternalProcessorService __internalProcessorService;
	private final ScheduleService __scheduleService;
	private final NetworkService __networkService;
	private final ServerApi __serverApi;

	private String __serverName;

	@Override
	public void start(BootstrapHandler bootstrapHandler, String[] params) throws Exception {

		// get the file path
		var file = params.length == 0 ? null : params[0];
		if (file == null) {
			file = CoreConstant.DEFAULT_CONNFIGURATION_FILE;
		}

		// load configuration file
		var configuration = bootstrapHandler.getConfigurationHandler().getConfiguration();
		configuration.load(file);

		__serverName = configuration.getString(CoreConfigurationType.SERVER_NAME);

		// show system information
		var systemInfo = new SystemInfo();
		systemInfo.logSystemInfo();
		systemInfo.logNetCardsInfo();
		systemInfo.logDiskInfo();

		info("SERVER", __serverName, "Starting ...");

		// subscribing for processes and handlers
		__internalProcessorService.subscribe();

		bootstrapHandler.getEventHandler().initialize(__eventManager);

		// collect all subscribers, listen all the events
		__eventManager.subscribe();

		var assessment = ConfigurationAssessment.newInstance(__eventManager, configuration);
		assessment.assess();

		// Put the current configurations to the logger
		info("CONFIGURATION", configuration.toString());

		__setupNetworkService(configuration);
		__setupInternalProcessorService(configuration);
		__setupScheduleService(configuration);

		__initializeServices();
		__startServices();

		// emit "server started" event
		__eventManager.emit(ServerEvent.SERVER_INITIALIZATION, __serverName, configuration);

		info("SERVER", __serverName, "Started");
	}

	private void __initializeServices() {
		__networkService.initialize();
		__internalProcessorService.initialize();
		__scheduleService.initialize();
	}

	private void __startServices() {
		__networkService.start();
		__internalProcessorService.start();
		__scheduleService.start();
	}

	private void __setupScheduleService(Configuration configuration) {
		__scheduleService.setCcuReportInterval(configuration.getInt(CoreConfigurationType.INTERVAL_CCU_SCAN));
		__scheduleService.setDeadlockScanInterval(configuration.getInt(CoreConfigurationType.INTERVAL_DEADLOCK_SCAN));
		__scheduleService.setDisconnectedPlayerScanInterval(
				configuration.getInt(CoreConfigurationType.INTERVAL_DISCONNECTED_PLAYER_SCAN));
		__scheduleService
				.setRemovedRoomScanInterval(configuration.getInt(CoreConfigurationType.INTERVAL_REMOVED_ROOM_SCAN));
		__scheduleService
				.setSystemMonitoringInterval(configuration.getInt(CoreConfigurationType.INTERVAL_SYSTEM_MONITORING));
		__scheduleService
				.setTrafficCounterInterval(configuration.getInt(CoreConfigurationType.INTERVAL_TRAFFIC_COUNTER));

		__scheduleService.setPlayerManager(__playerManager);
		__scheduleService.setRoomManager(__roomManager);
		__scheduleService.setNetworkReaderStatistic(__networkService.getNetworkReaderStatistic());
		__scheduleService.setNetworkWriterStatistic(__networkService.getNetworkWriterStatistic());
	}

	@SuppressWarnings("unchecked")
	private void __setupNetworkService(Configuration configuration)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

		var connectionFilterClazz = Class
				.forName(configuration.getString(CoreConfigurationType.CLASS_CONNECTION_FILTER).strip());
		__networkService.setConnectionFilterClass((Class<? extends ConnectionFilter>) connectionFilterClazz,
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_MAX_CONNECTIONS_PER_IP));

		var httpConfig = (List<HttpConfig>) configuration.get(CoreConfigurationType.HTTP_CONFIGS);
		__networkService.setHttpPort(!httpConfig.isEmpty() ? httpConfig.get(0).getPort() : 0);
		__networkService.setHttpPathConfigs(!httpConfig.isEmpty() ? httpConfig.get(0).getPaths() : null);

		__networkService.setSocketAcceptorBufferSize(
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_ACCEPTOR_BUFFER_SIZE));
		__networkService.setSocketAcceptorWorkers(configuration.getInt(CoreConfigurationType.THREADS_SOCKET_ACCEPTOR));

		__networkService.setSocketConfigs((List<SocketConfig>) configuration.get(CoreConfigurationType.SOCKET_CONFIGS));

		__networkService.setSocketReaderBufferSize(
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_READER_BUFFER_SIZE));
		__networkService.setSocketReaderWorkers(configuration.getInt(CoreConfigurationType.THREADS_SOCKET_READER));

		__networkService.setSocketWriterBufferSize(
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_WRITER_BUFFER_SIZE));
		__networkService.setSocketWriterWorkers(configuration.getInt(CoreConfigurationType.THREADS_SOCKET_WRITER));

		__networkService
				.setWebsocketConsumerWorkers(configuration.getInt(CoreConfigurationType.THREADS_WEBSOCKET_CONSUMER));
		__networkService
				.setWebsocketProducerWorkers(configuration.getInt(CoreConfigurationType.THREADS_WEBSOCKET_PRODUCER));

		__networkService.setWebsocketReceiverBufferSize(
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_RECEIVER_BUFFER_SIZE));
		__networkService.setWebsocketSenderBufferSize(
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_SENDER_BUFFER_SIZE));
		__networkService
				.setWebsocketUsingSSL(configuration.getBoolean(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_USING_SSL));

		var packetQueuePolicyClazz = Class
				.forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_QUEUE_POLICY).strip());
		__networkService.setPacketQueuePolicy((Class<? extends PacketQueuePolicy>) packetQueuePolicyClazz);
		__networkService.setPacketQueueSize(configuration.getInt(CoreConfigurationType.PROP_MAX_PACKET_QUEUE_SIZE));

		var binaryPacketCompressorClazz = Class
				.forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_COMPRESSOR).strip());
		var binaryPacketCompressor = (BinaryPacketCompressor) binaryPacketCompressorClazz.getDeclaredConstructor()
				.newInstance();
		var binaryPacketEncrypterClazz = Class
				.forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_ENCRYPTER).strip());
		var binaryPacketEncrypter = (BinaryPacketEncrypter) binaryPacketEncrypterClazz.getDeclaredConstructor()
				.newInstance();
		var binaryPacketEncoderClazz = Class
				.forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_ENCODER).strip());
		var binaryPacketEncoder = (BinaryPacketEncoder) binaryPacketEncoderClazz.getDeclaredConstructor().newInstance();
		var binaryPacketDecoderClazz = Class
				.forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_DECODER).strip());
		var binaryPacketDecoder = (BinaryPacketDecoder) binaryPacketDecoderClazz.getDeclaredConstructor().newInstance();

		binaryPacketEncoder.setCompressionThresholdBytes(
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_PACKET_COMPRESSION_THRESHOLD_BYTES));
		binaryPacketEncoder.setCompressor(binaryPacketCompressor);
		binaryPacketEncoder.setEncrypter(binaryPacketEncrypter);

		binaryPacketDecoder.setCompressor(binaryPacketCompressor);
		binaryPacketDecoder.setEncrypter(binaryPacketEncrypter);

		__networkService.setPacketDecoder(binaryPacketDecoder);
		__networkService.setPacketEncoder(binaryPacketEncoder);
	}

	private void __setupInternalProcessorService(Configuration configuration) {
		__internalProcessorService
				.setMaxNumberPlayers(configuration.getInt(CoreConfigurationType.PROP_MAX_NUMBER_PLAYERS));
		__internalProcessorService.setPlayerManager(__playerManager);
		__internalProcessorService
				.setMaxRequestQueueSize(configuration.getInt(CoreConfigurationType.PROP_MAX_REQUEST_QUEUE_SIZE));
		__internalProcessorService
				.setThreadPoolSize(configuration.getInt(CoreConfigurationType.THREADS_INTERNAL_PROCESSOR));
	}

	@Override
	public void shutdown() {
		info("SERVER", __serverName, "Stopping ...");
		// emit "server shutdown" event
		__eventManager.emit(ServerEvent.SERVER_TEARDOWN, __serverName);
		__shutdownServices();
		info("SERVER", __serverName, "Stopped");
	}

	private void __shutdownServices() {
		__internalProcessorService.shutdown();
		__networkService.shutdown();
		__scheduleService.shutdown();
	}

	@Override
	public ServerApi getApi() {
		return __serverApi;
	}

	@Override
	public EventManager getEventManager() {
		return __eventManager;
	}

	@Override
	public PlayerManager getPlayerManager() {
		return __playerManager;
	}

	@Override
	public RoomManager getRoomManager() {
		return __roomManager;
	}

	@Override
	public void write(Response response) {
		__networkService.write(response);
	}

}
