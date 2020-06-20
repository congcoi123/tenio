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
package com.tenio.identity.server;

import java.io.IOException;

import com.tenio.common.configuration.constant.Constants;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.task.ITaskManager;
import com.tenio.common.task.TaskManager;
import com.tenio.identity.api.MessageApi;
import com.tenio.identity.api.PlayerApi;
import com.tenio.identity.api.RoomApi;
import com.tenio.identity.api.TaskApi;
import com.tenio.identity.common.configuration.BaseConfiguration;
import com.tenio.identity.common.configuration.constant.TEvent;
import com.tenio.identity.common.entity.manager.IPlayerManager;
import com.tenio.identity.common.entity.manager.IRoomManager;
import com.tenio.identity.common.entity.manager.PlayerManager;
import com.tenio.identity.common.entity.manager.RoomManager;
import com.tenio.identity.common.event.EventManager;
import com.tenio.identity.common.event.IEventManager;
import com.tenio.identity.common.exception.DuplicatedUriAndMethodException;
import com.tenio.identity.common.exception.NotDefinedSocketConnectionException;
import com.tenio.identity.common.exception.NotDefinedSubscribersException;
import com.tenio.identity.common.extension.IExtension;
import com.tenio.identity.common.network.INetwork;
import com.tenio.identity.common.network.http.HttpManagerTask;
import com.tenio.identity.common.network.netty.NettyNetwork;
import com.tenio.identity.common.server.IServer;
import com.tenio.identity.common.server.InternalLogic;
import com.tenio.identity.common.server.Server;
import com.tenio.identity.common.task.schedule.CCUScanTask;
import com.tenio.identity.common.task.schedule.EmptyRoomScanTask;
import com.tenio.identity.common.task.schedule.TimeOutScanTask;

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
		__eventManager = new EventManager();

		__roomManager = new RoomManager(__eventManager);
		__playerManager = new PlayerManager(__eventManager);
		__taskManager = new TaskManager();

		__playerApi = new PlayerApi(__playerManager, __roomManager);
		__roomApi = new RoomApi(__roomManager);
		__taskApi = new TaskApi(__taskManager);
		__messageApi = new MessageApi(__eventManager);

		__internalLogic = new InternalLogic(__eventManager, __playerManager, __roomManager);
		
		// print out the framework's icon
		for (var line : Constants.LOGO) {			
			info("", "", line);
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

	private final IEventManager __eventManager;

	private final IRoomManager __roomManager;
	private final IPlayerManager __playerManager;
	private final ITaskManager __taskManager;

	private final PlayerApi __playerApi;
	private final RoomApi __roomApi;
	private final TaskApi __taskApi;
	private final MessageApi __messageApi;

	private final InternalLogic __internalLogic;
	private IExtension __extension;
	private INetwork __network;

	@Override
	public void start(BaseConfiguration configuration) throws IOException, InterruptedException,
			NotDefinedSocketConnectionException, NotDefinedSubscribersException, DuplicatedUriAndMethodException {
		info("SERVER", configuration.getString(BaseConfiguration.SERVER_NAME), "Starting ...");

		// managements
		__playerManager.initialize(configuration);
		__roomManager.initialize(configuration);

		// main server logic
		__internalLogic.init(configuration);

		// initialize the subscribers
		getExtension().initialize();

		// server need at least one connection to start up
		__checkDefinedMainSocketConnection(configuration);

		// http checking
		__checkSubscriberHttpHandler(configuration);

		// schedules
		__createAllSchedules(configuration);

		// http handler
		__createHttpManagers(configuration);

		// start network
		__startNetwork(configuration);

		// check subscribers must handle subscribers for UDP attachment
		__checkSubscriberSubConnectionAttach(configuration);

		// must handle subscribers for reconnection
		__checkSubscriberReconnection(configuration);

		// collect all subscribers, listen all the events
		__eventManager.subscribe();

		info("SERVER", configuration.getString(BaseConfiguration.SERVER_NAME), "Started!");
	}

	private void __startNetwork(BaseConfiguration configuration) throws IOException, InterruptedException {
		__network = new NettyNetwork();
		__network.start(__eventManager, configuration);
	}

	@Override
	public void shutdown() {
		if (__network != null) {
			__network.shutdown();
		}
		// clear all managers
		__roomManager.clear();
		__playerManager.clear();
		__taskManager.clear();
		__eventManager.clear();
		// exit
		System.exit(0);
	}

	@Override
	public IExtension getExtension() {
		return __extension;
	}

	@Override
	public void setExtension(IExtension extension) {
		__extension = extension;
	}

	private void __checkSubscriberReconnection(BaseConfiguration configuration) throws NotDefinedSubscribersException {
		if (configuration.getBoolean(BaseConfiguration.KEEP_PLAYER_ON_DISCONNECT)) {
			if (!__eventManager.getExternal().hasSubscriber(TEvent.PLAYER_RECONNECT_REQUEST)
					|| !__eventManager.getExternal().hasSubscriber(TEvent.PLAYER_RECONNECT_SUCCESS)) {
				throw new NotDefinedSubscribersException(TEvent.PLAYER_RECONNECT_REQUEST,
						TEvent.PLAYER_RECONNECT_SUCCESS);
			}
		}
	}

	private void __checkSubscriberSubConnectionAttach(BaseConfiguration configuration)
			throws NotDefinedSubscribersException {
		if (configuration.getSocketPorts().size() > 1 || configuration.getWebSocketPorts().size() > 1) {
			if (!__eventManager.getExternal().hasSubscriber(TEvent.ATTACH_CONNECTION_REQUEST)
					|| !__eventManager.getExternal().hasSubscriber(TEvent.ATTACH_CONNECTION_SUCCESS)
					|| !__eventManager.getExternal().hasSubscriber(TEvent.ATTACH_CONNECTION_FAILED)) {
				throw new NotDefinedSubscribersException(TEvent.ATTACH_CONNECTION_REQUEST,
						TEvent.ATTACH_CONNECTION_SUCCESS, TEvent.ATTACH_CONNECTION_FAILED);
			}
		}
	}

	private void __checkDefinedMainSocketConnection(BaseConfiguration configuration)
			throws NotDefinedSocketConnectionException {
		if (configuration.getSocketPorts().isEmpty() && configuration.getWebSocketPorts().isEmpty()) {
			throw new NotDefinedSocketConnectionException();
		}
	}

	private void __checkSubscriberHttpHandler(BaseConfiguration configuration) throws NotDefinedSubscribersException {
		if (!configuration.getHttpPorts().isEmpty() && (!__eventManager.getExternal().hasSubscriber(TEvent.HTTP_REQUEST)
				|| !__eventManager.getExternal().hasSubscriber(TEvent.HTTP_HANDLER))) {
			throw new NotDefinedSubscribersException(TEvent.HTTP_REQUEST, TEvent.HTTP_HANDLER);
		}
	}

	private void __createAllSchedules(BaseConfiguration configuration) {
		__taskManager.create(Constants.KEY_SCHEDULE_TIME_OUT_SCAN,
				(new TimeOutScanTask(__eventManager, __playerApi, configuration.getInt(BaseConfiguration.IDLE_READER),
						configuration.getInt(BaseConfiguration.IDLE_WRITER),
						configuration.getInt(BaseConfiguration.TIMEOUT_SCAN))).run());
		__taskManager.create(Constants.KEY_SCHEDULE_EMPTY_ROOM_SCAN,
				(new EmptyRoomScanTask(__roomApi, configuration.getInt(BaseConfiguration.EMPTY_ROOM_SCAN))).run());
		__taskManager.create(Constants.KEY_SCHEDULE_CCU_SCAN,
				(new CCUScanTask(__eventManager, __playerApi, configuration.getInt(BaseConfiguration.CCU_SCAN))).run());
	}

	private String __createHttpManagers(BaseConfiguration configuration) throws DuplicatedUriAndMethodException {
		for (var port : configuration.getHttpPorts()) {
			var http = new HttpManagerTask(__eventManager, port.getName(), port.getPort(), port.getPaths());
			http.setup();
			__taskManager.create(Constants.KEY_SCHEDULE_HTTP_MANAGER, http.run());
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
