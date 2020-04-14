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

import com.tenio.AbstractApp;
import com.tenio.configuration.constant.TEvent;
import com.tenio.example.example4.constant.Constants;
import com.tenio.example.example4.entity.Inspector;
import com.tenio.example.server.Configuration;
import com.tenio.extension.AbstractExtensionHandler;
import com.tenio.extension.IExtension;

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
	 * @param args
	 */
	public static void main(String[] args) {
		var game = new TestServerMovement();
		game.start();
	}

	@Override
	public IExtension getExtension() {
		return new Extenstion();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Configuration getConfiguration() {
		return new Configuration("TenIOConfig.attach.xml");
	}

	/**
	 * Handle your own logic here
	 */
	private final class Extenstion extends AbstractExtensionHandler implements IExtension {

		@Override
		public void init() {

			_on(TEvent.CONNECTION_SUCCESS, args -> {
				var connection = _getConnection(args[0]);
				var message = _getTObject(args[1]);

				info("CONNECTION", connection.getAddress());

				// can make a login request for this connection
				String username = message.getString("u");
				_playerApi.login(new Inspector(username), connection);

				return null;
			});

			_on(TEvent.DISCONNECT_CONNECTION, args -> {
				var connection = _getConnection(args[0]);

				info("DISCONNECT CONNECTION", connection.getAddress());

				return null;
			});

			_on(TEvent.PLAYER_IN_SUCCESS, args -> {
				// the player has login successful
				var player = this.<Inspector>_getPlayer(args[0]);
				player.setIgnoreTimeout(true);

				info("PLAYER IN", player.getName());

				// now we can allow that client send request for UDP connection
				_messageApi.sendToPlayer(player, "c", "udp");

				return null;
			});

			_on(TEvent.PLAYER_TIMEOUT, args -> {
				var player = this.<Inspector>_getPlayer(args[0]);

				info("PLAYER TIMEOUT", player.getName());

				return null;
			});

			_on(TEvent.DISCONNECT_PLAYER, args -> {
				var player = this.<Inspector>_getPlayer(args[0]);

				info("DISCONNECT PLAYER", player.getName());

				return null;
			});

			_on(TEvent.ATTACH_UDP_REQUEST, args -> {
				var message = _getTObject(args[0]);
				String name = message.getString("u");

				// It should be ...
				// 1. check if player has sub connection
				// 2. confirm with player's name and main connection

				// But now temporary returns a player by his name
				return _playerApi.get(name);
			});

			_on(TEvent.ATTACH_UDP_SUCCESS, args -> {
				var player = this.<Inspector>_getPlayer(args[0]);

				info("ATTACH UDP SUCCESS", player.getName() + " " + player.getConnection().getAddress() + " "
						+ player.getSubConnection().getAddress());

				_messageApi.sendToPlayer(player, "c", "udp-done");

				return null;
			});

			_on(TEvent.ATTACH_UDP_FAILED, args -> {
				String reason = _getString(args[1]);

				info("ATTACH UDP FAILED", reason);

				return null;
			});

			// Create a world
			var world = new World(Constants.DESIGN_WIDTH, Constants.DESIGN_HEIGHT);
			world.debug("[TenIO] Server Debugger : Movement Simulation");
			_heartbeatApi.create("world", world);

		}

	}

}
