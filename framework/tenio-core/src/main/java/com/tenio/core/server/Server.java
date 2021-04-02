/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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

import java.io.IOException;
import java.util.List;

import com.tenio.common.api.TaskApi;
import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.configuration.constant.CommonConstants;
import com.tenio.common.element.CommonObject;
import com.tenio.common.element.CommonObjectArray;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.msgpack.ByteArrayInputStream;
import com.tenio.common.pool.IElementPool;
import com.tenio.common.task.ITaskManager;
import com.tenio.common.task.TaskManager;
import com.tenio.core.api.MessageApi;
import com.tenio.core.api.PlayerApi;
import com.tenio.core.api.RoomApi;
import com.tenio.core.configuration.HttpConfig;
import com.tenio.core.configuration.SocketConfig;
import com.tenio.core.configuration.constant.CoreConstants;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.entity.manager.IPlayerManager;
import com.tenio.core.entity.manager.IRoomManager;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.event.EventManager;
import com.tenio.core.event.IEventManager;
import com.tenio.core.exception.DuplicatedUriAndMethodException;
import com.tenio.core.exception.NotDefinedSocketConnectionException;
import com.tenio.core.exception.NotDefinedSubscribersException;
import com.tenio.core.extension.IExtension;
import com.tenio.core.monitoring.system.SystemInfo;
import com.tenio.core.network.INetwork;
import com.tenio.core.network.http.HttpManagerTask;
import com.tenio.core.network.netty.NettyNetwork;
import com.tenio.core.pool.ByteArrayInputStreamPool;
import com.tenio.core.pool.MessageObjectArrayPool;
import com.tenio.core.pool.MessageObjectPool;
import com.tenio.core.task.schedule.CCUScanTask;
import com.tenio.core.task.schedule.DeadlockScanTask;
import com.tenio.core.task.schedule.EmptyRoomScanTask;
import com.tenio.core.task.schedule.SystemMonitoringTask;
import com.tenio.core.task.schedule.TimeOutScanTask;

/**
 * This class manages the workflow of the current server. The instruction's
 * orders are important, event subscribes must be set last and all configuration
 * values should be confirmed.
 * 
 * @see IServer
 * 
 * @author kong
 * 
 */
public final class Server extends AbstractLogger implements IServer {

	private static Server __instance;

	private Server() {
		__msgObjectPool = new MessageObjectPool();
		__msgArrayPool = new MessageObjectArrayPool();
		__byteArrayPool = new ByteArrayInputStreamPool();

		__eventManager = new EventManager();

		__roomManager = new RoomManager(__eventManager);
		__playerManager = new PlayerManager(__eventManager);
		__taskManager = new TaskManager();

		__playerApi = new PlayerApi(__playerManager, __roomManager);
		__roomApi = new RoomApi(__roomManager);
		__taskApi = new TaskApi(__taskManager);
		__messageApi = new MessageApi(__eventManager, __msgObjectPool, __msgArrayPool);

		__internalLogic = new InternalLogicManager(__eventManager, __playerManager, __roomManager);

		// print out the framework's preface
		for (var line : CommonConstants.LOGO) {
			_info("", "", line);
		}
	} // prevent creation manually

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static Server getInstance() {
		if (__instance == null) {
			__instance = new Server();
		}
		return __instance;
	}

	private IConfiguration __configuration;

	private IElementPool<CommonObject> __msgObjectPool;
	private IElementPool<CommonObjectArray> __msgArrayPool;
	private IElementPool<ByteArrayInputStream> __byteArrayPool;

	private IEventManager __eventManager;

	private IRoomManager __roomManager;
	private IPlayerManager __playerManager;
	private ITaskManager __taskManager;

	private PlayerApi __playerApi;
	private RoomApi __roomApi;
	private TaskApi __taskApi;
	private MessageApi __messageApi;

	private InternalLogicManager __internalLogic;
	private IExtension __extension;
	private INetwork __network;

