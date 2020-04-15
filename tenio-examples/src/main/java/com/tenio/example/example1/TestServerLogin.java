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
package com.tenio.example.example1;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tenio.AbstractApp;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entity.annotation.EntityProcess;
import com.tenio.entity.element.TArray;
import com.tenio.example.server.Configuration;
import com.tenio.extension.AbstractExtensionHandler;
import com.tenio.extension.IExtension;

/**
 * This class shows how a server handle messages that came from a client
 * 
 * @author kong
 *
 */
public final class TestServerLogin extends AbstractApp {

	/**
	 * The entry point
	 */
	public static void main(String[] args) {
		var game = new TestServerLogin();
		game.start();
	}

	@Override
	public IExtension getExtension() {
		return new Extenstion();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Configuration getConfiguration() {
		return new Configuration("TenIOConfig.xml");
	}

	/**
	 * Your own logic handler class
	 */
	private final class Extenstion extends AbstractExtensionHandler implements IExtension {

		@Override
		public void init() {
			_on(TEvent.CONNECTION_SUCCESS, args -> {
				var connection = _getConnection(args[0]);
				var message = _getTObject(args[1]);

				info("CONNECTION", connection.getAddress());

				// Allow the connection login into server (become a player)
				String username = message.getString("u");
				// Should confirm that credentials by data from database or other services, here
				// is only for testing
				_playerApi.login(new PlayerLogin(username), connection);

				return null;
			});

			_on(TEvent.DISCONNECT_CONNECTION, args -> {
				var connection = _getConnection(args[0]);

				info("DISCONNECT CONNECTION", connection.getAddress());

				return null;
			});

			_on(TEvent.PLAYER_IN_SUCCESS, args -> {
				// The player has login successful
				var player = this.<PlayerLogin>_getPlayer(args[0]);

				info("PLAYER IN", player.getName());
				try {
					info("PLAYER BACKUP", EntityProcess.exportToJSON(player));
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Now you can send messages to the client
				_taskApi.run(player.getName(), Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
					// Only sent 10 messages
					if (player.counter >= 10) {
						_taskApi.kill(player.getName());
						_playerApi.logOut(player);
					}

					player.counter++;

					// Sending, the data need to be packed
					var data = _messageApi.getArrayPack();
					_messageApi.sendToPlayer(player, "c", "message", "d", data.put("H").put("3").put("L").put("O")
							.put(true).put(TArray.newInstance().put("Sub").put("Value").put(100)));

					try {
						info("PLAYER BACKUP", EntityProcess.exportToJSON(player));
					} catch (Exception e) {
						e.printStackTrace();
					}

				}, 0, 1, TimeUnit.SECONDS));

				return null;
			});

			_on(TEvent.PLAYER_TIMEOUT, args -> {
				var player = this.<PlayerLogin>_getPlayer(args[0]);

				info("PLAYER TIMEOUT", player.getName());

				return null;
			});

			_on(TEvent.DISCONNECT_PLAYER, args -> {
				var player = this.<PlayerLogin>_getPlayer(args[0]);

				info("DISCONNECT PLAYER", player.getName());

				return null;
			});

			_on(TEvent.SEND_TO_PLAYER, args -> {
				var message = _getTObject(args[2]);

				info("SEND_TO_PLAYER", message.toString());

				return null;
			});

		}

	}

}
