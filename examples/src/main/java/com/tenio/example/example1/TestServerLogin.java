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
package com.tenio.example.example1;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tenio.common.configuration.ZConfiguration;
import com.tenio.common.data.element.CommonObject;
import com.tenio.common.data.element.CommonObjectArray;
import com.tenio.core.AbstractApp;
import com.tenio.core.configuration.define.ZeroEvent;
import com.tenio.core.entity.backup.EntityProcesser;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.IExtension;
import com.tenio.example.server.Configuration;

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
	public static void main(String[] params) {
		var game = new TestServerLogin();
		game.start();
	}

	@Override
	public IExtension getExtension() {
		return new Extenstion();
	}

	@Override
	public Configuration getConfiguration() {
		return new Configuration("TenIOConfig.xml");
	}

	@Override
	public void onStarted(IExtension extension, ZConfiguration configuration) {

	}

	@Override
	public void onShutdown() {

	}

	/**
	 * Your own logic handler class
	 */
	private final class Extenstion extends AbstractExtensionHandler implements IExtension {

		@Override
		public void initialize(ZConfiguration configuration) {
			_on(ZeroEvent.CONNECTION_ESTABLISHED_SUCCESS, params -> {
				var connection = _getConnection(params[0]);
				var message = _getCommonObject(params[1]);

				// Allow the connection login into server (become a player)
				var playerName = message.getString("u");
				// Should confirm that credentials by data from database or other services, here
				// is only for testing
				_playerApi.login(new PlayerLogin(playerName), connection);

				return null;
			});

			_on(ZeroEvent.PLAYER_LOGINED_SUCCESS, params -> {
				// The player has login successful
				var player = (PlayerLogin) _getPlayer(params[0]);

				try {
					_info("PLAYER BACKUP", EntityProcesser.exportToJSON(player));
				} catch (Exception e) {
					_error(e, player.getName());
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
					var data = _messageApi.getMessageObjectArray();
					_messageApi.sendToPlayer(player, PlayerLogin.MAIN_CHANNEL, "c", "message", "d",
							data.put("H").put("3").put("L").put("O").put(true)
									.put(CommonObjectArray.newInstance().put("Sub").put("Value").put(100)));

					// Attempt to send internal message
					_messageApi.sendToInternalServer(player, 1,
							CommonObject.newInstance().add("internal", "this is a message in external server"));

				}, 0, 1, TimeUnit.SECONDS));

				return null;
			});

			_on(ZeroEvent.RECEIVED_MESSAGE_FROM_PLAYER, params -> {
				var index = _getInteger(params[1]);
				var message = _getCommonObject(params[2]);

				_info("RECEIVED INTERNAL MESSAGE", _buildgen("Index: ", index, " Content: ", message));

				return null;
			});

		}

	}

}