	private List<SocketConfig> __socketPorts;
	private List<SocketConfig> __webSocketPorts;
	private List<HttpConfig> __httpPorts;
	private int __socketPortsSize;
	private int __webSocketPortsSize;
	private String __serverName;

	@SuppressWarnings("unchecked")
	@Override
	public void start(IConfiguration configuration) throws IOException, InterruptedException,
			NotDefinedSocketConnectionException, NotDefinedSubscribersException, DuplicatedUriAndMethodException {
		__configuration = configuration;

		__serverName = configuration.getString(CoreConfigurationType.SERVER_NAME);

		// show system information
		SystemInfo.getInstance().logSystemInfo();
		SystemInfo.getInstance().logNetCardsInfo();
		SystemInfo.getInstance().logDiskInfo();

		_info("SERVER", __serverName, "Starting ...");

		// Put the current configurations to the logger
		_info("CONFIGURATION", configuration.toString());

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
		getExtension().initialize(configuration);

		// server need at least one connection to start up
		__checkDefinedMainSocketConnection(configuration);

		// HTTP checking
		__checkSubscriberHttpHandler(configuration);

		// schedules
		__createAllSchedules(configuration);

		// HTTP handler
		__createHttpManagers(configuration);

		// start network
		__startNetwork(configuration, __msgObjectPool, __byteArrayPool);

		// check subscribers must handle subscribers for UDP attachment
		__checkSubscriberSubConnectionAttach(configuration);

		// must handle subscribers for reconnection
		__checkSubscriberReconnection(configuration);

		// collect all subscribers, listen all the events
		__eventManager.subscribe();

		_info("SERVER", __serverName, "Started!");
	}

	private void __startNetwork(IConfiguration configuration, IElementPool<CommonObject> msgObjectPool,
			IElementPool<ByteArrayInputStream> byteArrayPool) throws IOException, InterruptedException {
		__network = new NettyNetwork();
		__network.start(__eventManager, configuration, msgObjectPool, byteArrayPool);
	}

	@Override
	public void shutdown() {
		_info("SERVER", __serverName, "Stopping ...");
		__shutdown();
		_info("SERVER", __serverName, "Stopped!");
		__cleanup();
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
		__msgObjectPool.cleanup();
		__msgArrayPool.cleanup();
		__byteArrayPool.cleanup();
		// clear all ports
		__socketPorts.clear();
		__webSocketPorts.clear();
		__httpPorts.clear();
	}

	private void __cleanup() {
		// assign by null
		__configuration = null;
		__msgObjectPool = null;
		__msgArrayPool = null;
		__byteArrayPool = null;
		__eventManager = null;
		__roomManager = null;
		__playerManager = null;
		__taskManager = null;
		__playerApi = null;
		__roomApi = null;
		__taskApi = null;
		__messageApi = null;
		__internalLogic = null;
		__extension = null;
		__network = null;
		__socketPorts = null;
		__webSocketPorts = null;
		__httpPorts = null;
	}

	@Override
	public IExtension getExtension() {
		return __extension;
	}

	@Override
	public void setExtension(IExtension extension) {
		__extension = extension;
	}

	private void __checkSubscriberReconnection(IConfiguration configuration) throws NotDefinedSubscribersException {
		if (configuration.getBoolean(CoreConfigurationType.KEEP_PLAYER_ON_DISCONNECT)) {
			if (!__eventManager.getExtension().hasSubscriber(ExtEvent.PLAYER_RECONNECT_REQUEST_HANDLE)
					|| !__eventManager.getExtension().hasSubscriber(ExtEvent.PLAYER_RECONNECT_SUCCESS)) {
				throw new NotDefinedSubscribersException(ExtEvent.PLAYER_RECONNECT_REQUEST_HANDLE,
						ExtEvent.PLAYER_RECONNECT_SUCCESS);
			}
		}
	}

