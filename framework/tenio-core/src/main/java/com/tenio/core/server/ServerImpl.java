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
import com.tenio.common.configuration.constant.CommonConstant;
import com.tenio.common.loggers.SystemLogger;
import com.tenio.core.api.ServerApi;
import com.tenio.core.api.ServerApiImpl;
import com.tenio.core.configuration.defines.CoreConfigurationType;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.managers.implement.PlayerManagerImpl;
import com.tenio.core.entities.managers.implement.RoomManagerImpl;
import com.tenio.core.events.EventManager;
import com.tenio.core.events.implement.EventManagerImpl;
import com.tenio.core.monitoring.system.SystemInfo;
import com.tenio.core.network.NetworkService;
import com.tenio.core.network.NetworkServiceImpl;
import com.tenio.core.network.defines.data.HttpConfig;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.schedule.ScheduleService;
import com.tenio.core.schedule.ScheduleServiceImpl;
import com.tenio.core.server.services.InternalProcessorService;
import com.tenio.core.server.settings.ConfigurationAssessment;

/**
 * This class manages the workflow of the current server. The instruction's
 * orders are important, event subscribes must be set last and all configuration
 * values should be confirmed.
 * 
 * @see Server
 * 
 * @author kong
 * 
 */
@ThreadSafe
public final class ServerImpl extends SystemLogger implements Server {

	private static Server __instance;

