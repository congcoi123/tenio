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
package com.tenio.examples.example4;

import com.tenio.AbstractApp;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.element.TObject;
import com.tenio.examples.example4.constants.Constants;
import com.tenio.examples.example4.entities.Inspector;
import com.tenio.examples.server.Configuration;
import com.tenio.extension.AbstractExtensionHandler;
import com.tenio.extension.IExtension;
import com.tenio.net.Connection;

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
		TestServerMovement game = new TestServerMovement();
		game.setup();
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

			on(TEvent.CONNECTION_SUCCESS, (source, args) -> {
				Connection connection = (Connection) args[0];
				TObject message = (TObject) args[1];

				info("CONNECTION", connection.getAddress());

				// can make a login request for this connection
				String username = message.getString("u");
				_playerApi.login(new Inspector(username), connection);

				return null;
			});

			on(TEvent.DISCONNECT_CONNECTION, (source, args) -> {
				Connection connection = (Connection) args[0];

				info("DISCONNECT CONNECTION", connection.getAddress());

				return null;
			});

			on(TEvent.PLAYER_IN_SUCCESS, (source, args) -> {
				// the player has login successful
				Inspector player = (Inspector) args[0];
				player.isIgnoreTimeout();

				info("PLAYER IN", player.getName());

				// now we can allow that client send request for UDP connection
				_messageApi.sendToPlayer(player, "c", "udp");

				return null;
			});

			on(TEvent.PLAYER_TIMEOUT, (source, args) -> {
				Inspector player = (Inspector) args[0];

				info("PLAYER TIMEOUT", player.getName());

				return null;
			});

			on(TEvent.DISCONNECT_PLAYER, (source, args) -> {
				Inspector player = (Inspector) args[0];

				info("DISCONNECT PLAYER", player.getName());

				return null;
			});

			on(TEvent.ATTACH_UDP_REQUEST, (source, args) -> {
				TObject message = (TObject) args[0];
				String name = message.getString("u");

				// It should be ...
				// 1. check if player has sub connection
				// 2. confirm with player's name and main connection

				// But now temporary returns a player by his name
				return _playerApi.get(name);
			});

			on(TEvent.ATTACH_UDP_SUCCESS, (source, args) -> {
				Inspector player = (Inspector) args[0];

				info("ATTACH UDP SUCCESS", player.getName() + " " + player.getConnection().getAddress() + " "
						+ player.getSubConnection().getAddress());

				_messageApi.sendToPlayer(player, "c", "udp-done");

				return null;
			});

			on(TEvent.ATTACH_UDP_FAILED, (source, args) -> {
				String reason = (String) args[1];

				info("ATTACH UDP FAILED", reason);

				return null;
			});

			// Create a world
			World world = new World(Constants.DESIGN_WIDTH, Constants.DESIGN_HEIGHT);
			world.debug("[TenIO] Server Debugger : Movement Simulation");
			_heartbeatApi.create("world", world);

		}

	}

}
