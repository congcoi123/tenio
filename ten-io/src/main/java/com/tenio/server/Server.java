/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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

import com.tenio.configuration.BaseConfiguration;
import com.tenio.configuration.constant.Constants;
import com.tenio.configuration.constant.TEvent;
import com.tenio.engine.heartbeat.HeartBeatManager;
import com.tenio.entities.manager.PlayerManager;
import com.tenio.entities.manager.RoomManager;
import com.tenio.event.EventManager;
import com.tenio.event.logic.LEventManager;
import com.tenio.event.main.TEventManager;
import com.tenio.extension.IExtension;
import com.tenio.logger.AbstractLogger;
import com.tenio.net.INetwork;
import com.tenio.net.mina.MinaNetwork;
import com.tenio.net.netty.NettyNetwork;
import com.tenio.task.schedule.CCUScanTask;
import com.tenio.task.schedule.EmptyRoomScanTask;
import com.tenio.task.schedule.TimeOutScanTask;

/**
 * @see {@link IServer}
 * 
 * @author kong
 * 
 */
public final class Server extends AbstractLogger implements IServer {

	private static volatile Server __instance;

	private Server() {
		
	} // prevent creation manually

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static Server getInstance() {
		if (__instance == null) {
			__instance = new Server();
		}
		return __instance;
	}

	/**
	 * @see {@link TEventManager}
	 */
	private TEventManager __events;

	/**
	 * @see {@link LEventManager}
	 */
	private LEventManager __logicEvents;
	
	/**
	 * @see {@link HeartBeatManager}
	 */
	private HeartBeatManager __heartBeatManager;
	
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
		info("SERVER", (String) configuration.get(BaseConfiguration.SERVER_NAME), "Starting ...");
		try {
			// main server logic
			new ServerLogic();
			
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
			EventManager.getEvent().subscribe();
			EventManager.getLogic().subscribe();

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					shutdown();
				}
			});

			info("SERVER", (String) configuration.get(BaseConfiguration.SERVER_NAME), "Started!");

		} catch (Exception e) { // if any exception occur, shutdown system immediately
			error("EXCEPTION START", "system", e);
			System.exit(1); // exit with error
		}
	}

	private void __startNetwork(BaseConfiguration configuration) throws IOException, InterruptedException {
		switch ((int) configuration.get(BaseConfiguration.NIO)) {
		case Constants.NETTY:
			__network = new NettyNetwork();
			break;
		case Constants.MINA:
			__network = new MinaNetwork();
			break;
		default:
			__network = new NettyNetwork();
			break;
		}
		__network.start(configuration);
	}

	@Override
	public void shutdown() {
		__network.shutdown();
		// clear all objects
		__events.clear();
		__logicEvents.clear();
		__heartBeatManager.clear();
		TEventManager.getInstance().clear();
		RoomManager.getInstance().clear();
		PlayerManager.getInstance().clear();
		HeartBeatManager.getInstance().clear();
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
		if ((boolean) configuration.get(BaseConfiguration.KEEP_PLAYER_ON_DISCONNECT)) {
			try {
				if (!__events.hasSubscriber(TEvent.PLAYER_RECONNECT_REQUEST)
						|| !__events.hasSubscriber(TEvent.PLAYER_RECONNECT_SUCCESS)) {
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
				if (!__events.hasSubscriber(TEvent.ATTACH_UDP_REQUEST)
						|| !__events.hasSubscriber(TEvent.ATTACH_UDP_SUCCESS)
						|| !__events.hasSubscriber(TEvent.ATTACH_UDP_FAILED)) {
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
		(new TimeOutScanTask((int) configuration.get(BaseConfiguration.IDLE_READER),
				(int) configuration.get(BaseConfiguration.IDLE_WRITER),
				(int) configuration.get(BaseConfiguration.TIMEOUT_SCAN))).run();
		(new EmptyRoomScanTask((int) configuration.get(BaseConfiguration.EMPTY_ROOM_SCAN))).run();
		(new CCUScanTask((int) configuration.get(BaseConfiguration.CCU_SCAN))).run();
	}

}