	private ServerImpl() {

		__eventManager = EventManagerImpl.newInstance();
		__roomManager = RoomManagerImpl.newInstance(__eventManager);
		__playerManager = PlayerManagerImpl.newInstance(__eventManager);
		__networkService = NetworkServiceImpl.newInstance(__eventManager);
		__internalProcessorService = InternalProcessorService.newInstance(__eventManager);
		__scheduleService = ScheduleServiceImpl.newInstance(__eventManager);
		__serverApi = ServerApiImpl.newInstance(this);

		// print out the framework's preface
		for (var line : CommonConstant.CREDIT) {
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

	private Configuration __configuration;
	private final EventManager __eventManager;
	private final RoomManager __roomManager;
	private final PlayerManager __playerManager;
	private final InternalProcessorService __internalProcessorService;
	private final ScheduleService __scheduleService;
	private final NetworkService __networkService;
	private final ServerApi __serverApi;

	private String __serverName;

	public void start(Configuration configuration) {
		__configuration = configuration;

		var ConfigsAssessment = ConfigurationAssessment.newInstance(__eventManager, configuration);
		ConfigsAssessment.assess();

		__serverName = configuration.getString(CoreConfigurationType.SERVER_NAME);

		// show system information
		var systemInfo = new SystemInfo();
		systemInfo.logSystemInfo();
		systemInfo.logNetCardsInfo();
		systemInfo.logDiskInfo();

		info("SERVER", __serverName, "Starting ...");

		// Put the current configurations to the logger
		info("CONFIGURATION", configuration.toString());

		// create all ports information
		__socketPorts = (List<SocketConfig>) (configuration.get(CoreConfigurationType.SOCKET_CONFIGS));
		__webSocketPorts = (List<SocketConfig>) (configuration.get(CoreConfigurationType.WEBSOCKET_PORTS));
		__httpPorts = (List<HttpConfig>) (configuration.get(CoreConfigurationType.HTTP_CONFIGS));

		__socketPortsSize = __socketPorts.size();
		__webSocketPortsSize = __webSocketPorts.size();

		// managements
		__playerManager.initialize(configuration);
		__roomManager.initialize(configuration);

		// main server logic
		__internalProcessorService.init(configuration);

		// initialize the subscribers
		var extension = getExtension();
		if (extension != null) {
			extension.initialize(configuration);
		}
		if (eventHandler != null) {
			eventHandler.initialize();
		}

		// server need at least one connection to start up
		__checkDefinedMainSocketConnection(configuration);

		// HTTP checking
		__checkSubscriberHttpHandler(configuration);

		// schedules
		__createAllSchedules(configuration);

		// HTTP handler
		__createHttpManagers(configuration);

		// start network
		__startNetwork(configuration, __commonObjectPool, __byteArrayInputPool);

		// check subscribers must handle subscribers for UDP attachment
		__checkSubscriberSubConnectionAttach(configuration);

		// must handle subscribers for reconnection
		__checkSubscriberReconnection(configuration);

		// collect all subscribers, listen all the events
		__eventManager.subscribe();

		info("SERVER", __serverName, "Started!");
	}

	private void __setupScheduleService() {
		__scheduleService.setCcuScanInterval(__configuration.getInt(CoreConfigurationType.INTERVAL_CCU_SCAN));
		__scheduleService.setDeadlockScanInterval(__configuration.getInt(CoreConfigurationType.INTERVAL_DEADLOCK_SCAN));
		__scheduleService.setDisconnectedPlayerScanInterval(
				__configuration.getInt(CoreConfigurationType.INTERVAL_DISCONNECTED_PLAYER_SCAN));
		__scheduleService
				.setRemovedRoomScanInterval(__configuration.getInt(CoreConfigurationType.INTERVAL_REMOVED_ROOM_SCAN));
		__scheduleService
				.setSystemMonitoringInterval(__configuration.getInt(CoreConfigurationType.INTERVAL_SYSTEM_MONITORING));
		__scheduleService
				.setTrafficCounterInterval(__configuration.getInt(CoreConfigurationType.INTERVAL_TRAFFIC_COUNTER));

		__scheduleService.setPlayerManager(__playerManager);
		__scheduleService.setRoomManager(__roomManager);
	}

	private void __setupNetworkService() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		var connectionFilterClazz = Class
				.forName(__configuration.getString(CoreConfigurationType.CLASS_CONNECTION_FILTER));
		__networkService.setConnectionFilterClass((Class<? extends ConnectionFilter>) connectionFilterClazz);
		var httpConfig = (List<HttpConfig>) __configuration.get(CoreConfigurationType.HTTP_CONFIGS);
		__networkService.setHttpPort(httpConfig.get(0).getPort());
		__networkService.setHttpPathConfigs(httpConfig.get(0).getPaths());
		__networkService.setSocketAcceptorBufferSize(
				__configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_ACCEPTOR_BUFFER_SIZE));
		__networkService
				.setSocketAcceptorWorkers(__configuration.getInt(CoreConfigurationType.THREADS_SOCKET_ACCEPTOR));
		__networkService
				.setSocketConfigs((List<SocketConfig>) __configuration.get(CoreConfigurationType.SOCKET_CONFIGS));
		__networkService.setSocketReaderBufferSize(
				__configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_READER_BUFFER_SIZE));
		__networkService.setSocketReaderWorkers(__configuration.getInt(CoreConfigurationType.THREADS_SOCKET_READER));
		__networkService.setSocketWriterBufferSize(
				__configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_WRITER_BUFFER_SIZE));
		__networkService.setSocketWriterWorkers(__configuration.getInt(CoreConfigurationType.THREADS_SOCKET_WRITER));
		// FIXME:
		__networkService.setWebsocketConfig(null);
		__networkService
				.setWebsocketConsumerWorkers(__configuration.getInt(CoreConfigurationType.THREADS_WEBSOCKET_CONSUMER));
		__networkService
				.setWebsocketProducerWorkers(__configuration.getInt(CoreConfigurationType.THREADS_WEBSOCKET_PRODUCER));
		__networkService.setWebsocketReceiverBufferSize(
				__configuration.getInt(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_RECEIVER_BUFFER_SIZE));
		__networkService.setWebsocketSenderBufferSize(
				__configuration.getInt(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_SENDER_BUFFER_SIZE));
		__networkService.setWebsocketUsingSSL(
				__configuration.getBoolean(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_USING_SSL));
	}

	private void __setupInternalProcessorService() {

	}

	@Override
	public synchronized void shutdown() {
		info("SERVER", __serverName, "Stopping ...");
		__shutdown();
		info("SERVER", __serverName, "Stopped!");
	}

	private void __shutdown() {
		if (__network != null) {
			__network.shutdown();
		}
		// clear configuration
		__configuration.clear();
		// clear all managers
		__roomManager.clear();
		__playerManager.clear();
		__taskManager.clear();
		__eventManager.clear();
		// clear all pools
		__commonObjectPool.cleanup();
		__commonObjectArrayPool.cleanup();
		__byteArrayInputPool.cleanup();
		// clear all ports
		__socketPorts.clear();
		__webSocketPorts.clear();
		__httpPorts.clear();
	}

	@Override
	public void start() {

	}

	@Override
	public ServerApi getApi() {
		return __serverApi;
	}

}
