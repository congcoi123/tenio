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
package com.tenio.engine.server;

import com.tenio.common.api.TaskApi;
import com.tenio.common.configuration.constant.CommonConstants;
import com.tenio.common.extension.IExtension;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.task.ITaskManager;
import com.tenio.common.task.TaskManager;
import com.tenio.engine.api.HeartBeatApi;
import com.tenio.engine.configuration.BaseConfiguration;
import com.tenio.engine.heartbeat.HeartBeatManager;
import com.tenio.engine.heartbeat.IHeartBeatManager;

/**
 * 
 * @see IEngine
 * 
 * @author kong
 * 
 */
public final class Engine extends AbstractLogger implements IEngine {

	private static Engine __instance;

	private Engine() {
		__heartBeatManager = new HeartBeatManager();
		__taskManager = new TaskManager();

		__heartbeatApi = new HeartBeatApi(__heartBeatManager);
		__taskApi = new TaskApi(__taskManager);

		// print out the logo
		for (var line : CommonConstants.LOGO) {
			info("", "", line);
		}
	} // prevent creation manually

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static Engine getInstance() {
		if (__instance == null) {
			__instance = new Engine();
		}
		return __instance;
	}

	private final IHeartBeatManager __heartBeatManager;
	private final ITaskManager __taskManager;

	private final HeartBeatApi __heartbeatApi;
	private final TaskApi __taskApi;
	private IExtension __extension;

	@Override
	public void start(BaseConfiguration configuration) throws Exception {
		info("ENGINE", configuration.getString(BaseConfiguration.SERVER_NAME), "Starting ...");

		// initialize the subscribers
		getExtension().initialize();
		
		// initialize the heartbeat manager
		__heartBeatManager.initialize(configuration);

		info("ENGINE", configuration.getString(BaseConfiguration.SERVER_NAME), "Started!");
	}

	@Override
	public void shutdown() {
		// clear all objects
		__heartBeatManager.clear();
		__taskManager.clear();
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

	@Override
	public HeartBeatApi getHeartBeatApi() {
		return __heartbeatApi;
	}

	@Override
	public TaskApi getTaskApi() {
		return __taskApi;
	}

}
