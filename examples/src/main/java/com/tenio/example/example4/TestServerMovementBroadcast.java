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
package com.tenio.example.example4;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.core.AbstractApp;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.IExtension;
import com.tenio.engine.heartbeat.HeartBeatManager;
import com.tenio.example.example4.constant.Constants;
import com.tenio.example.example4.entity.Inspector;
import com.tenio.example.server.Configuration;

/**
 * This class makes a simple simulator for the physic 2d movement.
 * 
 * @author kong
 *
 */
public final class TestServerMovementBroadcast extends AbstractApp {

	private static final int CONVERT_TO_MB = 1024 * 1024;

	/**
	 * The entry point
	 * 
	 * @param params
	 */
	public static void main(String[] params) {
		var game = new TestServerMovementBroadcast();
		game.start();
	}

	@Override
	public IExtension getExtension() {
		return new Extenstion();
	}

	@Override
	public Configuration getConfiguration() {
		return new Configuration("TenIOConfig.broadcast.xml");
	}

	@Override
	public void onStarted(IExtension extension, IConfiguration configuration) {

	}

	@Override
	public void onShutdown() {

	}

	/**
	 * Handle your own logic here
	 */
	private final class Extenstion extends AbstractExtensionHandler implements IExtension {

		@Override
		public void initialize(IConfiguration configuration) {

			_on(ExtEvent.CONNECTION_ESTABLISHED_SUCCESS, params -> {
				var connection = _getConnection(params[0]);
				var message = _getCommonObject(params[1]);

				// can make a login request for this connection
				var playerName = message.getString("u");
				_playerApi.login(new Inspector(playerName), connection);

				return null;
			});

			_on(ExtEvent.PLAYER_LOGINED_SUCCESS, params -> {
				// the player has login successful
				var player = (Inspector) _getPlayer(params[0]);
				player.setIgnoreTimeout(true);

				_info("PLAYER_LOGINED_SUCCESS", player.getName());

				// now we can allow that client send request for UDP connection
				_messageApi.sendToPlayer(player, Inspector.MAIN_CHANNEL, "c", "broadcast");

				return null;
			});

			_on(ExtEvent.FETCHED_CCU_NUMBER, params -> {
				var ccu = _getInteger(params[0]);

				_info("FETCHED_CCU_NUMBER", ccu);

				return null;
			});

			_on(ExtEvent.FETCHED_BANDWIDTH_INFO, params -> {
				long lastReadThroughput = _getLong(params[0]);
				long lastWriteThroughput = _getLong(params[1]);
				long realWriteThroughput = _getLong(params[2]);
				long currentReadBytes = _getLong(params[3]);
				long currentWrittenBytes = _getLong(params[4]);
				long realWrittenBytes = _getLong(params[5]);
				String name = _getString(params[6]);

				var bandwidth = String.format(
						"name=%s;lastReadThroughput=%dKB/s;lastWriteThroughput=%dKB/s;realWriteThroughput=%dKB/s;currentReadBytes=%dKB;currentWrittenBytes=%dKB;realWrittenBytes=%dKB",
						name, lastReadThroughput, lastWriteThroughput, realWriteThroughput, currentReadBytes,
						currentWrittenBytes, realWrittenBytes);

				_info("FETCHED_BANDWIDTH_INFO", bandwidth);

				return null;
			});

			_on(ExtEvent.MONITORING_SYSTEM, params -> {
				double cpuUsage = _getDouble(params[0]);
				long totalMemory = _getLong(params[1]);
				long usedMemory = _getLong(params[2]);
				long freeMemory = _getLong(params[3]);
				int countRunningThreads = _getInteger(params[4]);

				var info = String.format(
						"cpuUsage=%f;totalMemory=%.3fMB;usedMemory=%.3fMB;freeMemory=%.3fMB;runningThreads=%d",
						cpuUsage, (float) totalMemory / CONVERT_TO_MB, (float) usedMemory / CONVERT_TO_MB,
						(float) freeMemory / CONVERT_TO_MB, countRunningThreads);

				_info("MONITORING_SYSTEM", info);

				return null;
			});

			// Create a world
			var world = new World(Constants.DESIGN_WIDTH, Constants.DESIGN_HEIGHT, true);
			// world.debug("[TenIO] Server Debugger : Movement Simulation");
			var hearbeatManager = new HeartBeatManager();
			try {
				hearbeatManager.initialize(1);
				hearbeatManager.create("world", world);
			} catch (Exception e) {
				_error(e, "world");
			}

		}

	}

}