	private void __checkSubscriberSubConnectionAttach(IConfiguration configuration)
			throws NotDefinedSubscribersException {
		if (__socketPortsSize > 1 || __webSocketPortsSize > 1) {
			if (!__eventManager.getExtension().hasSubscriber(ExtEvent.ATTACH_CONNECTION_REQUEST_VALIDATE)
					|| !__eventManager.getExtension().hasSubscriber(ExtEvent.ATTACH_CONNECTION_SUCCESS)
					|| !__eventManager.getExtension().hasSubscriber(ExtEvent.ATTACH_CONNECTION_FAILED)) {
				throw new NotDefinedSubscribersException(ExtEvent.ATTACH_CONNECTION_REQUEST_VALIDATE,
						ExtEvent.ATTACH_CONNECTION_SUCCESS, ExtEvent.ATTACH_CONNECTION_FAILED);
			}
		}
	}

	private void __checkDefinedMainSocketConnection(IConfiguration configuration)
			throws NotDefinedSocketConnectionException {
		if (__socketPorts.isEmpty() && __webSocketPorts.isEmpty()) {
			throw new NotDefinedSocketConnectionException();
		}
	}

	private void __checkSubscriberHttpHandler(IConfiguration configuration) throws NotDefinedSubscribersException {
		if (!__httpPorts.isEmpty() && (!__eventManager.getExtension().hasSubscriber(ExtEvent.HTTP_REQUEST_VALIDATE)
				|| !__eventManager.getExtension().hasSubscriber(ExtEvent.HTTP_REQUEST_HANDLE))) {
			throw new NotDefinedSubscribersException(ExtEvent.HTTP_REQUEST_VALIDATE, ExtEvent.HTTP_REQUEST_HANDLE);
		}
	}

	private void __createAllSchedules(IConfiguration configuration) {
		__taskManager.create(CoreConstants.KEY_SCHEDULE_TIME_OUT_SCAN,
				(new TimeOutScanTask(__eventManager, __playerApi,
						configuration.getInt(CoreConfigurationType.IDLE_READER_TIME),
						configuration.getInt(CoreConfigurationType.IDLE_WRITER_TIME),
						configuration.getInt(CoreConfigurationType.TIMEOUT_SCAN_INTERVAL))).run());

		__taskManager.create(CoreConstants.KEY_SCHEDULE_EMPTY_ROOM_SCAN,
				(new EmptyRoomScanTask(__roomApi, configuration.getInt(CoreConfigurationType.EMPTY_ROOM_SCAN_INTERVAL)))
						.run());

		__taskManager.create(CoreConstants.KEY_SCHEDULE_CCU_SCAN, (new CCUScanTask(__eventManager, __playerApi,
				configuration.getInt(CoreConfigurationType.CCU_SCAN_INTERVAL))).run());

		__taskManager.create(CoreConstants.KEY_SCHEDULE_SYSTEM_MONITORING, (new SystemMonitoringTask(__eventManager,
				configuration.getInt(CoreConfigurationType.SYSTEM_MONITORING_INTERVAL))).run());

		__taskManager.create(CoreConstants.KEY_SCHEDULE_DEADLOCK_SCAN,
				(new DeadlockScanTask(CoreConstants.DEADLOCKED_THREAD_SCAN_INTERVAL)).run());
	}

	private String __createHttpManagers(IConfiguration configuration) throws DuplicatedUriAndMethodException {
		for (var port : __httpPorts) {
			var http = new HttpManagerTask(__eventManager, port.getName(), port.getPort(), port.getPaths());
			http.setup();
			__taskManager.create(CoreConstants.KEY_SCHEDULE_HTTP_MANAGER, http.run());
		}
		return null;
	}

	@Override
	public PlayerApi getPlayerApi() {
		return __playerApi;
	}

	@Override
	public RoomApi getRoomApi() {
		return __roomApi;
	}

	@Override
	public MessageApi getMessageApi() {
		return __messageApi;
	}

	@Override
	public TaskApi getTaskApi() {
		return __taskApi;
	}

	@Override
	public IEventManager getEventManager() {
		return __eventManager;
	}

}
