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
package com.tenio.server;

import com.tenio.api.HeartBeatApi;
import com.tenio.api.MessageApi;
import com.tenio.api.PlayerApi;
import com.tenio.api.RoomApi;
import com.tenio.api.TaskApi;
import com.tenio.configuration.BaseConfiguration;
import com.tenio.configuration.constant.Constants;
import com.tenio.configuration.constant.TEvent;
import com.tenio.engine.heartbeat.HeartBeatManager;
import com.tenio.engine.heartbeat.IHeartBeatManager;
import com.tenio.entity.manager.IPlayerManager;
import com.tenio.entity.manager.IRoomManager;
import com.tenio.entity.manager.PlayerManager;
import com.tenio.entity.manager.RoomManager;
import com.tenio.event.EventManager;
import com.tenio.event.IEventManager;
import com.tenio.exception.NotDefinedSocketConnectionException;
import com.tenio.exception.NotDefinedSubscribersException;
import com.tenio.extension.IExtension;
import com.tenio.logger.AbstractLogger;
import com.tenio.network.INetwork;
import com.tenio.network.netty.NettyNetwork;
import com.tenio.task.ITaskManager;
import com.tenio.task.TaskManager;
import com.tenio.task.schedule.CCUScanTask;
import com.tenio.task.schedule.EmptyRoomScanTask;
import com.tenio.task.schedule.TimeOutScanTask;

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

		__heartBeatManager = new HeartBeatManager();
		__roomManager = new RoomManager(__eventManager);
		__playerManager = new PlayerManager(__eventManager);
		__taskManager = new TaskManager();

		__playerApi = new PlayerApi(__playerManager, __roomManager);
		__roomApi = new RoomApi(__roomManager);
		__heartbeatApi = new HeartBeatApi(__heartBeatManager);
		__taskApi = new TaskApi(__taskManager);
		__messageApi = new MessageApi(__eventManager);

		__internalLogic = new InternalLogic(__eventManager, __playerManager, __roomManager);

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

	private final IHeartBeatManager __heartBeatManager;
	private final IRoomManager __roomManager;
	private final IPlayerManager __playerManager;
	private final ITaskManager __taskManager;

	private final PlayerApi __playerApi;
	private final RoomApi __roomApi;
	private final HeartBeatApi __heartbeatApi;
	private final TaskApi __taskApi;
	private final MessageApi __messageApi;

	private final InternalLogic __internalLogic;
	private IExtension __extension;
	private INetwork __network;

	@Override
	public boolean start(BaseConfiguration configuration) {
		info("SERVER", configuration.getString(BaseConfiguration.SERVER_NAME), "Starting ...");

		// main server logic
		__internalLogic.init();

		// datagram connection can not stand alone
		if (!__checkDefinedMainConnection(configuration)) {
			return false;
		}

		// schedules
		__createAllSchedules(configuration);

		// start network
		if (!__startNetwork(configuration)) {
			return false;
		}

		// initialize heart-beat
		if (configuration.isDefined(BaseConfiguration.MAX_HEARTBEAT)) {
			__heartBeatManager.initialize(configuration);
		}

		// initialize the subscribers
		getExtension().init();

		// check subscribers must handle subscribers for UDP attachment
		if (!__checkSubscriberUDPAttach(configuration)) {
			return false;
		}

		// must handle subscribers for reconnection
		if (!__checkSubscriberReconnection(configuration)) {
			return false;
		}

		// collect all subscribers, listen all the events
		__eventManager.subscribe();

		info("SERVER", configuration.getString(BaseConfiguration.SERVER_NAME), "Started!");

		return true;
	}

	private boolean __startNetwork(BaseConfiguration configuration) {
		__network = new NettyNetwork();
		return __network.start(__eventManager, configuration);
	}

	@Override
	public void shutdown() {
		if (__network != null) {
			__network.shutdown();
		}
		// clear all objects
		__heartBeatManager.clear();
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

	private boolean __checkSubscriberReconnection(BaseConfiguration configuration) {
		if (configuration.getBoolean(BaseConfiguration.KEEP_PLAYER_ON_DISCONNECT)) {
			if (!__eventManager.getExternal().hasSubscriber(TEvent.PLAYER_RECONNECT_REQUEST)
					|| !__eventManager.getExternal().hasSubscriber(TEvent.PLAYER_RECONNECT_SUCCESS)) {
				var e = new NotDefinedSubscribersException(TEvent.PLAYER_RECONNECT_REQUEST,
						TEvent.PLAYER_RECONNECT_SUCCESS);
				error(e);
				return false;
			}
		}
		return true;
	}

	private boolean __checkSubscriberUDPAttach(BaseConfiguration configuration) {
		if (configuration.isDefined(BaseConfiguration.DATAGRAM_PORT)) {
			if (!__eventManager.getExternal().hasSubscriber(TEvent.ATTACH_UDP_REQUEST)
					|| !__eventManager.getExternal().hasSubscriber(TEvent.ATTACH_UDP_SUCCESS)
					|| !__eventManager.getExternal().hasSubscriber(TEvent.ATTACH_UDP_FAILED)) {
				var e = new NotDefinedSubscribersException(TEvent.ATTACH_UDP_REQUEST, TEvent.ATTACH_UDP_SUCCESS,
						TEvent.ATTACH_UDP_FAILED);
				error(e);
				return false;
			}
		}
		return true;
	}

	private boolean __checkDefinedMainConnection(BaseConfiguration configuration) {
		if (configuration.isDefined(BaseConfiguration.DATAGRAM_PORT)
				&& !configuration.isDefined(BaseConfiguration.SOCKET_PORT)) {
			var e = new NotDefinedSocketConnectionException();
			error(e);
			return false;
		}
		return true;
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
	public HeartBeatApi getHeartBeatApi() {
		return __heartbeatApi;
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
