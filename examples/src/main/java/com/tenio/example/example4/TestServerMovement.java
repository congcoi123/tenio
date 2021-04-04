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
public final class TestServerMovement extends AbstractApp {

	/**
	 * The entry point
	 * 
	 * @param params
	 */
	public static void main(String[] params) {
		var game = new TestServerMovement();
		game.start();
	}

	@Override
	public IExtension getExtension() {
		return new Extenstion();
	}

	@Override
	public Configuration getConfiguration() {
		return new Configuration("TenIOConfig.attach.xml");
	}

	@Override
	public void onStarted() {

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
				String username = message.getString("u");
				_playerApi.login(new Inspector(username), connection);

				return null;
			});

			_on(ExtEvent.PLAYER_LOGINED_SUCCESS, params -> {
				// the player has login successful
				var player = (Inspector) _getPlayer(params[0]);
				player.setIgnoreTimeout(true);

				// now we can allow that client send request for UDP connection
				_messageApi.sendToPlayer(player, Inspector.MAIN_CHANNEL, "c", "udp");

				return null;
			});

			_on(ExtEvent.ATTACH_CONNECTION_REQUEST_VALIDATE, params -> {
				var message = _getCommonObject(params[1]);
				String name = message.getString("u");

				// It should be ...
				// 1. check if player has sub connection
				// 2. confirm with player's name and main connection

				// But now temporary returns a player by his name
				return _playerApi.get(name);
			});

			_on(ExtEvent.ATTACH_CONNECTION_SUCCESS, params -> {
				var player = (Inspector) _getPlayer(params[1]);

				_messageApi.sendToPlayer(player, Inspector.MAIN_CHANNEL, "c", "udp-done");

				return null;
			});

			// Create a world
			var world = new World(Constants.DESIGN_WIDTH, Constants.DESIGN_HEIGHT);
			world.debug("[TenIO] Server Debugger : Movement Simulation");
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
