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

import java.io.IOException;

import com.tenio.api.HeartBeatApi;
import com.tenio.api.MessageApi;
import com.tenio.api.PlayerApi;
import com.tenio.api.RoomApi;
import com.tenio.api.TaskApi;
import com.tenio.configuration.BaseConfiguration;
import com.tenio.configuration.constant.TEvent;
import com.tenio.engine.heartbeat.HeartBeatManager;
import com.tenio.engine.heartbeat.IHeartBeatManager;
import com.tenio.entities.manager.IPlayerManager;
import com.tenio.entities.manager.IRoomManager;
import com.tenio.entities.manager.PlayerManager;
import com.tenio.entities.manager.RoomManager;
import com.tenio.event.EventManager;
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
 * See {@link IServer}
 * 
 * @author kong
 * 
 */
public final class Server extends AbstractLogger implements IServer {

	private static Server __instance;

	private Server() {
		__heartBeatManager = new HeartBeatManager();
		__roomManager = new RoomManager();
		__playerManager = new PlayerManager();
		__taskManager = new TaskManager();

		__playerApi = new PlayerApi(__playerManager, __roomManager);
		__roomApi = new RoomApi(__roomManager);
		__heartbeatApi = new HeartBeatApi(__heartBeatManager);
		__taskApi = new TaskApi(__taskManager);
		__messageApi = new MessageApi();

		__logic = new ServerLogic(__playerManager, __roomManager);

	} // prevent creation manually

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static Server getInstance() {
		if (__instance == null) {
			__instance = new Server();
		}
		return __instance;
	}

	private final IHeartBeatManager __heartBeatManager;
	private final IRoomManager __roomManager;
	private final IPlayerManager __playerManager;
	private final ITaskManager __taskManager;

	private final PlayerApi __playerApi;
	private final RoomApi __roomApi;
	private final HeartBeatApi __heartbeatApi;
	private final TaskApi __taskApi;
	private final MessageApi __messageApi;

	/**
	 * @see {@link ServerLogic}
	 */
	private final ServerLogic __logic;
	/**
	 * @see {@link IExtension}
	 */
	private IExtension __extension;
	/**
	 * @see {@link INetwork}
	 */
	private INetwork __network;

	@Override
	public void start(BaseConfiguration configuration) {
		info("SERVER", configuration.getString(BaseConfiguration.SERVER_NAME), "Starting ...");
		try {
			// main server logic
			__logic.init();

			// Datagram connection can not stand alone
			__checkDefinedMainConnection(configuration);

			// schedules
			__createAllSchedules(configuration);

			// start network
			__startNetwork(configuration);

			// initialize heart-beat
			if (configuration.isDefined(BaseConfiguration.MAX_HEARTBEAT)) {
				__heartBeatManager.initialize(configuration);
			}

			// initialize the subscribers
			getExtension().init();

			// check subscribers
			// must handle subscribers for UDP attachment
			__checkSubscriberUDPAttach(configuration);

			// must handle subscribers for reconnection
			__checkSubscriberReconnection(configuration);

			// collect all subscribers, listen all the events
			EventManager.subscribe();

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					shutdown();
				}
			});

			info("SERVER", configuration.getString(BaseConfiguration.SERVER_NAME), "Started!");

		} catch (Exception e) { // if any exception occur, shutdown system immediately
			error("EXCEPTION START", "system", e);
			System.exit(1); // exit with error
		}
	}

	private void __startNetwork(BaseConfiguration configuration) throws IOException, InterruptedException {
		__network = new NettyNetwork();
		__network.start(configuration);
	}

	@Override
	public void shutdown() {
		__network.shutdown();
		// clear all objects
		__heartBeatManager.clear();
		__roomManager.clear();
		__playerManager.clear();
		EventManager.clear();
	}

	@Override
	public IExtension getExtension() {
		return __extension;
	}

	@Override
	public void setExtension(IExtension extension) {
		__extension = extension;
	}

	private void __checkSubscriberReconnection(BaseConfiguration configuration) {
		if (configuration.getBoolean(BaseConfiguration.KEEP_PLAYER_ON_DISCONNECT)) {
			try {
				if (!EventManager.getEvent().hasSubscriber(TEvent.PLAYER_RECONNECT_REQUEST)
						|| !EventManager.getEvent().hasSubscriber(TEvent.PLAYER_RECONNECT_SUCCESS)) {
					throw new Exception(
							new Throwable("Need to implement subscribers: PLAYER_RECONNECT, PLAYER_RECONNECT_SUCCESS"));
				}
			} catch (Exception e) {
				error("EXCEPTION EVENT", "system", e.getCause());
			}
		}
	}

	private void __checkSubscriberUDPAttach(BaseConfiguration configuration) {
		if (configuration.isDefined(BaseConfiguration.DATAGRAM_PORT)) {
			try {
				if (!EventManager.getEvent().hasSubscriber(TEvent.ATTACH_UDP_REQUEST)
						|| !EventManager.getEvent().hasSubscriber(TEvent.ATTACH_UDP_SUCCESS)
						|| !EventManager.getEvent().hasSubscriber(TEvent.ATTACH_UDP_FAILED)) {
					throw new Exception(new Throwable(
							"Need to implement subscribers: ATTACH_UDP_CONDITION, ATTACH_UDP_SUCCESS, ATTACH_UDP_FAILED"));
				}
			} catch (Exception e) {
				error("EXCEPTION EVENT", "system", e.getCause());
			}
		}
	}

	private void __checkDefinedMainConnection(BaseConfiguration configuration) throws Exception {
		if (configuration.isDefined(BaseConfiguration.DATAGRAM_PORT)
				&& !configuration.isDefined(BaseConfiguration.SOCKET_PORT)) {
			throw new Exception(
					new Throwable("Datagram connection can not stand alone, please define the Socket connection too"));
		}
	}

	private void __createAllSchedules(BaseConfiguration configuration) {
		(new TimeOutScanTask(__playerApi, configuration.getInt(BaseConfiguration.IDLE_READER),
				configuration.getInt(BaseConfiguration.IDLE_WRITER),
				configuration.getInt(BaseConfiguration.TIMEOUT_SCAN))).run();
		(new EmptyRoomScanTask(__roomApi, configuration.getInt(BaseConfiguration.EMPTY_ROOM_SCAN))).run();
		(new CCUScanTask(__playerApi, configuration.getInt(BaseConfiguration.CCU_SCAN))).run();
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

}
