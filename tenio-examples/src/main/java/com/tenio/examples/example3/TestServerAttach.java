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
package com.tenio.examples.example3;

import com.tenio.AbstractApp;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.element.TObject;
import com.tenio.examples.server.Configuration;
import com.tenio.extension.AbstractExtensionHandler;
import com.tenio.extension.IExtension;
import com.tenio.network.Connection;

/**
 * This class shows how a server handle messages that came from a client
 * 
 * @author kong
 *
 */
public final class TestServerAttach extends AbstractApp {

	/**
	 * The entry point
	 */
	public static void main(String[] args) {
		TestServerAttach game = new TestServerAttach();
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
	 * Your own logic handler class
	 */
	private final class Extenstion extends AbstractExtensionHandler implements IExtension {

		@Override
		public void init() {
			_on(TEvent.CONNECTION_SUCCESS, args -> {
				Connection connection = (Connection) args[0];
				TObject message = (TObject) args[1];

				info("CONNECTION", connection.getAddress());

				// Allow the connection login into server (become a player)
				String username = message.getString("u");
				// Should confirm that credentials by data from database or other services, here
				// is only for testing
				_playerApi.login(new PlayerAttach(username), connection);

				return null;
			});

			_on(TEvent.DISCONNECT_CONNECTION, args -> {
				Connection connection = (Connection) args[0];

				info("DISCONNECT CONNECTION", connection.getAddress());

				return null;
			});

			_on(TEvent.PLAYER_IN_SUCCESS, args -> {
				// The player has login successful
				PlayerAttach player = (PlayerAttach) args[0];

				info("PLAYER IN", player.getName());

				// Now you can allow the player make a UDP connection request
				_messageApi.sendToPlayer(player, "c", "udp");

				return null;
			});

			_on(TEvent.RECEIVED_FROM_PLAYER, args -> {
				PlayerAttach player = (PlayerAttach) args[0];
				boolean isSubConnection = (boolean) args[1];
				TObject message = (TObject) args[2];

				info("PLAYER RECV ", message);

				if (isSubConnection)
					_messageApi.sendToPlayerSub(player, "hello",
							"from server" + player.getSubConnection().getAddress());
				else
					_messageApi.sendToPlayer(player, "hello", "from server" + player.getConnection().getAddress());

				return null;
			});

			_on(TEvent.PLAYER_TIMEOUT, args -> {
				PlayerAttach player = (PlayerAttach) args[0];

				info("PLAYER TIMEOUT", player.getName());

				return null;
			});

			_on(TEvent.DISCONNECT_PLAYER, args -> {
				PlayerAttach player = (PlayerAttach) args[0];

				info("DISCONNECT PLAYER", player.getName());

				return null;
			});

			_on(TEvent.ATTACH_UDP_REQUEST, args -> {
				TObject message = (TObject) args[0];
				String name = message.getString("u");

				// It should be ...
				// 1. check if player has sub connection
				// 2. confirm with player's name and main connection

				// But now temporary returns a player by his name
				return _playerApi.get(name);
			});

			_on(TEvent.ATTACH_UDP_SUCCESS, args -> {
				PlayerAttach player = (PlayerAttach) args[0];

				info("ATTACH UDP SUCCESS", player.getName() + " " + player.getConnection().getAddress() + " "
						+ player.getSubConnection().getAddress());

				_messageApi.sendToPlayer(player, "c", "udp-done");

				return null;
			});

			_on(TEvent.ATTACH_UDP_FAILED, args -> {
				String reason = (String) args[1];

				info("ATTACH UDP FAILED", reason);

				return null;
			});

		}

	}

}
