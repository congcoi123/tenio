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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.api.TaskApi;
import com.tenio.common.configuration.Configuration;
import com.tenio.common.configuration.constant.CommonConstant;
import com.tenio.common.data.elements.CommonObject;
import com.tenio.common.data.elements.CommonObjectArray;
import com.tenio.common.loggers.SystemLogger;
import com.tenio.common.pool.ElementsPool;
import com.tenio.common.task.TaskManager;
import com.tenio.common.task.TaskManagerImpl;
import com.tenio.common.utilities.StringUtility;
import com.tenio.core.api.MessageApi;
import com.tenio.core.api.PlayerApi;
import com.tenio.core.api.RoomApi;
import com.tenio.core.api.ServerApi;
import com.tenio.core.api.ServerApiImpl;
import com.tenio.core.bootstrap.EventHandler;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.configuration.defines.CoreConfigurationType;
import com.tenio.core.configuration.defines.ExtensionEvent;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.managers.implement.PlayerManagerImpl;
import com.tenio.core.entities.managers.implement.RoomManagerImpl;
import com.tenio.core.events.EventManager;
import com.tenio.core.events.implement.EventManagerImpl;
import com.tenio.core.exceptions.DuplicatedUriAndMethodException;
import com.tenio.core.exceptions.NotDefinedSocketConnectionException;
import com.tenio.core.exceptions.NotDefinedSubscribersException;
import com.tenio.core.extension.Extension;
import com.tenio.core.monitoring.system.SystemInfo;
import com.tenio.core.network.IBroadcast;
import com.tenio.core.network.Network;
import com.tenio.core.network.NetworkService;
import com.tenio.core.network.defines.data.HttpConfig;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.jetty.JettyHttpService;
import com.tenio.core.network.netty.NettyWebSocketServiceImpl;
import com.tenio.core.network.security.filter.DefaultConnectionFilter;
import com.tenio.core.network.zero.engines.ZeroAcceptor;
import com.tenio.core.network.zero.engines.ZeroReader;
import com.tenio.core.network.zero.engines.ZeroWriter;
import com.tenio.core.network.zero.engines.implement.ZeroAcceptorImpl;
import com.tenio.core.network.zero.engines.implement.ZeroReaderImpl;
import com.tenio.core.network.zero.engines.implement.ZeroWriterImpl;
import com.tenio.core.schedule.ScheduleService;
import com.tenio.core.schedule.tasks.AutoDisconnectPlayerTask;
import com.tenio.core.schedule.tasks.AutoRemoveRoomTask;
import com.tenio.core.schedule.tasks.CCUScanTask;
import com.tenio.core.schedule.tasks.DeadlockScanTask;
import com.tenio.core.schedule.tasks.SystemMonitoringTask;
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
public final class ServerImpl extends SystemLogger {

	private static ServerImpl __instance;

	private ServerImpl() {

		__eventManager = new EventManagerImpl();
		__networkService = new NetworkService(__eventManager);

		__roomManager = RoomManagerImpl.newInstance(__eventManager);
		__playerManager = PlayerManagerImpl.newInstance(__eventManager);
		__serverApi = new ServerApiImpl();

		__internalLogic = InternalProcessorService.newInstance(__eventManager);

		// print out the framework's preface
		for (var line : CommonConstant.CREDIT) {
			info("", "", line);
		}
	} // prevent creation manually

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static ServerImpl getInstance() {
		if (__instance == null) {
			__instance = new ServerImpl();
		}
		return __instance;
	}

	private Configuration __configuration;

	private final EventManager __eventManager;

	private final RoomManager __roomManager;
	private final PlayerManager __playerManager;

	private final ServerApi __serverApi;

	private final InternalProcessorService __internalLogic;
	private final ScheduleService __scheduleService;
	private final NetworkService __networkService;

	private List<SocketConfig> __socketPorts;
	private List<SocketConfig> __webSocketPorts;
	private List<HttpConfig> __httpPorts;
	private int __socketPortsSize;
	private int __webSocketPortsSize;
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
		__socketPorts = (List<SocketConfig>) (configuration.get(CoreConfigurationType.SOCKET_PORTS));
		__webSocketPorts = (List<SocketConfig>) (configuration.get(CoreConfigurationType.WEBSOCKET_PORTS));
		__httpPorts = (List<HttpConfig>) (configuration.get(CoreConfigurationType.HTTP_PORTS));

		__socketPortsSize = __socketPorts.size();
		__webSocketPortsSize = __webSocketPorts.size();

		// managements
		__playerManager.initialize(configuration);
		__roomManager.initialize(configuration);

		// main server logic
		__internalLogic.init(configuration);

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
	public Extension getExtension() {
		return __extension;
	}

	@Override
	public void setExtension(Extension extension) {
		__extension = extension;
	}

}
